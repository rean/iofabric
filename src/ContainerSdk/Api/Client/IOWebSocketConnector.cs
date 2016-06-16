using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Api.Handler;

namespace Api.Client
{
	public class IOWebSocketConnector
	{
		private Logger log = new Logger();
		
		private IOContainerWSAPIHandler handler;
		private Boolean ssl;
		private String host;
		private int port;

		public IOWebSocketConnector(IOContainerWSAPIHandler handler, Boolean ssl, String host, int port)
		{
			this.handler = handler;
			this.ssl = ssl;
			this.host = host;
			this.port = port;
		}

		public void run()
		{
			//IOFabricApiConnector ioFabricAPIConnector = new IOFabricApiConnector(handler, ssl); (class initiation)
			//Channel channel = ioFabricAPIConnector.initConnection(host, port); //	(import statement)
			try
			{
				//handler.handshakeFuture().sync(); (class method)
			}
			catch(Exception e)
			{
				log.write("Error synchronizing channel for WebSocket connection");
			}
		}
	}
}
