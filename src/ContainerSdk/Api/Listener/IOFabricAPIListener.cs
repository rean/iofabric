using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using IOTracks.IOFabric.ContainerSdk;

namespace Api.Listener
{
	public interface IOFabricAPIListener
	{
		void onMessages(List<IOMessage> messages);

		void onMessageQuery(long timeframestart, long timeframeend, List<IOMessage> messages);

		void onError(Exception cause);

		void onBadRequest(String error);

		void onMessageReceipt(String messageID, long timestamp);

		void onNewConfig(JObject config);

		void onNewConfigSignal();
	}
}
