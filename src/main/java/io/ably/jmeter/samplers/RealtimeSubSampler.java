package io.ably.jmeter.samplers;

import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.realtime.Channel;
import io.ably.lib.types.AblyException;
import io.ably.lib.types.ErrorInfo;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A sampler that makes a subscription to a channel using an already-established realtime connection
 */
public class RealtimeSubSampler extends SubscribeSampler {
	private static final long serialVersionUID = 2979978053740194951L;
	private static final Logger logger = LoggerFactory.getLogger(RealtimeSubSampler.class.getCanonicalName());

	private transient AblyRealtime client = null;

	public RealtimeSubSampler() {
		super(logger);
	}

	@Override
	public SampleResult sample(Entry arg0) {
		logger.debug("sample");
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		client = (AblyRealtime) vars.getObject(BaseSampler.REALTIME_CLIENT);
		if(client == null) {
			return fillFailedResult(result, "500", "Subscribe failed because connection is not established.");
		}

		SubscriptionCondition subCondition = new SubscriptionCondition();
		String validateErr = subCondition.validate();
		if(validateErr != null) {
			return fillFailedResult(result, "500", validateErr);
		}

		String channelName = getChannelPrefix();
		ErrorInfo subError = subscribe(channelName, subCondition);
		if(subError != null) {
			return fillFailedResult(result, String.valueOf(subError.statusCode), "Failed to subscribe to channel:" + channelName + "; error: " + subError.message);
		}

		result.sampleStart();
		subCondition.waitForCondition(logger);
		return produceResult(result, channelName);
	}

	private ErrorInfo subscribe(final String channelName, final SubscriptionCondition subCondition) {
		final Channel channel = client.channels.get(channelName);
		ErrorInfo subError;
		try {
			SubResult subResult = new SubResult();
			channel.attach(subResult);
			subError = subResult.waitForResult();
		} catch (AblyException e) {
			logger.info("attach failed", e);
			subError = e.errorInfo;
		} catch (InterruptedException e) {
			logger.info("attach failed", e);
			subError = new ErrorInfo(e.getMessage(), 50000, 500);
		}
		if(subError == null) {
			try {
				channel.subscribe(message -> {
					if(subCondition.sampleByTime) {
						synchronized(subCondition) {
							addMessageToBean(subCondition, message);
						}
					} else {
						synchronized(subCondition) {
							Bean bean = addMessageToBean(subCondition, message);
							if(bean.getReceivedCount() == subCondition.sampleCount) {
								subCondition.notify();
							}
						}
					}
				});
			} catch (AblyException e) {
				subError = e.errorInfo;
			}
		}
		return subError;
	}
}
