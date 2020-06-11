package io.ably.jmeter;

public interface Constants {
	String ENVIRONMENT = "ably.environment";
	String CLIENT = "ably.client";
	String API_KEY = "ably.api_key";
	String CLIENT_ID_PREFIX = "ably.client_id_prefix";
	String CLIENT_ID_SUFFIX = "ably.client_id_suffix";
	String MESSAGE_TYPE = "ably.message_type";
	String MESSAGE_FIX_LENGTH = "ably.message_type_fixed_length";
	String MESSAGE_TO_BE_SENT = "ably.message_to_sent";
	String CHANNEL_NAME = "ably.channel_name";
	String SAMPLE_CONDITION_VALUE = "ably.sample_condition_value";
	String SAMPLE_CONDITION = "ably.sample_condition";
	String DEBUG_RESPONSE = "ably.debug_response";
	String MESSAGE_TYPE_RANDOM_STR_WITH_FIX_LEN = "Random string with fixed length";
	String MESSAGE_TYPE_HEX_STRING = "Hex string";
	String MESSAGE_TYPE_STRING = "String";
	String SAMPLE_ON_CONDITION_OPTION1 = "specified elapsed time (ms)";
	String SAMPLE_ON_CONDITION_OPTION2 = "number of received messages";
	int MAX_CLIENT_ID_LENGTH = 32;
	String DEFAULT_ENVIRONMENT = "";
	String DEFAULT_CHANNEL_NAME = "test_channel";
	String DEFAULT_CLIENTID = "client_";
	String DEFAULT_SAMPLE_VALUE_COUNT = "1";
	String DEFAULT_SAMPLE_VALUE_ELAPSED_TIME_MILLI_SEC = "1000";
	String DEFAULT_MESSAGE_FIX_LENGTH = "1024";
	boolean DEFAULT_ADD_CLIENT_ID_SUFFIX = true;
	int SUB_FAIL_PENALTY = 1000; // force to delay 1s if sub fails for whatever reason
}
