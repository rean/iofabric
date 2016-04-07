# ioFabric System Container Requirements

Every ioFabric instance comes with some default containers. These "system containers" provide functionality that enhances the ioFabric software. The reason this functionality comes in the form of a container is so it can be updated easily for all ioFabric instances. One of the best things about ioFabric is that once the base software that handles containers is running, everything else can be implemented as a container and this minimizes the versioning problems that happen with distributed software.

The first system container is called Core Networking. It is responsible for connecting to instances of the ComSat product, also made by iotracks. ComSat creates secure network pipes that move pure bytes from one place to another through all firewalls and other internetworking challenges. So the Core Networking container has functionality that manages connections to the ComSat, understands how to verify the identity of a ComSat, and relays the incoming traffic to the proper place in the ioFabric instance. It also moves the outbound traffic to the ComSat so it can reach its desintation on the other side (which is always unknown from the ioFabric instance's perspective).

The second system container is called Debug Console. Its primary purpose is to give developers the ability to look at ioMessages without needing to build an application to do so. So why can't developers just look at ioMessages directly? Because actual ioMessages move very quickly from one container to another and then they are gone. They either end up going out from an ioFabric instance to another ioFabric instance or going out to some final endpoint such as a data repository, an enterprise cloud system, or something similar. Developers need to look at ioMessages so they can debug their code in production situations and so they can see what the ioMessage data looks like at different points in the container-to-container processing chain.

The Debug Console captures the ioMessages that are routed to it and makes them available through a REST API. By taking data that is in motion and holding onto it, the Debug Console gives the developer a chance to see what's happening in a live system just like they were running a debugging console on a local build environment. It's a lot like setting a breakpoint in an IDE and looking at the value of some variables or objects.

The Debug Console hosts a REST API on port 80 that provides access to the messages it is holding. In order to talk to the container on port 80, the public port feature of the ComSat technology is used. This allows a developer to see ioMessages moving through a live system from anywhere. This is important because most deployed instances of ioFabric will not be in the same physical location as the developer or the solution maintenance person. To prevent unwanted access to the ioMessages, the Debug Console only responds to REST API requests that provide a valid access token. The Debug Console container gets the current valid access token from its container configuration information.


####Core Networking Container Requirements

* Hold a pool of socket connections to the ComSat specified in the configuration

* Create the number of pool connections specified in the configuration

* If configured in "private" mode, receive and post messages from and to the ioFabric

* If configured in "public" mode, take incoming bytes on the ComSat socket and pipe them directly into a local network request

* Make local network requests based on the configuration provided for this container

* Pipe the response from the local network request directly back to the ComSat socket which sent the bytes

* When a ComSat socket closes, remove it from the connection pool

* Monitor the connection pool and make sure it always has the configured number of open connections

* If connectivity to the ComSat disappears for any reason (gets dropped, network unavailable, etc.) then close all connections in the pool and open fresh connections to the ComSat

* If connections to the ComSat cannot be opened, try again regularly but don't consume too much CPU usage

* Send the ComSat socket passcode (provided in container configuration) immediately upon successfully opening each ComSat socket

* When in "private" mode, use the real-time data message Websocket to send and receive messages to and from the ioFabric Local API

* Use the real-time control message Websocket to make sure any changes to container configuration are received immediately

* When a "new config" control message is received, immediately make a request on the ioFabric REST API to retrieve the updated container configuration

* Build this system container with the Node.js Container SDK

* Use TLS secure socket connections to the ComSat, which will not open successfully if the identity of the ComSat cannot be verified

* Use a local intermediate public certificate file to verify the identity of the ComSat

* Send a heartbeat transmission to the ComSat on every open socket at the interval specified in the container configuration

* Send the ASCII byte values 'BEAT' as the heartbeat transmission on the ComSat sockets

* Keep track of the last time each socket had successful communication with the ComSat (the "last active" timestamp)

* Check incoming socket messages to see if they are equal to 'BEAT' or 'BEATBEAT'... if they are, update the "last active" timestamp for the receiving socket

* When a ComSat socket has been inactive past the threshold (set in container config) then close the socket so a new one can be opened

* Check incoming socket messages to see if they are equal to "AUTHORIZED"... if they are, update the "last active" timestamp for the receiving socket... this is a response that the ComSat will provide when the passcode was correct for a newly opened socket

* When sending a message on a ComSat socket, send a 'TXEND' transmission immediately after the end of the actual message

* When receiving a message on a ComSat socket, accumulate the incoming message bytes until receiving a 'TXEND' transmission... then it is OK to parse the message

* After receiving a 'TXEND' transmission on a ComSat socket, send an 'ACK' transmission 

* When operating in "private" mode, keep a buffer of messages to be sent on the ComSat socket... this this allows messages to still be delivered under troublesome network connectivity situations

* Remove a message from the buffer when an "ACK" message has been received after sending the message

* If an 'ACK' is not received after sending a message on a ComSat socket, send the same message again after a short time period

* Limit the amount of messages stored in the buffer to a safe level to avoid memory limit crashes... simply delete the oldest message when a new one arrives

* Limit the number of bytes being buffered by a receiving ComSat socket to a safe level... drop bytes out of memory if needed and do not attempt to parse messages that have missing bytes... and close a socket connection if needed in order to avoid memory limit crashes... alternatively you can drop bytes until receiving a 'TXEND' and then send an 'ACK' in order to avoid receiving the same large message again

* Parse and consume container configuration according to this example:

<pre>
	{"mode":"public","host":"comsat1.iotracks.com","port":35046,"connectioncount":60,"passcode":"vy7cvpztnhgc3jdptgxp9ttmzxfyfbqh","localhost":"iofabric","localport":60401,"heartbeatfrequency":20000,"heartbeatabsencethreshold":60000}

	Or

	{"mode":"private","host":"comsat2.iotracks.com","port":35081,"connectioncount":1,"passcode":"vy7cvpztnhgc3jdptgxp9ttmzxfyfbqh","localhost":"","localport":0,"heartbeatfrequency":20000,"heartbeatabsencethreshold":60000}
</pre>


####Debug Console Container Requirements

* Get the current container configuration from the ioFabric Local API immediately when the container starts

* Open a control message Websocket connection to the ioFabric Local API and make sure that an open connection is always present

* When a new config message is received on the control message Websocket, send an "acknowledge" response and then make a request to the ioFabric Local API REST endpoing to get the current container configuration

* Whenever container configuration is received, use the configuration information to set up the container's operations according to the config information - the template for the configuration information can be found in the Debug Console Specification document

* Open a data message Websocket connection to the ioFabric Local API and make sure that an open connection is always present

* Receive messages that arrive on the data message Websocket connection and send an "acknowledge" response and then store each message in the appropriate file

* Store messages in a different file for each publisher

* Store messages in JSON format in the files in a way that allows them to be retrieved and turned into an array easily

* Name the storage files as "XXXX.json" where the XXXX is the actual publisher ID

* Limit the stored file size for each publisher -  The limit storage size will be provided in the container configuration

* When a file for a particular publisher file has reached its size limit, simply delete the oldest message to make room for the next new message

* Provide a REST API on port 80 according to the Debug Console Specification document

* Provide a "404 Not Found" response to any request on the REST API that does not include a valid access token

* Use the local JSON message storage files to get the messages needed for output on the REST API

* Use the Java Container SDK to build the container


####Stream Viewer Container Requirements

* dsgsds



