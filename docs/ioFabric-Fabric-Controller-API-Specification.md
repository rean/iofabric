# Fabric Controller API V2 Specification

#####This is the 2nd version of the Fabric Controller API. The first version remains active and unchanged.

Each ioFabric instance can do very little without connecting to a fabric controller. In fact, connecting to a fabric controller is what makes a particular ioFabric instance become an actual part of the I/O Compute Fabric. Every fabric controller will offer this API exactly as it is shown here. This allows an ioFabric instance to connect to fabric controller and operate properly.

The API endpoints are listed here with a short description and the actual inputs and outputs. The actual IP address or domain name of the fabric controller will vary from deployment to deployment. It is mandatory that HTTPS be used, and both domain names and IP addresses are allowed for connecting to a fabric controller. The placeholder address of 1.2.3.4 is used in this document for the location of the fabric controller.

####Server Status

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


