package com.iotracks.iofabric.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * provides methods for "number <=> byte array" conversions
 * 
 * @author saeid
 *
 */
public class BytesUtil {
	
	public static byte[] copyOfRange(byte[] src, int from, int to) {
		if (from < 0 || from >= src.length || to < from || to > src.length)
			return new byte[] {};
		byte[] tmp = new byte[from];
		byte[] result = new byte[to - from];
		ByteArrayInputStream input = new ByteArrayInputStream(src);
		input.read(tmp, 0, tmp.length);
		input.read(result, 0, result.length);
		try {
			input.close();
		} catch (IOException e) {}
		return result;
	}
	
	public static byte[] longToBytes(long x) {
		byte[] b = new byte[8];
		for (int i = 0; i < 8; ++i) {
			b[i] = (byte) (x >> (8 - i - 1 << 3));
		}
		return b;
	}

	public static long bytesToLong(byte[] bytes) {
		long result = 0;
		for (int i = 0; i < bytes.length; i++) {
			result = (result << 8) + (bytes[i] & 0xff);
		}
		return result;
	}

	public static byte[] integerToBytes(int x) {
		byte[] b = new byte[4];
		for (int i = 0; i < 4; ++i) {
			b[i] = (byte) (x >> (4 - i - 1 << 3));
		}
		return b;
	}

	public static int bytesToInteger(byte[] bytes) {
		int result = 0;
		for (int i = 0; i < bytes.length; i++) {
			result = (result << 8) + (bytes[i] & 0xff);
		}
		return result;
	}

	public static byte[] shortToBytes(short x) {
		byte[] b = new byte[2];
		for (int i = 0; i < 2; ++i) {
			b[i] = (byte) (x >> (2 - i - 1 << 3));
		}
		return b;
	}

	public static short bytesToShort(byte[] bytes) {
		short result = 0;
		for (int i = 0; i < bytes.length; i++) {
			result = (short) ((result << 8) + (bytes[i] & 0xff));
		}
		return result;
	}

	public static byte[] stringToBytes(String s) {
		if (s == null)
			return new byte[] {};
		else
			return s.getBytes();
	}

	public static String bytesToString(byte[] bytes) {
		return new String(bytes);
	}

	/**
	 * returns string presentation of byte array
	 * byte[] a = {1, 2, 3, 4} => String a = "[1, 2, 3, 4]"
	 * 
	 * @param bytes
	 * @return string
	 */
	public static String byteArrayToString(byte[] bytes) {
		StringBuilder result = new StringBuilder();

		result.append("[");
		for (byte b : bytes) {
			if (result.length() > 1)
				result.append(", ");
			result.append(b);
		}
		result.append("]");

		return result.toString();
	}
}
