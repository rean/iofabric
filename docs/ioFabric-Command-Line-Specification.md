# Command Line Specification

As a service intended to run constantly in the background (also known as a daemon), the ioFabric software needs to respond to shell commands from the user. This document defines all of the commands that the software needs to accept and the exact structure of the commands and responses.

The root command is the executable keyword. When using a text editor such as "nano" you simply type "nano xyz.xml" if you want to edit an XML file in the current directory. The executable keyword is "nano" and the parameter that follows is the file to open in the nano editor.

The root command keyword for the ioFabric product is "iofabric" in all lowercase letters. If a user only types "iofabric" they should be presented with the help options displayed as if they typed "iofabric -h" or "iofabric --help" or "iofabric -?" to access the help menu.

####Help Menu

#####Accepted Inputs

<pre>
iofabric --help
iofabric -h
iofabric -?
</pre>

#####Output

<pre>
Usage: iofabric [OPTIONS]

Option				GNU long option			Meaning
-h, -?				--help					Show this message
-d	&lt;#GB Limit&gt;		--disk=&lt;#GB Limit&gt;		Set the disk consumption limit
-dl &lt;dir&gt;			--disklocation=&lt;dir&gt;	Set the directory to use for disk storage
</pre>


####Display ioFabric Status

#####Accepted Inputs

* `iofabric status`

#####Output

`TBD`


####Start ioFabric

#####Accepted Inputs

* `iofabric start`

#####Output

`TBD`


####Stop ioFabric

#####Accepted Inputs

* `iofabric stop`

#####Output

`TBD`



####Restart ioFabric

#####Accepted Inputs

* `iofabric restart`

#####Output

`TBD`



####Provision this ioFabric instance to a controller 

#####Accepted Inputs

* `iofabric provision D98we4sd`
* The provisioning key entered by the user takes the place of the D98we4sd above

#####Output

`TBD`



####De-provision this ioFabric instance (removed from any controller)

#####Accepted Inputs

* `iofabric deprovision`

#####Output

`TBD`



####Show ioFabric information

#####Accepted Inputs

* `iofabric info`

#####Output

`TBD`



####Change ioFabric configuration

#####Accepted Inputs

* `iofabric config -d 17.5`
* `iofabric config -dl ~/temp/spool/`
* `iofabric config -m 568`
* `iofabric config -p 82.0`
* `iofabric config -a https://250.17.0.200/controllers/7/`
* `iofabric config -ac ~/temp/certs/controller_identity_proof.crt`
* `iofabric config -c unix:///var/run/docker.sock`
* `iofabric config -n eth0`

#####Output

`TBD`

