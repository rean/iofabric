package com.iotracks.iofabric.local_api;


import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.CharsetUtil;

public class MessageSenderHandler {

	public void handle(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception{

		System.out.println("In MessageSenderHandler : handle");		
		HttpHeaders headers = req.headers();

		if (req.getMethod() != POST) {
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
			return;
		}

		if(!(headers.get(HttpHeaders.Names.CONTENT_TYPE).equals("application/json"))){
			System.out.println("header content type failure..." + headers.get("CONTENT_TYPE"));
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
			return;
		}

		ByteBuf msgBytes = req.content();
		String requestBody = msgBytes.toString(io.netty.util.CharsetUtil.US_ASCII);
		System.out.println("body :"+ requestBody);
		JsonReader reader = Json.createReader(new StringReader(requestBody));
		JsonObject jsonObject = reader.readObject();


		if(validateMessage(jsonObject) != null){
			System.out.println("Validation failure");
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
			return;
		}

		System.out.println("Validation Successful.. ");
		//Put JSON message on message bus, get message id and timestamp from bus 
		//Send in response

		JsonBuilderFactory factory = Json.createBuilderFactory(null);
		JsonObjectBuilder builder = factory.createObjectBuilder();
		builder.add("status", "okay");
		builder.add("timestamp", "12233444");
		builder.add("id", "wewwererwwerfty");

		String configData = builder.build().toString();
		ByteBuf	bytesData = ctx.alloc().buffer();
		bytesData.writeBytes(configData.getBytes());
		FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, bytesData);
		HttpHeaders.setContentLength(res, bytesData.readableBytes());

		sendHttpResponse( ctx, req, res); 
		return;


	}

	private String validateMessage(JsonObject message){
		System.out.println("In validateMessage...");

		if(!message.containsKey("publisher")) return "Error: Missing input field publisher";
		if(!message.containsKey("version")) return "Error: Missing input field version";
		if(!message.containsKey("infotype")) return "Error: Missing input field infotype";
		if(!message.containsKey("infoformat")) return "Error: Missing input field infoformat";
		if(!message.containsKey("contentdata")) return "Error: Missing input field contentdata";

		if((message.getString("publisher").trim().equals(""))) return "Error: Missing input field value publisher";
		if((message.getString("infotype").trim().equals(""))) return "Error: Missing input field value infotype";
		if((message.getString("infoformat").trim().equals(""))) return "Error: Missing input field value infoformat";

		String version = message.get("version").toString();
		if(!(version.matches("[0-9]+"))){
			return "Error: Invalid  value for version";
		}

		if(message.containsKey("sequencenumber")){
			String sNum = message.get("sequencenumber").toString();
			if(!(sNum.matches("[0-9]+"))){
				return "Error: Invalid  value for field sequence number";
			}
		}

		if(message.containsKey("sequencetotal")){
			String stot = message.get("sequencetotal").toString();
			if(!(stot.matches("[0-9]+"))){
				return "Error: Invalid  value for field sequence total";
			}
		}

		if(message.containsKey("priority")){
			String priority = message.get("priority").toString();
			if(!(priority.matches("[0-9]+"))){
				return "Error: Invalid  value for field priority";
			}
		}

		if(message.containsKey("chainposition")){
			String chainPos = message.get("chainposition").toString();
			if(!(chainPos.matches("[0-9]+"))){
				return "Error: Invalid  value for field chain position";
			}
		}

		if(message.containsKey("difficultytarget")){
			String difftarget = message.get("difficultytarget").toString();
			if(!(difftarget.matches("[0-9]+"))){
				return "Error: Invalid  value for field difficulty target";
			}
		}

		return null;
	}


	private static void sendHttpResponse(
			ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
		// Generate an error page if response getStatus code is not OK (200).
		if (res.getStatus().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
			HttpHeaders.setContentLength(res, res.content().readableBytes());
		}

		// Send the response and close the connection if necessary.
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}
}
