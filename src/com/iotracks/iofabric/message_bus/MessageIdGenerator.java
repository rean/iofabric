package com.iotracks.iofabric.message_bus;

public class MessageIdGenerator {
	private final char[] ALPHABETS_ARRAY = "123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
	private final int MAX_SEQUENCE = 100000000;
	
	private volatile long lastTime = 0;
	private volatile int sequence = MAX_SEQUENCE;
	public synchronized String generate(long time) {
		if (lastTime == time) {
			sequence--;
		} else {
			lastTime = time;
			sequence = MAX_SEQUENCE;
		}
		return toBase58(time) + toBase58(sequence);
	}

	private volatile long lower = 0; 
	private volatile int upper = 0; 
	
	public synchronized String generate() {
		if (lower == Long.MAX_VALUE) {
			lower = 0;
			upper++;
		}
		lower++;
		return toBase58(upper) + toBase58(lower);
	}

	private String toBase58(long number) {
		StringBuilder result = new StringBuilder();
		while (number >= 58) {
			result.append(ALPHABETS_ARRAY[(int) (number % 58)]);
			number /= 58;
		}
		result.append(ALPHABETS_ARRAY[(int) number]);
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
