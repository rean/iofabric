package com.iotracks.iofabric.message_bus;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MessageIdGenerator {
	private final char[] ALPHABETS_ARRAY = "123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
	private String toBase58(long number) {
		StringBuilder result = new StringBuilder();
		while (number >= 58) {
			result.append(ALPHABETS_ARRAY[(int) (number % 58)]);
			number /= 58;
		}
		result.append(ALPHABETS_ARRAY[(int) number]);
		return result.reverse().toString();
	}

	
	// timestamp-sequence
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

	
	// uuid
	private final int PRE_GENERATED_IDS_COUNT = 100000;
	Queue<UUID> generatedIds = new LinkedList<>();
	private final Runnable refill = () -> {
		while (generatedIds.size() < PRE_GENERATED_IDS_COUNT)
			generatedIds.offer(UUID.randomUUID());
	};
	
	public synchronized String getNextId() {
		while (generatedIds.size() == 0);
		return generatedIds.poll().toString();
	}
	
	public MessageIdGenerator() {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(refill, 0, 1, TimeUnit.SECONDS);
	}
	
//			 			 1         2         3         4         5         6         7         8         9         0         1         2         3         4         5         6         7         
//  			12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789
//  Double:		uWJ7hf5NAL7ufAjUbfwAfAwuwQfLNN9y9wyUQy5syYE3W9qJsWC3bEQu7WYUWJyUYNu3EbSfbCLWwdNsGsEYuqY35h79YoSqYh7bfYWGGNmWqYyWoummsdwodoqLyjGSwyfWhu3hb1Q1J9wWhdUbJufo9AACYJyuYG3E5mmTre6jpcs
//	Float:		319jQQdmU33UhsMFqAEuBx
//  Long:		ddQ8PjM6Lpn
//	Integer:	85qLg4
//	Elements:	rv8H3m2fdzKJrKNVGftmWFYDbvgb3tpv

}
