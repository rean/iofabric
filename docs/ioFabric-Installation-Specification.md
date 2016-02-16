# Installation Specification

One of the most important aspects of the ioFabric product is its ease of installation. Unfortunately, it is difficult to produce an easy installation experience across a variety of Linux machines. It is even harder to make such an installation completely reliable.

That's why we are putting the challenge of creating a great installation experience right at the front. Adding an installation package after a product is fully built seems like the logical treatment, but I believe it is backwards. If installation is important, put it up front.

The packaging mechanisms and install scripts can grow and change, but the requirements of a great product installation experience don't waver. If we follow these requirements from the start, we will be able to overcome the challenges.

####Officially Supported Linux Versions
* CentOS 7
* RHEL (Red Hat Enterprise Linux) 7.0
* RHEL (Red Hat Enterprise Linux) 7.1
* RHEL (Red Hat Enterprise Linux) 7.2
* Debian 7.7
* Debian 8
* Ubuntu 12.04
* Ubuntu 14.04
* Ubuntu 15.10

####Installation Requirements

* Set up the ioFabric daemon to run as a service on system boot
	* For Ubuntu use 
* Install Docker 1.5+ as a dependency
* Install Java 8+ as a dependency
* Register the executable path so the command line functionality works from anywhere
* Install the software as a native package for each Linux version (as a PPA for Ubuntu Linux, for example)
* If multiple Linux versions cannot be covered immediately, focus on installing for Ubuntu Linux first
* Place all program files in the standard locations for each Linux version
* Minimize the amount of installation text the user sees
* Clearly report the cause of installation errors on the screen if they are encountered
