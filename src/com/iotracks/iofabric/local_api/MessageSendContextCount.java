package com.iotracks.iofabric.local_api;

import com.iotracks.iofabric.message_bus.Message;

public class MessageSendContextCount {
	Message message;
	int sendTryCount = 0;
	
	MessageSendContextCount(Message message, int count){
		this.message = message;
		this.sendTryCount = count;
	}
	
	public Message getMessage() {
		return message;
	}
	public void setMessage(Message message) {
		this.message = message;
	}
	public int getSendTryCount() {
		return sendTryCount;
	}
	public void setSendTryCount(int sendTryCount) {
		this.sendTryCount = sendTryCount;
	}

}