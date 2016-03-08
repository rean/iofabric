package com.iotracks.iofabric.message_bus;

public class MessageIdGenerator {
	private final char ALPHABET[] = "123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
	private final static int MAX_SEQUENCE = 100000000;
	private static volatile long lastTime = 0;
	private static volatile int sequence = MAX_SEQUENCE;
	
	public synchronized String generate(long time) {
		if (lastTime == time) {
			sequence--;
		} else {
			lastTime = time;
			sequence = MAX_SEQUENCE;
		}
		return toBase58(time, 11) + toBase58(sequence, 5);
	}

	
	private String toBase58(long number, int len) {
		StringBuilder result = new StringBuilder();
		while (number >= 58) {
			result.append(ALPHABET[(int) (number % 58)]);
			number /= 58;
		}
		result.append(ALPHABET[(int) number]);
		for (int i = result.length(); i < len; i++)
			result.append(ALPHABET[0]);
		return result.reverse().toString();
	}
//			 			 1         2         3         4         5         6         7         8         9         0         1         2         3         4         5         6         7         
//  			12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789
//  Double:		uWJ7hf5NAL7ufAjUbfwAfAwuwQfLNN9y9wyUQy5syYE3W9qJsWC3bEQu7WYUWJyUYNu3EbSfbCLWwdNsGsEYuqY35h79YoSqYh7bfYWGGNmWqYyWoummsdwodoqLyjGSwyfWhu3hb1Q1J9wWhdUbJufo9AACYJyuYG3E5mmTre6jpcs
//	Float:		319jQQdmU33UhsMFqAEuBx
//  Long:		ddQ8PjM6Lpn
//	Integer:	85qLg4
//	Elements:	rv8H3m2fdzKJrKNVGftmWFYDbvgb3tpv

}
