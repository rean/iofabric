package com.iotracks.iofabric.local_api;

import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import com.iotracks.iofabric.utils.logging.LoggingService;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

public class GetConfigurationHandler implements Callable<Object> {

	private final String MODULE_NAME = "Local API";
	
	private final FullHttpRequest req;
	private ByteBuf bytesData;
	
	public GetConfigurationHandler(FullHttpRequest req, ByteBuf	bytesData) {
		this.req = req;
		this.bytesData = bytesData;
	}
	

	public Object handleGetConfigurationRequest() {
		LoggingService.logInfo(MODULE_NAME,"In Get Configuration Handler: handle");

		if (req.getMethod() != POST) {
			return new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED);
		}

		HttpHeaders headers = req.headers();

		if(!(headers.get(HttpHeaders.Names.CONTENT_TYPE).equals("application/json"))){
			String errorMsg = " Incorrect content/data ";
			bytesData.writeBytes(errorMsg.getBytes());
			return new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST, bytesData);
		}

		ByteBuf msgBytes = req.content();
		String requestBody = msgBytes.toString(io.netty.util.CharsetUtil.US_ASCII);
		LoggingService.logInfo(MODULE_NAME,"body :"+ requestBody);
		JsonReader reader = Json.createReader(new StringReader(requestBody));
		JsonObject jsonObject = reader.readObject();
		
		if(getErrorMessageInReq(jsonObject) != null){
			String errorMsg = getErrorMessageInReq(jsonObject);
			bytesData.writeBytes(errorMsg.getBytes());
			return new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST, bytesData);
		}

		String receiverId = jsonObject.getString("id");
		LoggingService.logInfo(MODULE_NAME,"In GetConfigurationHandler: handle - Publisher " + receiverId);

		for(Map.Entry<String, String> entry : ConfigurationMap.containerConfigMap.entrySet()){
			if(entry.getKey().equals(receiverId)){
				LoggingService.logInfo(MODULE_NAME,"Element found: status ok");
				String containerConfig = entry.getValue();
				JsonBuilderFactory factory = Json.createBuilderFactory(null);
				JsonObjectBuilder builder = factory.createObjectBuilder();
				builder.add("status", "okay");
				builder.add("config", containerConfig);
				String configData = builder.build().toString();
				LoggingService.logInfo(MODULE_NAME,"Config: "+ configData);
				bytesData.writeBytes(configData.getBytes());
				FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, bytesData);
				HttpHeaders.setContentLength(res, bytesData.readableBytes());
				return res;
			}else{
				String errorMsg = "No configuration found for the id" + receiverId;
				LoggingService.logInfo(MODULE_NAME,"Element not found");
				bytesData.writeBytes(errorMsg.getBytes());
				return new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST, bytesData);
			}
		}
		return new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST, bytesData);
	}

	private String getErrorMessageInReq(JsonObject jsonObject){
		String error = null;
		if(!jsonObject.containsKey("id")) return " Id not found ";
		if(jsonObject.getString("id").equals(null) || jsonObject.getString("id").trim().equals("")) return " Id value not found ";
		return error;
	}

	@Override
	public Object call() throws Exception {
		return handleGetConfigurationRequest();
	}
}