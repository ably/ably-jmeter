package io.ably.jmeter.samplers;

import com.launchdarkly.eventsource.EventSource;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * A sampler that disconnects a previously established Ably realtime connection
 */
public class SSEDisconnectSampler extends BaseSampler {
	private static final long serialVersionUID = 4360869021667126983L;
	private static final Logger logger = LoggerFactory.getLogger(SSEDisconnectSampler.class.getCanonicalName());

	@Override
	public SampleResult sample(Entry entry) {
		logger.debug("sample");
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		EventSource client = (EventSource) vars.getObject(BaseSampler.SSE_CLIENT);
		if(client == null) {
			result.sampleStart();
			result.setSuccessful(false);
			result.setResponseMessage("Connection not found.");
			result.setResponseData("Failed. Connection not found.".getBytes());
			result.setResponseCode("500");
			result.sampleEnd(); // avoid endtime=0 exposed in trace log
			return result;
		}

		SSEConnectSampler.SSESubscriptionHandler handler = (SSEConnectSampler.SSESubscriptionHandler)vars.getObject(BaseSampler.SSE_CLIENT_HANDLER);
		String clientId = handler.clientId;
		try {
			logger.info(MessageFormat.format("Disconnect connection {0}.", client));
			result.sampleStart();
			closeSSEClient(logger, client);
			result.sampleEnd();
			vars.remove(BaseSampler.SSE_CLIENT);
			vars.remove(BaseSampler.SSE_CLIENT_HANDLER);

			result.setSuccessful(true);
			result.setResponseData("Successful.".getBytes());
			result.setResponseMessage(MessageFormat.format("Connection {0} disconnected.", client));
			result.setResponseCodeOK();
		} catch (Exception e) {
			logger.error("Failed to disconnect client" + client, e);
			if(result.getEndTime() == 0) result.sampleEnd(); //avoid re-enter sampleEnd()
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Failed to disconnect client {0}.", client));
			result.setResponseData(MessageFormat.format("Client [{0}] failed. Couldn't disconnect client.", (clientId == null ? "null" : clientId)).getBytes());
			result.setResponseCode("501");
		}
		return result;
	}
}
