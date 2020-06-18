package io.ably.jmeter.samplers;

import io.ably.jmeter.Util;
import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.realtime.CompletionListener;
import io.ably.lib.types.ErrorInfo;
import io.ably.lib.types.Message;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * A sampler that publishes a single message on a previously established Ably realtime connection.
 * Publishes are made in transient mode to the given channel
 */
public class RealtimePubSampler extends BaseSampler {
	private static final long serialVersionUID = 4312341622759500786L;
	private static final Logger logger = LoggerFactory.getLogger(RealtimePubSampler.class.getCanonicalName());

	public RealtimePubSampler() {
		super(logger);
	}

	private static class PubResult implements CompletionListener {
		private ErrorInfo error;

		private synchronized ErrorInfo waitForResult() throws InterruptedException {
			wait();
			return error;
		}

		@Override
		public synchronized void onSuccess() {
			notify();
		}

		@Override
		public void onError(ErrorInfo reason) {
			this.error = reason;
			notify();
		}
	}

	private Object payload = null;
	private String channelName = "";

	@Override
	public SampleResult sample(Entry arg0) {
		logger.debug("sample");
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());
	
		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		AblyRealtime client = (AblyRealtime) vars.getObject(BaseSampler.REALTIME_CLIENT);
		if(client == null) {
			return fillFailedResult(result, "Connection not found", 500);
		}

		String clientId = client.options.clientId;
		try {
			payload = getPayload();
			channelName = getChannelName();
			vars.putObject(BaseSampler.CHANNEL_NAME, channelName);
			if(logger.isDebugEnabled()) {
				logger.debug("pub [clientId]: " + clientId + ", [channel]: " + channelName + ", [payload]: " + Util.displayPayload(payload));
			}

			final PubResult pubOutcome = new PubResult();
			Message msg = getMessage(payload);

			result.sampleStart();
			client.channels.get(channelName).publish(msg, pubOutcome);
			ErrorInfo error = pubOutcome.waitForResult();

			result.setSamplerData(Util.displayPayload(payload));
			result.setSentBytes(Util.payloadLength(payload));

			if(error == null) {
				return fillOKResult(result);
			} else {
				logger.info(MessageFormat.format("** [clientId: {0}, channel: {1}, payload: {2}] Publish failed for connection {3}.", (clientId == null ? "null" : clientId),
						channelName, Util.displayPayload(payload), client));
				return fillFailedResult(result, error);
			}

		} catch (Exception e) {
			logger.error("Publish failed for connection " + client, e);
			return fillFailedResult(result, "Publish failed for connection" + e.getMessage(), 500);
		}
	}
}
