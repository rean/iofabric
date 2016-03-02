# Local API Specification

This is the second version of the Local API, which is also sometimes called the "Edge API" or the "Edge Services" of the ioFabric. Only this second version will be offered in the new production release of ioFabric for Linux.

This Local API offers both standard REST API endpoints and Websocket API endpoints. While REST API standards are well-known and well-documented, the use of Websockets is still on the rise and not yet common. For Websocket specifications, we will follow the IANA standards here:

<a href="http://www.iana.org/assignments/websocket/websocket.xml" target="_new">IANA Official Websocket Registry</a>

This means that we will offer the standard closure codes, op codes, etc.


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


####Get Container Next Unread Messages

sghksdjkhdskjhfsd

#####Endpoint

<pre>
	http://iofabric:54321/v2/messages/next
</pre>

#####Response

<pre>
	{
		"status":"okay",
		"count":1,
		"messages":
			[
				{
					"id":"ObJ5STY02PMLM4XKXM8oSuPlc7mUh5Ej",
					"tag":"",
					"groupid":"",
					"sequencenumber":1,
					"sequencetotal":1,
					"priority":0,
					"timestamp":1452214777495,
					"publisher":"R4b2WPZRbycCzyZBz9tD7BdMWg94YDhQ",
					"authid":"",
					"authgroup":"",
					"infotype":"text",
					"infoformat":"utf-8",
					"contextlength":0,
					"contextdata":"",
					"contentlength":14,
					"contentdata":"A New Message!"
				}
			]
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

