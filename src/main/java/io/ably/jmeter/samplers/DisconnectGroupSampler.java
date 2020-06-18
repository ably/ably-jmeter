package io.ably.jmeter.samplers;

import io.ably.lib.realtime.AblyRealtime;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A sampler that disconnects a previously established Ably realtime connection
 */
public class DisconnectGroupSampler extends BaseSampler {
	private static final long serialVersionUID = 4360869021667126983L;
	private static final Logger logger = LoggerFactory.getLogger(DisconnectGroupSampler.class.getCanonicalName());

	private transient AblyRealtime[] clients = null;

	public DisconnectGroupSampler() {
		super(logger);
	}

	@Override
	public SampleResult sample(Entry entry) {
		logger.debug("sample");
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		clients = (AblyRealtime[]) vars.getObject(BaseSampler.REALTIME_CLIENT_GROUP);
		if(clients == null) {
			return fillFailedResult(result, "Connection not found", 500);
		}

		try {
			logger.info("Disconnect connection group.");

			result.sampleStart();
			closeAllClients(logger, clients);
			vars.remove(BaseSampler.REALTIME_CLIENT_GROUP); // clean up thread local var as well
			return fillOKResult(result);
		} catch (Exception e) {
			logger.error("Failed to disconnect client", e);
			return fillFailedResult(result, "Failed to disconnect client" + e.getMessage(), 500);
		}
	}
}
