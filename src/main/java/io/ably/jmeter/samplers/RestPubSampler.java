package io.ably.jmeter.samplers;

import io.ably.lib.rest.AblyRest;
import io.ably.lib.types.AblyException;
import io.ably.lib.types.Message;
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
public class RestPubSampler extends BaseSampler {
	private static final long serialVersionUID = 1859006013465470528L;
	private static final Logger logger = LoggerFactory.getLogger(RestPubSampler.class.getCanonicalName());

	public RestPubSampler() {
		super(logger);
	}

	@Override
	public SampleResult sample(Entry entry) {
		logger.debug("sample");
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		AblyRest client = (AblyRest) vars.getObject(BaseSampler.REST_CLIENT);
		if(client == null) {
			return fillFailedResult(result, "Client not found", 500);
		}

		try {
			Object payload = getPayload();
			String channelName = getChannelName();
			Message msg = getMessage(payload);

			result.sampleStart();
			client.channels.get(channelName).publish(new Message[]{msg});
			return fillOKResult(result);
		} catch (AblyException e) {
			logger.error("Failed to publish " + client , e);
			return fillFailedResult(result, e.errorInfo);
		}
	}
}
