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
public class RealtimeSubGroupSampler extends SubscribeSampler {
	private static final long serialVersionUID = 2979978053740194951L;
	private static final Logger logger = LoggerFactory.getLogger(RealtimeSubSampler.class.getCanonicalName());

	private transient AblyRealtime[] clients = null;
	private transient RealtimeSubscription[] subscriptions = null;

	public RealtimeSubGroupSampler() {
		super(logger);
	}

	@Override
	public SampleResult sample(Entry arg0) {
		logger.debug("sample");
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		clients = (AblyRealtime[]) vars.getObject(BaseSampler.REALTIME_CLIENT_GROUP);
		if (clients == null) {
			return fillFailedResult(result, "Subscribe failed because connections have not been established.", 500);
		}

		SubscriptionCondition subCondition = new SubscriptionCondition();
		String validateErr = subCondition.validate();
		if(validateErr != null) {
			return fillFailedResult(result, validateErr, 500);
		}

		final String channelName = getChannelPrefix();
		subscriptions = new RealtimeSubscription[clients.length];
		for(int i = 0; i < clients.length; i++) {
			final RealtimeSubscription subscription = subscriptions[i] = new RealtimeSubscription(logger, subCondition, clients[i], channelName);
			final ErrorInfo subError = subscription.subscribe();
			if (subError != null) {
				closeSubscriptions();
				return fillFailedResult(result, subError);
			}
		}

		result.sampleStart();
		subCondition.waitSampleTime();
		for(RealtimeSubscription subscription : subscriptions) {
			subscription.waitForCount();
		}
		return produceResult(result, channelName);
	}

	private void closeSubscriptions() {
		if(subscriptions != null) {
			for(RealtimeSubscription subscription : subscriptions) {
				if(subscription != null) {
					subscription.unsubscribe();
				}
			}
		}
	}

	@Override
	public void threadFinished() {
		closeSubscriptions();
	}
}
