# Local API Specification

This is the second version of the Local API, which is also sometimes called the "Edge API" or the "Edge Services" of the ioFabric. Only this second version will be offered in the new production release of ioFabric for Linux.

This Local API offers both standard REST API endpoints and Websocket API endpoints. While REST API standards are well-known and well-documented, the use of Websockets is still on the rise and not yet common. For Websocket specifications, we will follow the IANA standards here:

<a href="http://www.iana.org/assignments/websocket/websocket.xml" target="_new">IANA Official Websocket Registry</a>



####Get Container Configuration

This endpoint provides the current JSON configuration string for the requesting container. Containers identify themselves by their element ID, which is mapped into the container as an environment variable.

#####Endpoint

<pre>
	http://iofabric:54321/v2/config/get
</pre>

#####Response

<pre>
	{
		"status":"okay",
		"config":"{\"property1\":\"value1\",\"property2\":\"value2\"}"
	}
</pre>

#####Querystring Parameters

<pre>
	None
</pre>

#####POST Parameters

<pre>
	{“publisher”:”R4b2WPZRbycCzyZBz9tD7BdMWg94YDhQ”}

	Note: The POST value is JSON and must be sent with HTTP header set as “Content-Type:application/json”
</pre>

