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
			result.sampleStart();
			result.setSuccessful(false);
			result.setResponseMessage("Publish: Connection not found.");
			result.setResponseData("Publish failed because connection is not established.".getBytes());
			result.setResponseCode("500");
			result.sampleEnd(); // avoid endtime=0 exposed in trace log
			return result;
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
			result.sampleEnd();

			result.setSamplerData(Util.displayPayload(payload));
			result.setSentBytes(Util.payloadLength(payload));
			result.setLatency(result.getEndTime() - result.getStartTime());

			if(error == null) {
				result.setSuccessful(true);
				result.setResponseData("Publish successfully.".getBytes());
				result.setResponseMessage(MessageFormat.format("publish successfully for Connection {0}.", client));
				result.setResponseCodeOK();
			} else {
				result.setSuccessful(false);
				result.setResponseMessage(MessageFormat.format("Publish failed for connection {0}.", client));
				result.setResponseData(MessageFormat.format("Client [{0}] publish failed: {1}", (clientId == null ? "null" : clientId), error.message).getBytes());
				result.setResponseCode(String.valueOf(error.statusCode));
				logger.info(MessageFormat.format("** [clientId: {0}, channel: {1}, payload: {2}] Publish failed for connection {3}.", (clientId == null ? "null" : clientId),
						channelName, Util.displayPayload(payload), client));
			}

		} catch (Exception ex) {
			logger.error("Publish failed for connection " + client, ex);
			if(result.getEndTime() == 0) result.sampleEnd();
			result.setLatency(result.getEndTime() - result.getStartTime());
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Publish failed for connection {0}.", client));
			result.setResponseData(MessageFormat.format("Client [{0}] publish failed: {1}", (clientId == null ? "null" : clientId), ex.getMessage()).getBytes());
			result.setResponseCode("502");
			if(logger.isWarnEnabled()) {
				logger.warn(MessageFormat.format("** [clientId: {0}, channel: {1}, payload: {2}] Publish failed for connection {3}.", (clientId == null ? "null" : clientId),
						channelName, Util.displayPayload(payload), client));
			}
		}
		return result;
	}
}
