# Command Line Specification

As a service intended to run constantly in the background (also known as a daemon), the ioFabric software needs to respond to shell commands from the user. This document defines all of the commands that the software needs to accept and the exact structure of the commands and responses.

The root command is the executable keyword. When using a text editor such as "nano" you simply type "nano xyz.xml" if you want to edit an XML file in the current directory. The executable keyword is "nano" and the parameter that follows is the file to open in the nano editor.

The root command keyword for the ioFabric product is "iofabric" in all lowercase letters. If a user only types "iofabric" they should be presented with the help options displayed as if they typed "iofabric -h" or "iofabric --help" or "iofabric -?" to access the help menu.

####Help Menu

#####Accepted Inputs

* iofabric --help
* iofabric -h
* iofabric -?

#####Output

`Usage: iofabric [OPTIONS]

Option				GNU long option			Meaning
-h, -?				--help					Show this message
-d	<#GB Limit>		--disk=<#GB Limit>		Set the disk consumption limit
-dl <dir>			--disklocation=<dir>	Set the directory to use for disk storage`


####Status

#####Accepted Inputs

* iofabric --help
* iofabric -h
* iofabric -?

#####Output

Usage: iofabric [OPTIONS]