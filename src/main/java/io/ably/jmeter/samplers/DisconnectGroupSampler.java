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

	@Override
	public SampleResult sample(Entry entry) {
		logger.debug("sample");
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		clients = (AblyRealtime[]) vars.getObject(BaseSampler.REALTIME_CLIENT_GROUP);
		if(clients == null) {
			result.sampleStart();
			result.setSuccessful(false);
			result.setResponseMessage("Connection not found.");
			result.setResponseData("Failed. Connection not found.".getBytes());
			result.setResponseCode("500");
			result.sampleEnd(); // avoid endtime=0 exposed in trace log
			return result;
		}

		try {
			logger.info("Disconnect connection group.");

			result.sampleStart();
			closeAllClients(logger, clients);
			result.sampleEnd();
			vars.remove(BaseSampler.REALTIME_CLIENT_GROUP); // clean up thread local var as well

			result.setSuccessful(true);
			result.setResponseData("Successful.".getBytes());
			result.setResponseMessage("Connection disconnected.");
			result.setResponseCodeOK();
		} catch (Exception e) {
			logger.error("Failed to disconnect client", e);
			if(result.getEndTime() == 0) result.sampleEnd(); //avoid re-enter sampleEnd()
			result.setSuccessful(false);
			result.setResponseMessage("Failed to disconnect client.");
			result.setResponseData("Couldn't disconnect client.".getBytes());
			result.setResponseCode("501");
		}
		return result;
	}
}
