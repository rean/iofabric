package com.iotracks.iofabric.mock;

public class IOMessage {
	private final String _id;
    private String _tag;
    private String groupId;
    private int sequenceNumber;
    private int sequenceTotal;
    private int priority;
    private long timestamp;
    private String publisher;
    private String authId;
    private String authGroup;
    private String infoType;
    private String infoFormat;
    private byte[] contextData;
    private byte[] contentData;

    public IOMessage(String id) {
        this._id = id;
    }

    public void setTag(String tag) {
        this._tag = tag;
    }

    public String getID() {
        return _id;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public void setSequenceTotal(int sequenceTotal) {
        this.sequenceTotal = sequenceTotal;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }


    public void setAuthId(String authId) {
        this.authId = authId;
    }


    public void setAuthGroup(String authGroup) {
        this.authGroup = authGroup;
    }

    public void setInfoType(String infoType) {
        this.infoType = infoType;
    }

    public void setInfoFormat(String infoFormat) {
        this.infoFormat = infoFormat;
    }

    public void setContextData(byte[] contextData) {
        this.contextData = contextData;
    }

    public int getContextLength() {
        if(this.contextData == null) return 0;
        return this.contextData.length;
    }


    public void setContentData(byte[] contentData) {
        this.contentData = contentData;
    }

    public int getContentLength() {
        if(this.contentData == null) return 0;
        return this.contentData.length;
    }
}
