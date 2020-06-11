package io.ably.jmeter.samplers;

import io.ably.jmeter.Constants;
import org.apache.jmeter.samplers.AbstractSampler;

/**
 * A base model for properties used by multiple samplers
 */
public abstract class AbstractAblySampler extends AbstractSampler implements Constants {
	private static final long serialVersionUID = 7163793218595455807L;

	public String getEnvironment() {
		return getPropertyAsString(ENVIRONMENT, DEFAULT_ENVIRONMENT);
	}
	public void setEnvironment(String env) {
		setProperty(ENVIRONMENT, env);
	}

	public String getApiKey() {
		return getPropertyAsString(API_KEY, "");
	}
	public void setApiKey(String key) {
		setProperty(API_KEY, key);
	}

	public String getClientIdPrefix() {
		return getPropertyAsString(CLIENT_ID_PREFIX, DEFAULT_CLIENTID);
	}
	public void setClientIdPrefix(String connPrefix) {
		setProperty(CLIENT_ID_PREFIX, connPrefix);
	}

	public boolean isClientIdSuffix() {
		return getPropertyAsBoolean(CLIENT_ID_SUFFIX, DEFAULT_ADD_CLIENT_ID_SUFFIX);
	}
	public void setClientIdSuffix(boolean clientIdSuffix) {
		setProperty(CLIENT_ID_SUFFIX, clientIdSuffix);
	}

	public String getTopic() {
		return getPropertyAsString(CHANNEL_NAME, DEFAULT_CHANNEL_NAME);
	}
	public void setTopic(String topicName) {
		setProperty(CHANNEL_NAME, topicName);
	}

	public String getMessageType() {
		return getPropertyAsString(MESSAGE_TYPE, MESSAGE_TYPE_RANDOM_STR_WITH_FIX_LEN);
	}
	public void setMessageType(String messageType) {
		setProperty(MESSAGE_TYPE, messageType);
	}

	public String getMessageLength() {
		return getPropertyAsString(MESSAGE_FIX_LENGTH, DEFAULT_MESSAGE_FIX_LENGTH);
	}
	public void setMessageLength(String length) {
		setProperty(MESSAGE_FIX_LENGTH, length);
	}

	public String getMessage() {
		return getPropertyAsString(MESSAGE_TO_BE_SENT, "");
	}
	public void setMessage(String message) {
		setProperty(MESSAGE_TO_BE_SENT, message);
	}
}
