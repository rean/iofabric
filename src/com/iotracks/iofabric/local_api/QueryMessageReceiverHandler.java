package com.iotracks.iofabric.local_api;

import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

import java.io.StringReader;
import java.util.List;
import java.util.concurrent.Callable;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import com.iotracks.iofabric.message_bus.Message;
import com.iotracks.iofabric.message_bus.MessageBus;
import com.iotracks.iofabric.utils.logging.LoggingService;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Handler to deliver the messages to the receiver, if found any. 
 * Messages are delivered for the particular query from the receiver. 
 * @author ashita
 * @since 2016
 */
public class QueryMessageReceiverHandler implements Callable<Object> {
	private final String MODULE_NAME = "Local API";
	
	private final FullHttpRequest req;
	private ByteBuf bytesData;

	public QueryMessageReceiverHandler(FullHttpRequest req, ByteBuf	bytesData) {
		this.req = req;
		this.bytesData = bytesData;
	}
	
	/**
	 * Handler method to deliver the messages to the receiver as per the query.
	 * Get the messages from message bus
	 * @param None
	 * @return Object
	 */
	public Object handleQueryMessageRequest() throws Exception{
		
		LoggingService.logInfo(MODULE_NAME,"In Query message receiver handler : handle");

		HttpHeaders headers = req.headers();

		if (req.getMethod() != POST) {
			LoggingService.logWarning(MODULE_NAME,"Request method not allowed");
			return new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED);
		}

		if(!(headers.get(HttpHeaders.Names.CONTENT_TYPE).equals("application/json"))){
			LoggingService.logWarning(MODULE_NAME,"Incorrect content type");
			String errorMsg = " Incorrect content type ";
			bytesData.writeBytes(errorMsg.getBytes());
			return new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST, bytesData);
		}

		ByteBuf msgBytes = req.content();
		String requestBody = msgBytes.toString(io.netty.util.CharsetUtil.UTF_8);
		JsonReader reader = Json.createReader(new StringReader(requestBody));
		JsonObject jsonObject = reader.readObject();

		if(validateMessageQueryInput(jsonObject) != null){
			LoggingService.logWarning(MODULE_NAME,"Incorrect input content/data");
			String errorMsg = validateMessageQueryInput(jsonObject);
			bytesData.writeBytes(errorMsg.getBytes());
			return new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST, bytesData);
		}

		String receiverId = jsonObject.getString("id");
		long timeframeStart = Long.parseLong(jsonObject.get("timeframestart").toString());
		long timeframeEnd = Long.parseLong(jsonObject.get("timeframeend").toString());
		JsonArray publishersArray = jsonObject.getJsonArray("publishers");

		JsonBuilderFactory factory = Json.createBuilderFactory(null);
		JsonObjectBuilder builder = factory.createObjectBuilder();
		JsonArrayBuilder messagesArray = factory.createArrayBuilder();

		MessageBus bus = MessageBus.getInstance();
		int msgCount = 0;

		for(int i=0; i<publishersArray.size(); i++){
			String publisherId = publishersArray.getString(i);
			
			List<Message> messageList = bus.messageQuery(publisherId, receiverId, timeframeStart, timeframeEnd);

			if(messageList != null){
				for(Message msg : messageList){
					JsonObject msgJson = msg.toJson();
					messagesArray.add(msgJson);
					msgCount++;
				}
			}
		}

		builder.add("status", "okay");
		builder.add("count", msgCount);
		builder.add("messages", messagesArray);

		String configData = builder.build().toString();
		bytesData.writeBytes(configData.getBytes());
		LoggingService.logInfo(MODULE_NAME,"Request completed successfully");
		FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, bytesData);
		HttpHeaders.setContentLength(res, bytesData.readableBytes());
		return res;
	}
	
	/**
	 * Validate the request and the query for the messages
	 * @param JsonObject
	 * @return String
	 */
	private String validateMessageQueryInput(JsonObject message){
		if(!message.containsKey("id")){
			LoggingService.logWarning(MODULE_NAME,"id not found");
			return "Error: Missing input field id";
		}

		if(!(message.containsKey("timeframestart") && message.containsKey("timeframeend"))){
			LoggingService.logWarning(MODULE_NAME,"timeframestart or timeframeend not found");
			return "Error: Missing input field timeframe start or end";
		}

		if(!message.containsKey("publishers")){
			LoggingService.logWarning(MODULE_NAME,"Publisher not found");
			return "Error: Missing input field publishers";
		}

		try{
			Long.parseLong(message.get("timeframestart").toString());
		}catch(Exception e){
			return "Error: Invalid value of timeframestart";
		}

		try{
			Long.parseLong(message.get("timeframeend").toString());
		}catch(Exception e){
			return "Error: Invalid value of timeframeend";
		}

		if((message.getString("id").trim().equals(""))) return "Error: Missing input field value id";

		return null;
	}
	
	/**
	 * Overriden method of the Callable interface which call the handler method
	 * @param None
	 * @return Object
	 */
	@Override
	public Object call() throws Exception {
		return handleQueryMessageRequest();
	}
}