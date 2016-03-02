# ioMessage Specification version 1.4 (March 2nd, 2016)

The purpose of a message is move information along a path. No understanding of the contents of the messages should be required in order to help it to its correct destination. The header fields of each message, however, are intended to be read and understood by functional pieces of the iotracks system. Because the data contents of the message format are open, that means each recipient will be required to determine for itself if it understands how to read the data. Recipients can check the information type and information format headers to determine this.

####ID
<pre>
    Data Type: UUID (Universally Unique ID) as a UTF-8 string
    Key: ID
    Required: Yes
    Description: A universally unique identifier per message allows for portability and system-wide verification of events.
</pre>


####Tag
<pre>
    Data Type: Text
    Key: Tag
    Required: No
    Description: This is an open field for associating a message with a particular device or any other interesting thing. It should be queryable later, making this a high-value field for some applications.
</pre>

Message Group ID
    Data Type: UUID
    Key: GroupID
    Required: No
    Description: This is how messages can be allocated to a sequence or stream.

Sequence Number
    Data Type: Integer
    Key: SequenceNumber
    Required: No
    Description: What number in the sequence is this current message?

Sequence Total
    Data Type: Integer
    Key: SequenceTotal
    Required: No
    Description: How many total messages are in the sequence? Absence of a total count while sequence numbers and a message group ID are present may be used to indicate a stream with indeterminate length.

Priority
    Data Type: Integer
    Key: Priority
    Required: No
    Description: The lower the number, the higher the priority. This is a simple quality of service (QoS) indicator. Emergency messages or system error logs might get the highest priority. Self-contained messages (such as a button push or a temperature reading) might get very high priority. Media stream messages (such as one second of audio) might get very low priority ranking in order to allow message slowing or dropping as needed in a busy system.

Timestamp
    Data Type: Integer
    Key: Timestamp
    Required: Yes
    Description: Universal timecode including milliseconds. Milliseconds can be entered as zeroes if needed.

Publisher
    Data Type: UUID
    Key: Publisher
    Required: Yes
    Description: This is the identifier of the track or app element that is sending the message. It can be used to determine routing or guarantee privacy and security. Because each track or app element is assigned a UUID during configuration, even across edge servers no message should be received by an unintended entity.

Authentication Identifier
    Data Type: Text
    Key: AuthID
    Required: No
    Description: This is an open field to pass along authentication information about the particular authorized entity generating the message, such as an employee ID number or a user ID in the application.

Authentication Group
    Data Type: Text
    Key: AuthGroup
    Required: No
    Description: This is an open field to pass authentication group information. This allows pieces of the application to know they are dealing with a message from an authenticated user of a particular type (such as “employee” or “system admin”) without needing to know the actual identification information.


Information Type
    Data Type: Text
    Key: InfoType
    Required: Yes
    Description: This is like a MIME type

Information Format
    Data Type: Text
    Key: InfoFormat
    Required: Yes
    Description: This is a sub-field of the Information Type. It defines the format of the data content in this message. If the information type is “Temperature”, for example, then the information format might be “Degrees Kelvin”.

Context Number of Bytes
    Data Type: Integer
    Key: ContextLength
    Required: No
    Description: What is the byte size of the context content in this message?

Context Data
    Data Type: Binary
    Key: ContextData
    Required: No
    Description: Context data in raw bytes. This field can be used to embed any information desired and will likely be very different from one solution to the next. It is the responsibility of the receiving element(s) to understand the context data format and the meaning of the context information.

Content Number of Bytes
    Data Type: Integer
    Key: ContentLength
    Required: Yes
    Description: What is the size of the data content body in this message?

Data Content
    Data Type: Binary
    Key: ContentData
    Required: Yes
    Description: The actual data content of the message in raw bytes. Having a raw binary data format allows for the lowest overall amount of processing in the system. That’s because much of the data in the IoT world exists in binary, especially at the edge.
