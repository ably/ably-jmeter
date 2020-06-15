package io.ably.jmeter.samplers;

import com.google.gson.JsonPrimitive;
import io.ably.jmeter.AblyLog;
import io.ably.jmeter.Constants;
import io.ably.jmeter.Util;
import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.realtime.ConnectionState;
import io.ably.lib.realtime.ConnectionStateListener;
import io.ably.lib.types.ClientOptions;
import io.ably.lib.types.ErrorInfo;
import io.ably.lib.types.Message;
import io.ably.lib.util.JsonUtils;
import io.ably.lib.util.Log;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.testelement.ThreadListener;
import org.slf4j.Logger;

import java.util.Map;

/**
 * A base model for properties used by multiple samplers
 */
public abstract class AbstractAblySampler extends AbstractSampler implements Constants, ThreadListener {
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

	public String getClientId() {
		String clientId = getClientIdPrefix();
		if(isClientIdSuffix()) {
			clientId = Util.generateRandomSuffix(clientId);
		}
		return clientId;
	}

	public String getChannelPrefix() {
		return getPropertyAsString(Constants.CHANNEL_NAME_PREFIX, Constants.DEFAULT_CHANNEL_NAME_PREFIX);
	}
	public void setChannelPrefix(String channelName) {
		setProperty(Constants.CHANNEL_NAME_PREFIX, channelName);
	}

	public boolean isChannelNameSuffix() {
		return getPropertyAsBoolean(CHANNEL_NAME_SUFFIX, DEFAULT_ADD_CHANNEL_NAME_SUFFIX);
	}
	public void setChannelNameSuffix(boolean channelNameSuffix) {
		setProperty(CHANNEL_NAME_SUFFIX, channelNameSuffix);
	}

	public String getChannelName() {
		String channelName = getChannelPrefix();
		if(isChannelNameSuffix()) {
			channelName = Util.generateRandomSuffix(channelName);
		}
		return channelName;
	}

	public Map<String, String> getChannelParams() {
		String encoded = getPropertyAsString(Constants.CHANNEL_PARAMS);
		return Util.stringToMap(encoded);
	}
	public void setChannelParams(Map<String, String> params) {
		setProperty(Constants.CHANNEL_PARAMS, Util.mapToString(params));
	}

