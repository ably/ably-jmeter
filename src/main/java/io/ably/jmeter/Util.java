package io.ably.jmeter;

import org.apache.jmeter.config.Arguments;

import javax.xml.bind.DatatypeConverter;
import java.security.SecureRandom;
import java.util.*;

public class Util implements Constants {

	private static SecureRandom random = new SecureRandom();
    private static char[] seeds = "abcdefghijklmnopqrstuvwxmy0123456789".toCharArray();

	public static String generateRandomSuffix(String prefix) {
		int leng = prefix.length();
		int postLeng = MAX_RANDOM_ID_LENGTH - leng - 1;
		if(postLeng < 0) {
			throw new IllegalArgumentException("ClientId prefix " + prefix + " is too long, max allowed is "
					+ MAX_RANDOM_ID_LENGTH + " but was " + leng);
		}
		UUID uuid = UUID.randomUUID();
		String string = uuid.toString().replace("-", "");
		String post = string.substring(0, postLeng);
		return prefix + "_" + post;
	}

	public static String generatePayload(int size) {
		StringBuilder res = new StringBuilder();
		for(int i = 0; i < size; i++) {
			res.append(seeds[random.nextInt(seeds.length - 1)]);
		}
		return res.toString();
	}

	public static byte[] hexToBinary(String hex) {
		return DatatypeConverter.parseHexBinary(hex);
	}

	public static byte[] payloadBytes(Object data) {
		if(data instanceof String) {
			return ((String)data).getBytes();
		}
		if(data instanceof byte[]) {
			return (byte[])data;
		}
		return new byte[0];
	}

	public static String displayPayload(Object data) {
		if(data instanceof String) {
			return (String)data;
		}
		if(data instanceof byte[]) {
			return DatatypeConverter.printHexBinary((byte[])data);
		}
		return "Unexpected payload";
	}

	public static int payloadLength(Object data) {
		if(data instanceof String) {
			return ((String)data).getBytes().length;
		}
		if(data instanceof byte[]) {
			return ((byte[])data).length;
		}
		return 0;
	}

	public static Arguments mapToArguments(Map<String, String> map) {
		Arguments args = new Arguments();
		for(Map.Entry<String, String> e : map.entrySet()) {
			args.addArgument(e.getKey(), e.getValue());
		}
		return args;
	}

	public static Map<String, String> argumentsToMap(Arguments args) {
		return args.getArgumentsAsMap();
	}

	public static String mapToString(Map<String, String> map) {
		StringBuilder bldr = new StringBuilder();
		for(Map.Entry<String, String> e : map.entrySet()) {
			bldr.append(e.getKey()).append('=').append(e.getValue()).append('\n');
		}
		return bldr.toString();
	}

	public static Map<String, String> stringToMap(String str) {
		Map<String, String> map = new HashMap<>();
		for(String s : str.split("\n")) {
			String[] parts = s.split("=");
			if(parts.length == 2) {
				map.put(parts[0], parts[1]);
			}
		}
		return map;
	}
}
