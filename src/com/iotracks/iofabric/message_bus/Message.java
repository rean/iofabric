package com.iotracks.iofabric.message_bus;

import java.io.ByteArrayOutputStream;

import org.bouncycastle.util.Arrays;
import org.json.simple.JSONObject;

import com.iotracks.iofabric.utils.BytesUtil;

public class Message {
	private final short VERSION = 4; 
	
	private String id;
	private String tag;
	private String messageGroupId;
	private int sequenceNumber;
	private int sequenceTotal;
	private byte priority;
	private long timestamp;
	private String publisher;
	private String authIdentifier;
	private String authGroup;
	private short version;
	private long chainPosition;
	private String hash;
	private String previousHash;
	private String nonce;
	private int difficultyTarget;
	private String infoType;
	private String infoFormat;
	private byte[] contextData;
	private byte[] contentData;

	public Message() {
		id = null;
		tag = null;
		messageGroupId = null;
		sequenceNumber = 0;
		sequenceTotal = 0;
		priority = 0;
		timestamp = 0;
		publisher = null;
		authIdentifier = null;
		authGroup = null;
		chainPosition = 0;
		hash = null;
		previousHash = null;
		nonce = null;
		difficultyTarget = 0;
		infoType = null;
		infoFormat = null;
		contentData = null;
		contextData = null;		
	}
	
	// from json
	public Message(JSONObject json) {
		super();
		// TODO: from json
	}
	
