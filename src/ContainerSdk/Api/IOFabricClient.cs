using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Api
{
	public class IOFabricClient
	{
		//logger
		private const string ID_PARAM_NAME = "id";
		private const string TIMEFRAME_START_PARAM_NAME = "timeframestart";
		private const string TIMEFRAME_END_PARAM_NAME = "timeframeends";
		private const string PUBLISHERS_PARAM_NAME = "publishiers";

		private string server;
		private int port;
		private bool ssl;
		private string elementID = "UNKNOWN_IO_TRACKS_CONTAINER_UIID";
		
	}
}
