package io.ably.jmeter.samplers;

import io.ably.lib.rest.AblyRest;
import io.ably.lib.types.ClientOptions;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A sampler that publishes a single message to a given channel using the Ably REST client
 */
public class RestSampler extends BaseSampler {
	private static final long serialVersionUID = 1859006013465470528L;
	private static final Logger logger = LoggerFactory.getLogger(RestSampler.class.getCanonicalName());

	public RestSampler() {
		super(logger);
	}

	@Override
	public SampleResult sample(Entry entry) {
		logger.debug("sample");
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		AblyRest client = (AblyRest) vars.getObject(BaseSampler.REST_CLIENT);
		if(client != null) {
			return fillFailedResult(result, "Client already exists", 500);
		}

		ClientOptions opts = getClientOptions(logger);
		result.sampleStart();
		try {
			client = new AblyRest(opts);
			vars.putObject(BaseSampler.REST_CLIENT, client); // save connection object as thread local variable !!
			return fillOKResult(result);
		} catch (Exception e) {
			logger.error("Failed to disconnect client", e);
			return fillFailedResult(result, "Failed to disconnect client" + e.getMessage(), 500);
		}
	}
}