	// from rawBytes
	public Message(byte[] rawBytes) {
		super();
		
		version = BytesUtil.bytesToShort(Arrays.copyOfRange(rawBytes, 0, 2));
		if (version != VERSION) {
			// TODO: incompatible version
			return;
		}
		
		int pos = 33;
		
		int size = rawBytes[2];
		if (size > 0) {
			id = BytesUtil.bytesToString(Arrays.copyOfRange(rawBytes, pos, pos + size));
			pos += size;
		}
		
		size = BytesUtil.bytesToShort(Arrays.copyOfRange(rawBytes, 3, 5));
		if (size > 0) {
			tag = BytesUtil.bytesToString(Arrays.copyOfRange(rawBytes, pos, pos + size));
			pos += size;
		}

		size = rawBytes[5];
		if (size > 0) {
			messageGroupId = BytesUtil.bytesToString(Arrays.copyOfRange(rawBytes, pos, pos + size));
			pos += size;
		}

		size = rawBytes[6];
		if (size > 0) {
			sequenceNumber = BytesUtil.bytesToInteger(Arrays.copyOfRange(rawBytes, pos, pos + size));
			pos += size;
		}

		size = rawBytes[7];
		if (size > 0) {
			sequenceTotal = BytesUtil.bytesToInteger(Arrays.copyOfRange(rawBytes, pos, pos + size));
			pos += size;
		}

		size = rawBytes[8];
		if (size > 0) {
			priority = rawBytes[pos];
			pos += size;
		}

		size = rawBytes[9];
		if (size > 0) {
			timestamp = BytesUtil.bytesToLong(Arrays.copyOfRange(rawBytes, pos, pos + size));
			pos += size;
		}

		size = rawBytes[10];
		if (size > 0) {
			publisher = BytesUtil.bytesToString(Arrays.copyOfRange(rawBytes, pos, pos + size));
			pos += size;
		}

		size = BytesUtil.bytesToShort(Arrays.copyOfRange(rawBytes, 11, 13));
		if (size > 0) {
			authIdentifier = BytesUtil.bytesToString(Arrays.copyOfRange(rawBytes, pos, pos + size));
			pos += size;
		}

		size = BytesUtil.bytesToShort(Arrays.copyOfRange(rawBytes, 13, 15));
		if (size > 0) {
			authGroup = BytesUtil.bytesToString(Arrays.copyOfRange(rawBytes, pos, pos + size));
			pos += size;
		}

		size = rawBytes[15];
		if (size > 0) {
			chainPosition = BytesUtil.bytesToLong(Arrays.copyOfRange(rawBytes, pos, pos + size));
			pos += size;
		}

		size = BytesUtil.bytesToShort(Arrays.copyOfRange(rawBytes, 16, 18));
		if (size > 0) {
			hash = BytesUtil.bytesToString(Arrays.copyOfRange(rawBytes, pos, pos + size));
			pos += size;
		}

		size = BytesUtil.bytesToShort(Arrays.copyOfRange(rawBytes, 18, 20));
		if (size > 0) {
			previousHash = BytesUtil.bytesToString(Arrays.copyOfRange(rawBytes, pos, pos + size));
			pos += size;
		}

		size = BytesUtil.bytesToShort(Arrays.copyOfRange(rawBytes, 20, 22));
		if (size > 0) {
			nonce = BytesUtil.bytesToString(Arrays.copyOfRange(rawBytes, pos, pos + size));
			pos += size;
		}

		size = rawBytes[22];
		if (size > 0) {
			difficultyTarget = BytesUtil.bytesToInteger(Arrays.copyOfRange(rawBytes, pos, pos + size));
			pos += size;
		}

		size = rawBytes[23];
		if (size > 0) {
			infoType = BytesUtil.bytesToString(Arrays.copyOfRange(rawBytes, pos, pos + size));
			pos += size;
		}

		size = rawBytes[24];
		if (size > 0) {
			infoFormat = BytesUtil.bytesToString(Arrays.copyOfRange(rawBytes, pos, pos + size));
			pos += size;
		}

		size = BytesUtil.bytesToInteger(Arrays.copyOfRange(rawBytes, 25, 29));
		if (size > 0) {
			contextData = Arrays.copyOfRange(rawBytes, pos, pos + size);
			pos += size;
		}

		size = BytesUtil.bytesToInteger(Arrays.copyOfRange(rawBytes, 29, 33));
		if (size > 0) {
			contentData = Arrays.copyOfRange(rawBytes, pos, pos + size);
		}
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getMessageGroupId() {
		return messageGroupId;
	}
	public void setMessageGroupId(String messageGroupId) {
		this.messageGroupId = messageGroupId;
	}
	public int getSequenceNumber() {
		return sequenceNumber;
	}
	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	public int getSequenceTotal() {
		return sequenceTotal;
	}
	public void setSequenceTotal(int sequenceTotal) {
		this.sequenceTotal = sequenceTotal;
	}
	public byte getPriority() {
		return priority;
	}
	public void setPriority(byte priority) {
		this.priority = priority;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	public String getAuthIdentifier() {
		return authIdentifier;
	}
	public void setAuthIdentifier(String authIdentifier) {
		this.authIdentifier = authIdentifier;
	}
	public String getAuthGroup() {
		return authGroup;
	}
	public void setAuthGroup(String authGroup) {
		this.authGroup = authGroup;
	}
	public short getVersion() {
		return version;
	}
	public void setVersion(short version) {
		this.version = version;
	}
	public long getChainPosition() {
		return chainPosition;
	}
	public void setChainPosition(long chainPosition) {
		this.chainPosition = chainPosition;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public String getPreviousHash() {
		return previousHash;
	}
	public void setPreviousHash(String previousHash) {
		this.previousHash = previousHash;
	}
	public String getNonce() {
		return nonce;
	}
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}
	public int getDifficultyTarget() {
		return difficultyTarget;
	}
	public void setDifficultyTarget(int difficultyTarget) {
		this.difficultyTarget = difficultyTarget;
	}
	public String getInfoType() {
		return infoType;
	}
	public void setInfoType(String infoType) {
		this.infoType = infoType;
	}
	public String getInfoFormat() {
		return infoFormat;
	}
	public void setInfoFormat(String infoFormat) {
		this.infoFormat = infoFormat;
	}
	public byte[] getContextData() {
		return contextData;
	}
	public void setContextData(byte[] contextData) {
		this.contextData = contextData;
	}
	public byte[] getContentData() {
		return contentData;
	}
	public void setContentData(byte[] contentData) {
		this.contentData = contentData;
	}
	
	private int getLength(String str) {
		if (str == null)
			return 0;
		else
			return str.length();
	}
	
	public byte[] getBytes() throws Exception {
		ByteArrayOutputStream headerBaos = new ByteArrayOutputStream();
		ByteArrayOutputStream dataBaos = new ByteArrayOutputStream();
		try {
			//version
			headerBaos.write(BytesUtil.shortToBytes((short) VERSION));

			// id
			int len = getLength(getId());
			headerBaos.write((byte) (len & 0xff));
			if (len > 0)
				dataBaos.write(BytesUtil.stringToBytes(getId()));
			
			// tag
			len = getLength(getTag());
			headerBaos.write(BytesUtil.shortToBytes((short) (len & 0xffff)));
			if (len > 0)
				dataBaos.write(BytesUtil.stringToBytes(getTag()));
			
			//groupid
			len = getLength(getMessageGroupId());
			headerBaos.write((byte) (len & 0xff));
			if (len > 0)
				dataBaos.write(BytesUtil.stringToBytes(getMessageGroupId()));
			
			// seq no
			if (getSequenceNumber() == 0)
				headerBaos.write(0);
			else {
				dataBaos.write(BytesUtil.integerToBytes(getSequenceNumber()));
				headerBaos.write(4);
			}
			
			// seq total
			if (getSequenceTotal() == 0)
				headerBaos.write(0);
			else {
				dataBaos.write(BytesUtil.integerToBytes(getSequenceTotal()));
				headerBaos.write(4);
			}
			
			
			// priority
			if (getPriority() == 0)
				headerBaos.write(0);
			else {
				headerBaos.write(1);
				dataBaos.write(getPriority());
			}
			
			//timestamp
			if (getTimestamp() == 0)
				headerBaos.write(0);
			else {
				headerBaos.write(8);
				dataBaos.write(BytesUtil.longToBytes(getTimestamp()));
			}
			
			// publisher
			len = getLength(getPublisher());
			headerBaos.write((byte) (len & 0xff));
			if (len > 0)
				dataBaos.write(BytesUtil.stringToBytes(getPublisher()));

			// authIdentifier
			len = getLength(getAuthIdentifier());
			headerBaos.write(BytesUtil.shortToBytes((short) (len & 0xffff)));
			if (len > 0)
				dataBaos.write(BytesUtil.stringToBytes(getAuthIdentifier()));

			// authGroup
			len = getLength(getAuthGroup());
			headerBaos.write(BytesUtil.shortToBytes((short) (len & 0xffff)));
			if (len > 0)
				dataBaos.write(BytesUtil.stringToBytes(getAuthGroup()));
			
			// chainPosition
			if (getChainPosition() == 0)
				headerBaos.write(0);
			else {
				headerBaos.write(8);
				dataBaos.write(BytesUtil.longToBytes(getChainPosition()));
			}
			
			// hash
			len = getLength(getHash());
			headerBaos.write(BytesUtil.shortToBytes((short) (len & 0xffff)));
			if (len > 0)
				dataBaos.write(BytesUtil.stringToBytes(getHash()));

			// previousHash
			len = getLength(getPreviousHash());
			headerBaos.write(BytesUtil.shortToBytes((short) (len & 0xffff)));
			if (len > 0)
				dataBaos.write(BytesUtil.stringToBytes(getPreviousHash()));

			// nonce
			len = getLength(getNonce());
			headerBaos.write(BytesUtil.shortToBytes((short) (len & 0xffff)));
			if (len > 0) 
				dataBaos.write(BytesUtil.stringToBytes(getNonce()));
				
			// difficultyTarget
			if (getDifficultyTarget() == 0)
				headerBaos.write(0);
			else {
				headerBaos.write(4);
				dataBaos.write(BytesUtil.integerToBytes(getDifficultyTarget()));
			}

			// infoType
			len = getLength(getInfoType());
			headerBaos.write((byte) (len & 0xff));
			if (len > 0)
				dataBaos.write(BytesUtil.stringToBytes(getInfoType()));

			// infoFormat
			len = getLength(getInfoFormat());
			headerBaos.write((byte) (len & 0xff));
			if (len > 0)
				dataBaos.write(BytesUtil.stringToBytes(getInfoFormat()));
			
			// contextData
			if (getContextData() == null)
				headerBaos.write(BytesUtil.integerToBytes(0));
			else {
				headerBaos.write(BytesUtil.integerToBytes(getContextData().length));
				dataBaos.write(getContextData());
			}
			
			// contentData
			if (getContentData() == null)
				headerBaos.write(BytesUtil.integerToBytes(0));
			else {
				headerBaos.write(BytesUtil.integerToBytes(getContentData().length));
				dataBaos.write(getContentData());
			}
			
		} catch (Exception e) {
			throw e;
		}
		
//		byte[] header = headerBaos.toByteArray();
//		byte[] data = dataBaos.toByteArray();
//		byte[] result = new byte[header.length + data.length];
//		for (int i = 0; i < header.length; i++)
//			result[i] = header[i];
//		for (int i = 0; i < data.length; i++)
//			result[header.length + i] = data[i];
//		return result;
		return Arrays.concatenate(headerBaos.toByteArray(), dataBaos.toByteArray());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		JSONObject result = new JSONObject();
		result.put("id", id);
		result.put("tag", tag);
		result.put("messageGroupId", messageGroupId);
		result.put("sequenceNumber", sequenceNumber);
		result.put("sequenceTotal", sequenceTotal);
		result.put("priority", priority);
		result.put("timestamp", timestamp);
		result.put("publisher", publisher);
		result.put("authenticationIdentifier", authIdentifier);
		result.put("authenticationGroup", authGroup);
		result.put("version", version);
		result.put("chainPosition", chainPosition);
		result.put("hash", hash);
		result.put("previousMessageHash", previousHash);
		result.put("nonce", nonce);
		result.put("difficultyTarget", difficultyTarget);
		result.put("informationType", infoType);
		result.put("informationFormat", infoFormat);
		result.put("contextData", BytesUtil.byteArrayToString(contextData));
		result.put("contentData", BytesUtil.byteArrayToString(contentData));
		
		return result.toJSONString();
	}
	
	public static void main(String[] args) throws Exception {
		Message m = new Message();
		m.setId("AA");
		m.setTag("BB");
		m.setMessageGroupId("CC");
		m.setSequenceNumber(1);
		m.setSequenceTotal(2);
		m.setPriority((byte) 3);
		m.setTimestamp(4);
		m.setPublisher("DD");
		m.setAuthIdentifier("EE");
		m.setAuthGroup("FF");
		m.setChainPosition(5);
		m.setHash("GG");
		m.setPreviousHash("HH");
		m.setNonce("II");
		m.setDifficultyTarget(6);
		m.setInfoType("JJ");
		m.setInfoFormat("KK");
		m.setContextData(new byte[] {7, 7, 7, 7, 7});
		m.setContentData(new byte[] {8, 8, 8, 8, 8});
		
		byte[] b = m.getBytes();
		System.out.println(b.length);
		System.out.println();
		
		Message n = new Message(b);

		String s = n.toString();
		System.out.println(s);
	}
}
