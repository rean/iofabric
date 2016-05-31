using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Utils
{
	public enum IOFabricLocalAPIURL
	{
		GET_CONFIG_REST_LOCAL_API = "/v2/config/get",
		GET_NEXT_MSG_REST_LOCAL_API="/v2/messages/next",
		POST_MSG_REST_LOCAL_API="/v2/messages/new",
		GET_MSGS_QUERY_REST_LOCAL_API="/v2/messages/query",
		GET_CONTROL_WEB_SOCKET_LOCAL_API="/v2/control/socket/id",
		GET_MSG_WEB_SOCKET_LOCAL_API="/v2/messages/socket/id",
	}

}
