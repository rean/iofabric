# The iotracks ioIntegrator product

This repository is the production code base for the x86 Linux version of the ioIntegrator product.

ioIntegrator is a service that runs constantly in the background on a Linux machine. It is the agent that turns a Linux computer into a piece of the iotracks I/O compute fabric.

There should be an ioIntegrator code base for every processing platform that becomes part of the I/O compute fabric. Network connectivity, process invocation, thread management, and other details of an ioIntegrator will vary from platform to platform. The same ioIntegrator principles apply to every version, but the implementation of the principles should match the native languages and structures best suited for the platform.

###Principles of an ioIntegrator:

* Never go down
* Respond immediately to the fabric controller
* Operate flawlessly when offline
* Report status frequently and reliably
* Execute instructions with no understanding of the bigger picture
* Provide a high-performance message bus and local API
* Enforce the configured resource consumption constraints strictly
* Allow the most flexible and powerful processing element model possible
* Be able to instantiate processing elements from any available source
* Be able to communicate with any reachable fabric controller
* Allow processing elements to implement security and connectivity as they would natively
* Ensure that complying with the local API is the only requirement placed on a processing element
* Only shutdown or restart processing elements when requested or when absolutely necessary
* Run only processing elements with verified source and integrity
* Never allow a message to reach unauthorized processing elements
* Only allow messages of the proper registered type to reach processing elements
* Guarantee message source and order


See the docs folder in this repository for architecture, engineering philosophy, functional specifications, and more.