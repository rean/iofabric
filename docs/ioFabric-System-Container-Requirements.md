# ioFabric System Container Requirements

Every ioFabric instance comes with some default containers. These "system containers" provide functionality that enhances the ioFabric software. The reason this functionality comes in the form of a container is so it can be updated easily for all ioFabric instances. One of the best things about ioFabric is that once the base software that handles containers is running, everything else can be implemented as a container and this minimizes the versioning problems that happen with distributed software.

The first system container is called Core Networking. It is responsible for connecting to instances of the ComSat product, also made by iotracks. ComSat creates secure network pipes that move pure bytes from one place to another through all firewalls and other internetworking challenges. So the Core Networking container has functionality that manages connections to the ComSat, understands how to verify the identity of a ComSat, and relays the incoming traffic to the proper place in the ioFabric instance. It also moves the outbound traffic to the ComSat so it can reach its desintation on the other side (which is always unknown from the ioFabric instance's perspective).

The second system container is called Debug Console. Its primary purpose is to give developers the ability to look at ioMessages without needing to build an application to do so. So why can't developers just look at ioMessages directly? Because actual ioMessages move very quickly from one container to another and then they are gone. They either end up going out from an ioFabric instance to another ioFabric instance or going out to some final endpoint such as a data repository, an enterprise cloud system, or something similar. Developers need to look at ioMessages so they can debug their code in production situations and so they can see what the ioMessage data looks like at different points in the container-to-container processing chain.

The Debug Console captures the ioMessages that are routed to it and makes them available through a REST API. By taking data that is in motion and holding onto it, the Debug Console gives the developer a chance to see what's happening in a live system just like they were running a debugging console on a local build environment. It's a lot like setting a breakpoint in an IDE and looking at the value of some variables or objects.

The Debug Console hosts a REST API on port 80 that provides access to the messages it is holding. In order to talk to the container on port 80, the public port feature of the ComSat technology is used. This allows a developer to see ioMessages moving through a live system from anywhere. This is important because most deployed instances of ioFabric will not be in the same physical location as the developer or the solution maintenance person. To prevent unwanted access to the ioMessages, the Debug Console only responds to REST API requests that provide a valid access token. The Debug Console container gets the current valid access token from its container configuration information.


####Core Networking Container Requirements

* abc


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



####Stream Viewer Container Requirements

* dsgsds



