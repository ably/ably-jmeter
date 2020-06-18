package io.ably.jmeter.samplers;

import io.ably.lib.realtime.AblyRealtime;
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
	private transient RealtimeSubscription subscription = null;

	public RealtimeSubSampler() {
		super(logger);
	}

	@Override
	public SampleResult sample(Entry arg0) {
		logger.debug("sample");
		final SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		client = (AblyRealtime) vars.getObject(BaseSampler.REALTIME_CLIENT);
		if(client == null) {
			return fillFailedResult(result, "Subscribe failed because connection is not established.", 500);
		}

		final SubscriptionCondition subCondition = new SubscriptionCondition();
		final String validateErr = subCondition.validate();
		if(validateErr != null) {
			return fillFailedResult(result, validateErr, 500);
		}

		final String channelName = getChannelPrefix();
		subscription = new RealtimeSubscription(logger, subCondition, client, channelName);
		final ErrorInfo subError = subscription.subscribe();
		if(subError != null) {
			return fillFailedResult(result, subError);
		}

		result.sampleStart();
		subCondition.waitSampleTime();
		subscription.waitForCount();
		return produceResult(result, channelName);
	}

	private void closeSubscription() {
		if(subscription != null) {
			subscription.unsubscribe();
		}
	}

	@Override
	public void threadFinished() {
		closeSubscription();
	}
}
