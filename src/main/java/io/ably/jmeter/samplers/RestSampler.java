package io.ably.jmeter.samplers;

import io.ably.lib.rest.AblyRest;
import io.ably.lib.types.ClientOptions;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * A sampler that publishes a single message to a given channel using the Ably REST client
 */
public class RestSampler extends BaseSampler {
	private static final long serialVersionUID = 1859006013465470528L;
	private static final Logger logger = LoggerFactory.getLogger(RestSampler.class.getCanonicalName());

	@Override
	public SampleResult sample(Entry entry) {
		logger.debug("sample");
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		AblyRest client = (AblyRest) vars.getObject(BaseSampler.REST_CLIENT);
		if(client != null) {
			result.sampleStart();
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Client {0} already exists.", client));
			result.setResponseData("Failed. Client already exists.".getBytes());
			result.setResponseCode("500");
			result.sampleEnd(); // avoid endtime=0 exposed in trace log
			return result;
		}

		ClientOptions opts = getClientOptions(logger);
		result.sampleStart();
		try {
			client = new AblyRest(opts);
			vars.putObject(BaseSampler.REST_CLIENT, client); // save connection object as thread local variable !!
			result.setSuccessful(true);
			result.setResponseData("Successful.".getBytes());
			result.setResponseCodeOK();
		} catch (Exception e) {
			logger.error("Failed to establish client " + client, e);
			result.setSuccessful(false);
			result.setResponseData("Failed to establish client. Please check configuration.".getBytes());
			result.setResponseCode("502");
		} finally {
			result.sampleEnd();
			return result;
		}
	}
}
