package com.iotracks.iofabric.local_api;

import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

import java.io.StringReader;
import java.util.concurrent.Callable;

import javax.json.Json;
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
 * Handler to publish the messages from the container to message bus
 * @author ashita
 * @since 2016
 */
public class MessageSenderHandler implements Callable<Object> {
	private final String MODULE_NAME = "Local API";

	private final FullHttpRequest req;
	private ByteBuf bytesData;

	public MessageSenderHandler(FullHttpRequest req, ByteBuf bytesData) {
		this.req = req;
		this.bytesData = bytesData;
	}
	
	/**
	 * Handler method to publish the messages from the container to message bus
	 * @param None
	 * @return Object
	 */
	public Object handleMessageSenderRequest() throws Exception{

		LoggingService.logInfo(MODULE_NAME,"In message sender handler : handle");		
		HttpHeaders headers = req.headers();

		if (req.getMethod() != POST) {
			LoggingService.logWarning(MODULE_NAME,"Request method not allowed");
			return new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED);
		}

		if(!(headers.get(HttpHeaders.Names.CONTENT_TYPE).trim().split(";")[0].equalsIgnoreCase("application/json"))){
			LoggingService.logWarning(MODULE_NAME,"Incorrect content type");
			String errorMsg = " Incorrect content type ";
			bytesData.writeBytes(errorMsg.getBytes());
			return new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST, bytesData);
		}

		ByteBuf msgBytes = req.content();
		String msgString = msgBytes.toString(io.netty.util.CharsetUtil.UTF_8);

		JsonReader reader = Json.createReader(new StringReader(msgString));
		JsonObject jsonObject = reader.readObject();

		if(validateMessage(jsonObject) != null){
			LoggingService.logWarning(MODULE_NAME,"Incorrect content/data");
			LoggingService.logWarning(MODULE_NAME,"Validation Error");
			String errorMsg = validateMessage(jsonObject);
			bytesData.writeBytes(errorMsg.getBytes());
			return new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST, bytesData);
		}

		MessageBus bus = MessageBus.getInstance();
		Message message = new Message(jsonObject);
		Message messageWithId = bus.publishMessage(message);

		JsonBuilderFactory factory = Json.createBuilderFactory(null);
		JsonObjectBuilder builder = factory.createObjectBuilder();
		builder.add("status", "okay");
		builder.add("timestamp", messageWithId.getTimestamp());
		builder.add("id", messageWithId.getId());

		String configData = builder.build().toString();
		bytesData.writeBytes(configData.getBytes());
		FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, bytesData);
		LoggingService.logInfo(MODULE_NAME,"Messages published successfully");
		HttpHeaders.setContentLength(res, bytesData.readableBytes());
		return res;
	}
	
	/**
	 * Validate the request and the message to be publish
	 * @param JsonObject
	 * @return String
	 */
	private String validateMessage(JsonObject message) throws Exception{

		if(!message.containsKey("publisher")) return "Error: Missing input field publisher ";
		if(!message.containsKey("version")) return "Error: Missing input field version ";
		if(!message.containsKey("infotype")) return "Error: Missing input field infotype ";
		if(!message.containsKey("infoformat")) return "Error: Missing input field infoformat ";
		if(!message.containsKey("contentdata")) return "Error: Missing input field contentdata ";

		if((message.getString("publisher").trim().equals(""))) return "Error: Missing input field value publisher ";
		if((message.getString("infotype").trim().equals(""))) return "Error: Missing input field value infotype ";
		if((message.getString("infoformat").trim().equals(""))) return "Error: Missing input field value infoformat ";

		String version = message.get("version").toString();
		if(!(version.matches("[0-9]+"))){
			return "Error: Invalid  value for version";
		}

		if(message.containsKey("sequencenumber")){
			String sNum = message.get("sequencenumber").toString();
			if(!(sNum.matches("[0-9]+"))){
				return "Error: Invalid  value for field sequence number ";
			}
		}

		if(message.containsKey("sequencetotal")){
			String stot = message.get("sequencetotal").toString();
			if(!(stot.matches("[0-9]+"))){
				return "Error: Invalid  value for field sequence total ";
			}
		}

		if(message.containsKey("priority")){
			String priority = message.get("priority").toString();
			if(!(priority.matches("[0-9]+"))){
				return "Error: Invalid  value for field priority ";
			}
		}

		if(message.containsKey("chainposition")){
			String chainPos = message.get("chainposition").toString();
			if(!(chainPos.matches("[0-9]+"))){
				return "Error: Invalid  value for field chain position ";
			}
		}

		if(message.containsKey("difficultytarget")){
			String difftarget = message.get("difficultytarget").toString();
			if(!(difftarget.matches("[0-9]*.?[0-9]*"))){
				return "Error: Invalid  value for field difficulty target ";
			}
		}

		return null;
	}
	
	/**
	 * Overriden method of the Callable interface which call the handler method
	 * @param None
	 * @return Object
	 */
	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		return handleMessageSenderRequest();
	}
}
