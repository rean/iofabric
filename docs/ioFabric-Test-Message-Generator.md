# Test Message Generator

As developers build ioElement containers, they start by building the code in a non-container environment. They need to test the code before spending the time turning it into a published container... but they can't actually test the processing of messages or connection to the ioFabric instance without going through the publishing and deployment process.

To facilitate development, we need to have a surrogate version of the ioFabric Local API. It should mimic the API endpoints of the real ioFabric Local API, including offering the control Websocket and message Websocket. It should run on "localhost" so it can be reached directly on the computer that is being used to build the ioElement container.

A developer can precisely mimic the production environment on their build machine by mapping a host. If they map "127.0.0.1" with the host name "iofabric" then their local code will be able to operate with the same "http://iofabric:54321/" endpoints found in the SDKs and described in the API specification.

####Functional Requirements

* Allow the developer to set up a list of fully defined ioMessages that the Test Message Generator will output

* Randomly send ioMessages from the list as output

* Allow the developer to specify the rate of output messages

* Provide the full set of API endpoints specified for the production Local API module of ioFabric (including both Websockets)

* Run as a local server listening on port 54321 just like the production ioFabric Local API

* Log messages that are posted into the Test Message Generator so developers can verify that their message transmission is working properly

* Allow the developer to set up configuration JSON for their ioElement container that the Test Message Generator will give as output

* Allow the developer to specify the rate of transmission of "new configuration" control messages

* Send a "new configuration" control message to the ioElement container at the interval specified by the developer

* Send the complete list of defined ioMessages when the ioElement container makes a request on the "http://iofabric:54321/v2/messages/query" endpoint

