package com.iotracks.iofabric.local_api;

import java.util.Map;

import com.iotracks.iofabric.status_reporter.StatusReporter;
import com.iotracks.iofabric.utils.logging.LoggingService;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

public class ControlWebsocketWorker  implements Runnable{
	private final String MODULE_NAME = "Local API";
	private static final Byte OPCODE_CONTROL_SIGNAL = 0xC;

	@Override
	public void run() {
		LoggingService.logInfo(MODULE_NAME,"Initiating control signals for unacknowledged signals");
		
		for(Map.Entry<ChannelHandlerContext, Integer> contextEntry : WebSocketMap.unackControlSignalsMap.entrySet()){
			ChannelHandlerContext ctx = contextEntry.getKey();
			int tryCount = contextEntry.getValue();
			if(tryCount < 10){
				try {
					initiateControlSignal(ctx);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				LoggingService.logInfo(MODULE_NAME," Initiating control signal expires");
				try {
					WebSocketMap.unackControlSignalsMap.remove(ctx);
					WebsocketUtil.removeWebsocketContextFromMap(ctx, WebSocketMap.controlWebsocketMap);
					StatusReporter.setLocalApiStatus().setOpenConfigSocketsCount(WebSocketMap.controlWebsocketMap.size());
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;
			}
			LoggingService.logInfo(MODULE_NAME,"Acknowledgement opcode not found" );		

			return;
		}
		return;
	}
	
	private void initiateControlSignal(ChannelHandlerContext ctx) throws Exception{

		int tryCount = WebSocketMap.unackControlSignalsMap.get(ctx);
		tryCount = tryCount+1;
		WebSocketMap.unackControlSignalsMap.put(ctx, tryCount);

		ByteBuf buffer1 = ctx.alloc().buffer();
		buffer1.writeByte(OPCODE_CONTROL_SIGNAL);
		ctx.channel().writeAndFlush(new BinaryWebSocketFrame(buffer1));
	}
}
