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

	public SSEDisconnectSampler() {
		super(logger);
	}

	@Override
	public SampleResult sample(Entry entry) {
		logger.debug("sample");
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		EventSource client = (EventSource) vars.getObject(BaseSampler.SSE_CLIENT);
		if(client == null) {
			return fillFailedResult(result, "Connection not found", 500);
		}

		SSEConnectSampler.SSESubscriptionHandler handler = (SSEConnectSampler.SSESubscriptionHandler)vars.getObject(BaseSampler.SSE_CLIENT_HANDLER);
		try {
			logger.info(MessageFormat.format("Disconnect connection {0}.", client));
			result.sampleStart();
			closeSSEClient(logger, client);
			vars.remove(BaseSampler.SSE_CLIENT);
			vars.remove(BaseSampler.SSE_CLIENT_HANDLER);
			return fillOKResult(result);
		} catch (Exception e) {
			logger.error("Failed to disconnect client", e);
			return fillFailedResult(result, "Failed to disconnect client" + e.getMessage(), 500);
		}
	}
}
