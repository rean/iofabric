package com.iotracks.iofabric.message_bus;

import java.util.HashMap;
import java.util.Map;

public class MessageBusStatus {
	private long processedMessages;
	private Map<String, Long> publishedMessagesPerElement;
	private float averageSpeed;
	
	public MessageBusStatus() {
		publishedMessagesPerElement = new HashMap<>();
		processedMessages = 0;
		averageSpeed = 0;
	}
	
	public long getProcessedMessages() {
		return processedMessages;
	}

	public Long getPublishedMessagesPerElement(String element) {
		return publishedMessagesPerElement.get(element);
	}

	public MessageBusStatus increasePublishedMessagesPerElement(String element) {
		this.processedMessages++;

		Long n = this.publishedMessagesPerElement.get(element);
		if (n == null)
			n = 0l;
		this.publishedMessagesPerElement.put(element, n + 1);
		return this;
	}

	public float getAverageSpeed() {
		return averageSpeed;
	}

	public MessageBusStatus setAverageSpeed(float averageSpeed) {
		this.averageSpeed = averageSpeed;
		return this;
	}

}
