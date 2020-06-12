package io.ably.jmeter.samplers;

import io.ably.jmeter.AblyLog;
import io.ably.jmeter.Util;
import io.ably.lib.rest.AblyRest;
import io.ably.lib.types.ClientOptions;
import io.ably.lib.util.Log;
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
public class RestSampler extends AbstractAblySampler {
	private static final long serialVersionUID = 1859006013465470528L;
	private static final Logger logger = LoggerFactory.getLogger(RestSampler.class.getCanonicalName());

	@Override
	public SampleResult sample(Entry entry) {
		logger.debug("sample");
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		AblyRest client = (AblyRest) vars.getObject(AbstractAblySampler.REST_CLIENT);
		if (client != null) {
			result.sampleStart();
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Client {0} already exists.", client));
			result.setResponseData("Failed. Client already exists.".getBytes());
			result.setResponseCode("500");
			result.sampleEnd(); // avoid endtime=0 exposed in trace log
			return result;
		}

		ClientOptions opts = new ClientOptions();
		String clientId = getClientIdPrefix();
		result.sampleStart();
		try {
			if(isClientIdSuffix()) {
				clientId = Util.generateRandomSuffix(clientId);
			}

			String env = getEnvironment();
			if(env != null && !env.isEmpty()) {
				opts.environment = env;
			}
			opts.key = getApiKey();
			opts.clientId = clientId;
			opts.useTokenAuth = false;

			int logLevel = AblyLog.asAblyLevel(getLogLevelIndex());
			opts.logLevel = logLevel;
			if(logLevel != Log.NONE) {
				opts.logHandler = AblyLog.getAblyHandler(logger);
			}

			client = new AblyRest(opts);
			vars.putObject(AbstractAblySampler.REST_CLIENT, client); // save connection object as thread local variable !!
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
