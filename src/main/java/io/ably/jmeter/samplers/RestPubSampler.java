package io.ably.jmeter.samplers;

import com.google.gson.JsonPrimitive;
import io.ably.jmeter.Util;
import io.ably.lib.rest.AblyRest;
import io.ably.lib.types.AblyException;
import io.ably.lib.types.Message;
import io.ably.lib.util.JsonUtils;
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
public class RestPubSampler extends AbstractAblySampler {
	private static final long serialVersionUID = 1859006013465470528L;
	private static final Logger logger = LoggerFactory.getLogger(RestPubSampler.class.getCanonicalName());

	@Override
	public SampleResult sample(Entry entry) {
		logger.debug("sample");
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		JMeterVariables vars = JMeterContextService.getContext().getVariables();
		AblyRest client = (AblyRest) vars.getObject(AbstractAblySampler.REST_CLIENT);
		if (client == null) {
			result.sampleStart();
			result.setSuccessful(false);
			result.setResponseMessage("Publish: client configuration not found.");
			result.setResponseData("Publish failed because client configuration is not found.".getBytes());
			result.setResponseCode("500");
			result.sampleEnd(); // avoid endtime=0 exposed in trace log
			return result;
		}

		try {
			Object payload = null;
			if (MESSAGE_TYPE_HEX_STRING.equals(getMessageType())) {
				payload = Util.hexToBinary(getMessage());
			} else if (MESSAGE_TYPE_STRING.equals(getMessageType())) {
				payload = getMessage();
			} else if(MESSAGE_TYPE_RANDOM_STR_WITH_FIX_LEN.equals(getMessageType())) {
				payload = Util.generatePayload(Integer.parseInt(getMessageLength()));
			}

			String channelName = getChannelPrefix();
			if(isChannelNameSuffix()) {
				channelName = Util.generateRandomSuffix(channelName);
			}
			vars.putObject(AbstractAblySampler.CHANNEL_NAME, channelName);
			Message msg = new Message(getMessageEventName(), payload);
			String encoding = getMessageEncoding();
			if(encoding != null && !encoding.isEmpty()) {
				msg.encoding = encoding;
			}
			if(isAddTimestamp()) {
				msg.extras = JsonUtils.object()
						.add("metadata", JsonUtils.object()
								.add("timestamp", new JsonPrimitive(System.currentTimeMillis())))
						.toJson();
			}
			result.sampleStart();
			client.channels.get(channelName).publish(new Message[]{msg});
			result.sampleEnd();

			result.setSuccessful(true);
			result.setResponseData("Successful.".getBytes());
			result.setResponseMessage(MessageFormat.format("Connection {0} established.", client));
			result.setResponseCodeOK();
		} catch (AblyException e) {
			logger.error("Failed to publish " + client , e);
			if (result.getEndTime() == 0) { result.sampleEnd(); } //avoid re-enter sampleEnd()
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Failed to publish {0}.", client));
			result.setResponseData(MessageFormat.format("Publish [{0}] failed with exception.", client.options.clientId).getBytes());
			result.setResponseCode(String.valueOf(e.errorInfo.statusCode));
		}
		return result;
	}
}
