# Fabric Controller API V2 Specification

#####This is the 2nd version of the Fabric Controller API. The first version remains active and unchanged.

Each ioFabric instance can do very little without connecting to a fabric controller. In fact, connecting to a fabric controller is what makes a particular ioFabric instance become an actual part of the I/O Compute Fabric. Every fabric controller will offer this API exactly as it is shown here. This allows an ioFabric instance to connect to fabric controller and operate properly.

The API endpoints are listed here with a short description and the actual inputs and outputs. The actual IP address or domain name of the fabric controller will vary from deployment to deployment. It is mandatory that HTTPS be used, and both domain names and IP addresses are allowed for connecting to a fabric controller. The placeholder address of 1.2.3.4 is used in this document for the location of the fabric controller.

####Get Server Status

This endpoint just gives you a response from the fabric controller with its status. It can be used for simple "ping" purposes to see if the fabric controller is online and operational.

#####Endpoint

<pre>
	https://1.2.3.4/api/v2/status
</pre>

#####Response

<pre>
	{
		“status”:”ok”,
		”timestamp”:1234567890123
	}
</pre>

#####Querystring Parameters

<pre>
	None
</pre>

#####POST Parameters

<pre>
	None
</pre>


####Get ioFabric Instance ID and Access Token

This endpoint registers the ioFabric instance that is submitting the provisioning key and delivers the ioFabric instance ID along with an access token that must be submitted for any further API interaction. The access token remains valid until it is revoked. If it becomes invalid, the ioFabric instance must be re-provisioned to re-establish access to the fabric controller API.

#####Endpoint

<pre>
	https://1.2.3.4/api/v2/instance/provision/key/A8842h
</pre>

#####Response

<pre>
	{
		“status”:”ok”,
		”timestamp”:1234567890123,
		“id”:”4sd9whcfh”,
		“token”:”3498wfesdhusdvkjh3refkjhsdpaohrg”
	}
</pre>

#####Querystring Parameters

<pre>
	key - the provisioning key provided via the command line (example shown here as a8842h)
</pre>

#####POST Parameters

<pre>
	None
</pre>


####Post ioFabric Instance Status Information

This endpoint allows the ioFabric instance to send its status information to the fabric controller. This should be done regularly, but not so often as to waste bandwidth and CPU resources.

#####Endpoint

<pre>
	https://1.2.3.4/api/v2/instance/status/id/4sd9whcfh/token/3498wfesdhusdvkjh3refkjhsdpaohrg
</pre>

#####Response

<pre>
	{
		“status”:”ok”,
		”timestamp”:1234567890123
	}
</pre>

#####Querystring Parameters

<pre>
	id - the instance ID held by the ioFabric instance (example shown here as 4sd9whcfh)
    
    token - the access token given to the ioFabric instance for accessing the API (example shown here as 3498wfesdhusdvkjh3refkjhsdpaohrg)
</pre>

#####POST Parameters

<pre>
    daemonstatus - ioFabric daemon status string (example: running)
    
    daemonoperatingduration - ioFabric daemon operating duration in milliseconds (example: 43473272)
    
    daemonlaststart - Timestamp of the ioFabric daemon last start in milliseconds (example: 1234567890123)
    
    memoryusage - ioFabric current memory usage in mebibytes MiB (example: 562.8)
    
    diskusage - ioFabric current disk usage in gibibytes GiB (example: 2.79)
    
    cpuusage - ioFabric current CPU usage in percent (example: 24.71)
    
    memoryviolation - Status indicating if the ioFabric's current memory usage is in violation of the configured limit (example: yes)
    
    diskviolation - Status indicating if the ioFabric's current disk usage is in violation of the configured limit (example: no)
    
    cpuviolation - Status indicating if the ioFabric's current CPU usage is in violation of the configured limit (example: no)
    
    elementstatus - JSON string providing the status of all elements (example below)
    	
    	[{"id":"sdfkjhweSDDkjhwer8","status":"starting","starttime":1234567890123,"operatingduration":278421},{"id":"239y7dsDSFuihweiuhr32we","status":"stopped","starttime":1234567890123,"operatingduration":421900}]
        
    repositorycount - Number of Docker container registries being used by the ioFabric instance (example: 5)

    repositorystatus - JSON string providing the status of all the repositories (example below)

    	[{"url":"hub.docker.com","linkstatus":"connected"},{"url":"188.65.2.81/containers","failed login"}]

    systemtime - Timestamp of the current ioFabric system time in milliseconds (example: 1234567890123)
    
    laststatustime - Timestamp in milliseconds of the last time any status information on the ioFabric instance was updated (example: 1234567890123)

    ipaddress - Current IP address of the network adapter configured for the ioFabric instance (example: 10.0.15.13)

    processedmessages - Total number of messages processed by the ioFabric instance (example: 4481)

    elementmessagecounts - JSON string providing the number of messages published per element (example below)

    	[{"id":"d9823y23rewfouhSDFkh","messagecount":428},{"id":"978yerwfiouhASDFkjh","messagecount":8321}]
    
    messagespeed - The average speed, in milliseconds, of messages moving through the ioFabric instance (example: 84)

    lastcommandtime - Timestamp, in milliseconds, of the last update received by the ioFabric instance (example: 1234567890123)
</pre>


####Get ioFabric Configuration

This endpoint provides the configuration for the ioFabric instance. Note that some configuration items, such as the fabric controller URL and certificate path, are not featured here. That's for security reasons. If someone gains control of a fabric controller, we don't want them to be able to tell the ioFabric instances to listen to a different fabric controller. This also prevents accidental disconnection of ioFabric instances from the fabric controller.

#####Endpoint

<pre>
	https://1.2.3.4/api/v2/instance/config/id/4sd9whcfh/token/3498wfesdhusdvkjh3refkjhsdpaohrg
</pre>

#####Response

<pre>
	{
        “status”:”ok”,
        “timestamp”:1234567890123,
        “config”:
            {
                "networkinterface":"p2p1",
                "dockerurl":"unix:///var/run/docker.sock",
                "disklimit":12.0,
                "diskdirectory":"/var/lib/iofabric/",
                "memorylimit":1024.0,
                "cpulimit":35.58,
                "loglimit":2.45,
                "logdirectory":"/var/log/iofabric/",
                "logfilecount":10
            }
    }
</pre>

#####Querystring Parameters

<pre>
	id - the instance ID held by the ioFabric instance (example shown here as 4sd9whcfh)
    
    token - the access token given to the ioFabric instance for accessing the API (example shown here as 3498wfesdhusdvkjh3refkjhsdpaohrg)
</pre>

#####POST Parameters

<pre>
	None
</pre>


