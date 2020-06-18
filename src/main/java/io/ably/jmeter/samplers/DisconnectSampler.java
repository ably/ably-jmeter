package io.ably.jmeter.samplers;

import java.text.MessageFormat;

import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.realtime.ConnectionState;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A sampler that disconnects a previously established Ably realtime connection
 */
public class DisconnectSampler extends BaseSampler {
	private static final long serialVersionUID = 4360869021667126983L;
	private static final Logger logger = LoggerFactory.getLogger(DisconnectSampler.class.getCanonicalName());

	private transient AblyRealtime client = null;

	public DisconnectSampler() {
		super(logger);
	}

	@Override
	public SampleResult sample(Entry entry) {
		logger.debug("sample");
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		client = (AblyRealtime) vars.getObject(BaseSampler.REALTIME_CLIENT);
		if(client == null) {
			return fillFailedResult(result, "Connection not found", 500);
		}

		String clientId = client.options.clientId;
		try {
			logger.info(MessageFormat.format("Disconnect connection {0}.", client));
			result.sampleStart();
			closeClient();
			vars.remove(BaseSampler.REALTIME_CLIENT); // clean up thread local var as well
			return fillOKResult(result);
		} catch (Exception e) {
			logger.error("Failed to disconnect client", e);
			return fillFailedResult(result, "Failed to disconnect client" + e.getMessage(), 500);
		}
	}

	private void closeClient() {
		if(client != null) {
			try {
				if(client.connection.state != ConnectionState.closed) {
					logger.info("closeClient: client is not closed; closing now");
					client.close();
				}
			} catch(Exception e) {
				logger.error("closeClient: exception closing client", e);
			}
		}
	}
}
