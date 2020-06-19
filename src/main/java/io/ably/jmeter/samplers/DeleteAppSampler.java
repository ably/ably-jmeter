package io.ably.jmeter.samplers;

import io.ably.lib.http.Http;
import io.ably.lib.http.HttpUtils;
import io.ably.lib.rest.AblyRest;
import io.ably.lib.types.AblyException;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A sampler that publishes a single message to a given channel using the Ably REST client
 */
public class DeleteAppSampler extends SetupSampler {
	private static final long serialVersionUID = 1859006013465470528L;
	private static final Logger logger = LoggerFactory.getLogger(DeleteAppSampler.class.getCanonicalName());

	public DeleteAppSampler() {
		super(logger);
	}

	@Override
	public SampleResult sample(Entry entry) {
		logger.debug("sample");
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		AppSpec appSpec = (AppSpec) vars.getObject(BaseSampler.APP_SPEC);
		if (appSpec == null) {
			logger.error("App not found");
			return fillFailedResult(result, "App not found", 500);
		}

		String environment = vars.get(ENVIRONMENT);
		if (environment == null) {
			logger.error("Environment not found");
			return fillFailedResult(result, "Environment not found", 500);
		}

		AblyRest ably = getAbly(environment, appSpec.keys[0].keyStr);
		if (ably == null) {
			logger.error("Unable to instance Ably library");
			return fillFailedResult(result, "Unable to instance Ably library", 500);
		}

		try {
			ably.http.request((Http.Execute<Void>) (http, callback) -> http.del("/apps/" + appSpec.appId, HttpUtils.defaultAcceptHeaders(false), null, null, true, callback)).sync();
			return fillOKResult(result);
		} catch (AblyException e) {
			logger.error("Unable to create test app", e);
			return fillFailedResult(result, e.errorInfo);
		}
	}
}