	public boolean isAddTimestamp() {
		return getPropertyAsBoolean(ADD_TIMESTAMP);
	}
	public void setAddTimestamp(boolean addTimestamp) {
		setProperty(ADD_TIMESTAMP, addTimestamp);
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

	public String getMessageEventName() {
		return getPropertyAsString(MESSAGE_EVENT_NAME, DEFAULT_EVENT_NAME);
	}
	public void setMessageEventName(String name) {
		setProperty(MESSAGE_EVENT_NAME, name);
	}

	public String getMessageEncoding() {
		return getPropertyAsString(MESSAGE_ENCODING, DEFAULT_ENCODING);
	}
	public void setMessageEncoding(String name) {
		setProperty(MESSAGE_ENCODING, name);
	}

	public String getStatsStart() {
		return getPropertyAsString(STATS_START, DEFAULT_STATS_START);
	}
	public void setStatsStart(String val) {
		setProperty(STATS_START, val);
	}

	public String getStatsEnd() {
		return getPropertyAsString(STATS_END, DEFAULT_STATS_END);
	}
	public void setStatsEnd(String val) {
		setProperty(STATS_END, val);
	}

	public String getStatsLimit() {
		return getPropertyAsString(STATS_LIMIT, DEFAULT_STATS_LIMIT);
	}
	public void setStatsLimit(String val) {
		setProperty(STATS_LIMIT, val);
	}

	public String getHistoryStart() {
		return getPropertyAsString(HISTORY_START, DEFAULT_HISTORY_START);
	}
	public void setHistoryStart(String val) {
		setProperty(HISTORY_START, val);
	}

	public String getHistoryEnd() {
		return getPropertyAsString(HISTORY_END, DEFAULT_HISTORY_END);
	}
	public void setHistoryEnd(String val) {
		setProperty(HISTORY_END, val);
	}

	public String getHistoryLimit() {
		return getPropertyAsString(HISTORY_LIMIT, DEFAULT_HISTORY_LIMIT);
	}
	public void setHistoryLimit(String val) {
		setProperty(HISTORY_LIMIT, val);
	}

	public String[] getUnitValues() {
		return UNITS.split(",");
	}
	public int getStatsUnitIndex() {
		return getPropertyAsInt(STATS_UNIT, DEFAULT_UNIT);
	}
	public void setStatsUnitIndex(int idx) {
		setProperty(STATS_UNIT, idx);
	}

	public String[] getDirectionValues() {
		return DIRECTIONS.split(",");
	}
	public int getStatsDirectionIndex() {
		return getPropertyAsInt(STATS_DIRECTION, DEFAULT_STATS_DIRECTION);
	}
	public void setStatsDirectionIndex(int idx) {
		setProperty(STATS_DIRECTION, idx);
	}

	public int getHistoryDirectionIndex() {
		return getPropertyAsInt(HISTORY_DIRECTION, DEFAULT_HISTORY_DIRECTION);
	}
	public void setHistoryDirectionIndex(int idx) {
		setProperty(HISTORY_DIRECTION, idx);
	}

	public int getLogLevelIndex() {
		return getPropertyAsInt(LOG_LEVEL, DEFAULT_LOG_LEVEL);
	}
	public void setLogLevelIndex(int idx) {
		setProperty(LOG_LEVEL, idx);
	}

	public int getGroupSize() {
		return getPropertyAsInt(GROUP_SIZE, DEFAULT_GROUP_SIZE);
	}
	public void setGroupSize(int size) {
		setProperty(GROUP_SIZE, size);
	}

	protected ClientOptions getClientOptions(Logger logger) {
		ClientOptions opts = new ClientOptions();
		String clientId = getClientId();
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
		return opts;
	}

	protected ClientOptions getRealtimeClientOptions(Logger logger) {
		ClientOptions opts = getClientOptions(logger);
		opts.autoConnect = false;
		return opts;
	}

	protected void closeAllClients(Logger logger, AblyRealtime[] clients) {
		if(clients != null) {
			for(AblyRealtime client : clients) {
				closeClient(logger, client);
			}
		}
	}

	protected void closeClient(Logger logger, AblyRealtime client) {
		if(client != null) {
			try {
				if(client.connection.state != ConnectionState.closed) {
					logger.info("closeClient: client is not closed; closing now");
					client.close();
				}
			} catch(Exception e) {
				logger.error("closeClient: exception closing client", e);
			}
		}
	}

	protected Object getPayload() {
		Object payload = null;
		if (MESSAGE_TYPE_HEX_STRING.equals(getMessageType())) {
			payload = Util.hexToBinary(getMessage());
		} else if (MESSAGE_TYPE_STRING.equals(getMessageType())) {
			payload = getMessage();
		} else if(MESSAGE_TYPE_RANDOM_STR_WITH_FIX_LEN.equals(getMessageType())) {
			payload = Util.generatePayload(Integer.parseInt(getMessageLength()));
		}
		return payload;
	}

	protected Message getMessage(Object payload) {
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
		return msg;
	}

	@Override
	public void threadStarted() {}

	@Override
	public void threadFinished() {}

	protected static class ConnectResult implements ConnectionStateListener {
		private ConnectionStateChange state;

		private synchronized void setState(ConnectionStateChange state) {
			this.state = state;
			notify();
		}

		synchronized ErrorInfo waitForResult() throws InterruptedException {
			while(state == null) {
				wait();
			}
			return state.reason;
		}

		@Override
		public void onConnectionStateChanged(ConnectionStateChange state) {
			switch(state.current) {
				case connected:
				case failed:
				case suspended:
					setState(state);
					break;
				default:
					/* ignore */
			}
		}
	}
}
