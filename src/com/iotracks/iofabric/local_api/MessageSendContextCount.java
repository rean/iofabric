package com.iotracks.iofabric.local_api;

import com.iotracks.iofabric.message_bus.Message;

/**
 * Unacknowledged message with the try count.
 * @author ashita
 * @since 2016
 */
public class MessageSendContextCount {
	Message message;
	int sendTryCount = 0;
	
	MessageSendContextCount(Message message, int count){
		this.message = message;
		this.sendTryCount = count;
	}
	
	/**
	 * Get message
	 * @param none
	 * @return Message
	 */
	public Message getMessage() {
		return message;
	}
	
	/**
	 * Save message
	 * @param Message
	 * @return void
	 */
	public void setMessage(Message message) {
		this.message = message;
	}
	
	/**
	 * Get message sending trial count
	 * @param none
	 * @return int
	 */
	public int getSendTryCount() {
		return sendTryCount;
	}
	
	/**
	 * Save message sending trial count
	 * @param int
	 * @return void
	 */
	public void setSendTryCount(int sendTryCount) {
		this.sendTryCount = sendTryCount;
	}

}