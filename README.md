# The iotracks ioIntegrator product

This repository is the production code base for the x86 Linux version of the ioIntegrator product.

ioIntegrator is a service that runs constantly in the background on a Linux machine. It is the agent that turns a Linux computer into a piece of the iotracks I/O compute fabric.

There should be an ioIntegrator code base for every processing platform that becomes part of the I/O compute fabric. Network connectivity, process invocation, thread management, and other details of an ioIntegrator will vary from platform to platform. The same ioIntegrator principles apply to every version, but the implementation of the principles should match the native languages and structures best suited for the platform.

###Principles of an ioIntegrator:

* Do not

See the docs folder in this repository for architecture, engineering philosophy, functional specifications, and more.