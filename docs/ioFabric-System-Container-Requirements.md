# ioFabric System Container Requirements

Every ioFabric instance comes with some default containers. These "system containers" provide functionality that enhances the ioFabric software. The reason this functionality comes in the form of a container is so it can be updated easily for all ioFabric instances. One of the best things about ioFabric is that once the base software that handles containers is running, everything else can be implemented as a container and this minimizes the versioning problems that happen with distributed software.

The first system container is called Core Networking. It is responsible for connecting to instances of the ComSat product, also made by iotracks. ComSat creates secure network pipes that move pure bytes from one place to another through all firewalls and other internetworking challenges. So the Core Networking container has functionality that manages connections to the ComSat, understands how to verify the identity of a ComSat, and relays the incoming traffic to the proper place in the ioFabric instance. It also moves the outbound traffic to the ComSat so it can reach its desintation on the other side (which is always unknown from the ioFabric instance's perspective).

The second system container is called Debug Console. Its primary purpose is to give developers the ability to look at ioMessages without needing to build an application to do so. So why can't developers just look at ioMessages directly? Because actual ioMessages move very quickly from one container to another and then they are gone. They either end up going out from an ioFabric instance to another ioFabric instance or going out to some final endpoint such as a data repository, an enterprise cloud system, or something similar. Developers need to look at ioMessages so they can debug their code in production situations and so they can see what the ioMessage data looks like at different points in the container-to-container processing chain.

The Debug Console captures the ioMessages that are routed to it and makes them available through a REST API. By taking data that is in motion and holding onto it, the Debug Console gives the developer a chance to see what's happening in a live system just like they were running a debugging console on a local build environment. It's a lot like setting a breakpoint in an IDE and looking at the value of some variables or objects.

The Debug Console hosts a REST API on port 80 that provides access to the messages it is holding. In order to talk to the container on port 80, the public port feature of the ComSat technology is used. This allows a developer to see ioMessages moving through a live system from anywhere. This is important because most deployed instances of ioFabric will not be in the same physical location as the developer or the solution maintenance person. To prevent unwanted access to the ioMessages, the Debug Console only responds to REST API requests that provide a valid access token. The Debug Console container gets the current valid access token from its container configuration information.


####Core Networking Container Requirements

* abc


####Stream Viewer Container Requirements

* dsgsds


####Debug Console Container Requirements

* sdsdf


