package com.iotracks.iofabric.local_api;

import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

import java.io.StringReader;
import java.util.List;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import com.iotracks.iofabric.element.Element;

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

public class GetConfigurationHandler {

	public void handle(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception{
		System.out.println("In GetConfigurationHandler: handle");

		if (req.getMethod() != POST) {
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
			return;
		}
		
		HttpHeaders headers = req.headers();

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

		if(!jsonObject.containsKey("id")){
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
			return;
		}

		String publisherId = jsonObject.getString("id");
		System.out.println("In GetConfigurationHandler: handle - Publisher " + publisherId);

		boolean elementFound = false;

		//To change - For testing 
		//Get configuration object from field agent module and make JSON and pass
		LocalApi api = new LocalApi();
		List<Element> containersList = api.readContainerConfig();

		//Element found
		for(Element element : containersList){
			if(element.getElementID().equals(publisherId)){
				System.out.println("Element found: status ok");
				//HttpResponse res = new DefaultHttpResponse(HTTP_1_1, OK);
				elementFound = true;
				JsonBuilderFactory factory = Json.createBuilderFactory(null);
				JsonObjectBuilder builder = factory.createObjectBuilder();
				builder.add("status", "okay");
				if(element.getElementConfig() != null) {
					builder.add("config", element.getElementConfig());
				} else {
					builder.add("config","");
				}

				String configData = builder.build().toString();
				
			//	res.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
				System.out.println("Config: "+ configData);
				ByteBuf	bytesData = ctx.alloc().buffer();
				bytesData.writeBytes(configData.getBytes());
				FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, bytesData);
				HttpHeaders.setContentLength(res, bytesData.readableBytes());
				
				sendHttpResponse( ctx, req, res); 
				return;
			}
		}

		if(elementFound == false) {
			System.out.println("Element not found: status FORBIDDEN");
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
		}

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
