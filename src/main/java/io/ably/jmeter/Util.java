package io.ably.jmeter;

import javax.xml.bind.DatatypeConverter;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Util implements Constants {

	private static SecureRandom random = new SecureRandom();
    private static char[] seeds = "abcdefghijklmnopqrstuvwxmy0123456789".toCharArray();

	public static String generateClientId(String prefix) {
		int leng = prefix.length();
		int postLeng = MAX_CLIENT_ID_LENGTH - leng;
		if (postLeng < 0) {
			throw new IllegalArgumentException("ClientId prefix " + prefix + " is too long, max allowed is "
					+ MAX_CLIENT_ID_LENGTH + " but was " + leng);
		}
		UUID uuid = UUID.randomUUID();
		String string = uuid.toString().replace("-", "");
		String post = string.substring(0, postLeng);
		return prefix + post;
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
}
