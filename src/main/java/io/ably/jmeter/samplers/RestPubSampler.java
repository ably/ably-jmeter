package io.ably.jmeter.samplers;

import com.google.gson.JsonPrimitive;
import io.ably.jmeter.Util;
import io.ably.lib.rest.AblyRest;
import io.ably.lib.types.AblyException;
import io.ably.lib.types.ClientOptions;
import io.ably.lib.types.Message;
import io.ably.lib.util.JsonUtils;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A sampler that publishes a single message to a given channel using the Ably REST client
 */
public class RestPubSampler extends AbstractAblySampler {
	private static final long serialVersionUID = 1859006013465470528L;
	private static final Logger logger = Logger.getLogger(RestPubSampler.class.getCanonicalName());

	@Override
	public SampleResult sample(Entry entry) {
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());

		logger.log(Level.FINE, "sample");
		ClientOptions opts = new ClientOptions();
		String clientId = getClientIdPrefix();
		AblyRest client;
		try {
			if(isClientIdSuffix()) {
				clientId = Util.generateClientId(clientId);
			}

			String env = getEnvironment();
			if(env != null && !env.equals("")) {
				opts.environment = env;
			}
			opts.key = getApiKey();
			opts.clientId = clientId;
			client = new AblyRest(opts);
		} catch (AblyException e) {
			logger.log(Level.SEVERE, "Failed to init client " + clientId , e);
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Failed to init client {0}. Please check connection configuration.", clientId));
			result.setResponseData("Failed to init client. Please check connection configuration.".getBytes());
			result.setResponseCode(String.valueOf(e.errorInfo.statusCode));
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

			String channelName = getChannel();
			Message msg = new Message("test event", payload);
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
			logger.log(Level.SEVERE, "Failed to publish " + client , e);
			if (result.getEndTime() == 0) { result.sampleEnd(); } //avoid re-enter sampleEnd()
			result.setSuccessful(false);
			result.setResponseMessage(MessageFormat.format("Failed to publish {0}.", client));
			result.setResponseData(MessageFormat.format("Publish [{0}] failed with exception.", clientId).getBytes());
			result.setResponseCode(String.valueOf(e.errorInfo.statusCode));
		}
		return result;
	}
}
