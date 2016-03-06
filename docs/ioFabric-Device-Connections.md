# Connecting Devices to ioFabric

One of the main challenges of the Internet of Things (IoT) is the large variety of connection methods for devices and systems. If a sensor cannot communicate with the greater system, then all is lost. ioFabric provides both edge processing and edge connectivity. The connectivity is very flexible, which also means there are some decisions to be made for each implementation.

This document describes the different ways that you can connect sensors, devices, legacy systems, and the greater world into ioFabric. Once you do that, of course, the rest is pretty darn easy.

Some connectivity methods are more efficient than others. Some should only be used if there is no better option available. The drawbacks and benefits of each connection method are listed here to help you determine what will fit best for your situations.

####Listen for Incoming Data

When you add a container element to your ioFabric instance, you can choose to open ports. The ports are mapped so that you can have a different port on the inside of the container than the one which is exposed to the outside world. This is so the container code can be written to listen to standard ports (such as 80 or 443) and yet there can be many such containers running at the same time in an ioFabric instance.

By opening a listening port on a container element, the sensors and devices outside of ioFabric can direct their communications to the IP address of the ioFabric instance and the port of the appropriate container element. The container element will simply start receiving the incoming network traffic and can perform all of the parsing, decoding, decrypting, and other tasks needed to work with the device.

Note that opening a port for listening does not in any reduce the container element's ability to send network traffic out or talk directly to the devices.

#####Pros

* Efficiency - the sensors and devices talk directly to the receiving container elements whenever they need
* Simplicity - the container element does not need to establish connections or request data
* Scale - a single listening container element can take in data from a large number of devices

#####Cons

* Setup - the sensors, devices, and external systems all need to be setup with the IP address and port information of the receiving container element
* Security - if the container element is not built with protection mechanisms in place, the external port opening can pose a security risk
* Network - the sensors, devices, and external systems will need to be able to send traffic over Internet Protocol (IP) in order to reach the container element



