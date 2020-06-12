package io.ably.jmeter.samplers;

import java.text.MessageFormat;

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
public class DisconnectSampler extends AbstractAblySampler {
	private static final long serialVersionUID = 4360869021667126983L;
	private static final Logger logger = LoggerFactory.getLogger(DisconnectSampler.class.getCanonicalName());

	private transient AblyRealtime client = null;

	@Override
	public SampleResult sample(Entry entry) {
		logger.debug("sample");
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		client = (AblyRealtime) vars.getObject(AbstractAblySampler.REALTIME_CLIENT);
		if (client == null) {
			result.sampleStart();
			result.setSuccessful(false);
			result.setResponseMessage("Connection not found.");
			result.setResponseData("Failed. Connection not found.".getBytes());
			result.setResponseCode("500");
			result.sampleEnd(); // avoid endtime=0 exposed in trace log
			return result;
		}

		String clientId = client.options.clientId;
		try {
			result.sampleStart();

			logger.info(MessageFormat.format("Disconnect connection {0}.", client));
			client.close();
			vars.remove(AbstractAblySampler.REALTIME_CLIENT); // clean up thread local var as well

			result.sampleEnd();

			result.setSuccessful(true);
			result.setResponseData("Successful.".getBytes());
			result.setResponseMessage(MessageFormat.format("Connection {0} disconnected.", client));
			result.setResponseCodeOK();
		} catch (Exception e) {
			logger.error("Failed to disconnect client" + client, e);
			if (result.getEndTime() == 0) result.sampleEnd(); //avoid re-enter sampleEnd()
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Failed to disconnect client {0}.", client));
			result.setResponseData(MessageFormat.format("Client [{0}] failed. Couldn't disconnect client.", (clientId == null ? "null" : clientId)).getBytes());
			result.setResponseCode("501");
		}
		return result;
	}
}
