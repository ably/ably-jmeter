package io.ably.jmeter.samplers;

import io.ably.lib.http.HttpHelpers;
import io.ably.lib.http.HttpUtils;
import io.ably.lib.rest.AblyRest;
import io.ably.lib.types.AblyException;
import io.ably.lib.util.Serialisation;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Properties;

/**
 * A sampler that publishes a single message to a given channel using the Ably REST client
 */
public class SetupAppSampler extends SetupSampler {
	private static final long serialVersionUID = 1859006013465470528L;
	private static final Logger logger = LoggerFactory.getLogger(SetupAppSampler.class.getCanonicalName());

	public SetupAppSampler() {
		super(logger);
	}

	@Override
	public SampleResult sample(Entry entry) {
		logger.debug("sample");
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		/* TODO: populate these defaults from the UI */
		Key rootKey = new Key();
		AppSpec inputSpec = new AppSpec();
		inputSpec.keys = new Key[]{rootKey};
		inputSpec.namespaces = new Namespace[]{
				new Namespace() {{ id = "push"; pushEnabled = true; }},
				new Namespace() {{ id = "persisted"; persisted = true; }}
		};
		inputSpec.notes = "Test app; created by jmeter tests; date = " + new Date().toString();

		AblyRest ably = getAbly(getEnvironment(), null);
		AppSpec resultSpec;
		try {
			resultSpec = HttpHelpers.postSync(ably.http, "/apps", null, null, new HttpUtils.JsonRequestBody(inputSpec), (response, error) -> {
				if(error != null) {
					throw AblyException.fromErrorInfo(error);
				}

				return Serialisation.gson.fromJson(new String(response.body), AppSpec.class);
			}, false);
		} catch (AblyException e) {
			logger.error("Unable to create test app", e);
			return fillFailedResult(result, e.errorInfo);
		}

		Properties properties = JMeterContextService.getContext().getProperties();
		properties.setProperty(BaseSampler.APP_ID, resultSpec.appId);
		properties.setProperty(BaseSampler.ACCOUNT_ID, resultSpec.accountId);
		properties.setProperty(BaseSampler.API_KEY, resultSpec.keys[0].keyStr);

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		vars.putObject(BaseSampler.APP_SPEC, resultSpec);
		return fillOKResult(result);
	}

}
