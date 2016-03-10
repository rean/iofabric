package com.iotracks.iofabric.utils;

import java.nio.ByteBuffer;

public class BytesUtil {
	
    public static byte[] longToBytes(long x) {
    	ByteBuffer buffer;    
    	buffer = ByteBuffer.allocate(Long.BYTES);
    	buffer.putLong(0, x);
    	byte[] result = buffer.array();
    	buffer.clear();
        return result;
    }

    public static long bytesToLong(byte[] bytes) {
    	if (bytes.length < Long.BYTES)
    		return 0;
    	ByteBuffer buffer;    
    	buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip
        long result = buffer.getLong();
        buffer.clear();
        return result;
    }
    
    public static byte[] integerToBytes(int x) {
    	ByteBuffer buffer;    
    	buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(0, x);
    	byte[] result = buffer.array();
    	buffer.clear();
        return result;
    }

    public static int bytesToInteger(byte[] bytes) {
    	if (bytes.length < Integer.BYTES)
    		return 0;
    	ByteBuffer buffer;    
    	buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip
        int result = buffer.getInt();
        buffer.clear();
        return result;
    }
    
    public static byte[] shortToBytes(short x) {
    	ByteBuffer buffer;    
    	buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.putShort(0, x);
    	byte[] result = buffer.array();
    	buffer.clear();
        return result;
    }

    public static short bytesToShort(byte[] bytes) {
    	if (bytes.length < Short.BYTES)
    		return 0;
    	ByteBuffer buffer;    
    	buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip
        short result = buffer.getShort();
        buffer.clear();
        return result;
    }
    
    public static byte[] doubleToBytes(int x) {
    	ByteBuffer buffer;    
    	buffer = ByteBuffer.allocate(Double.BYTES);
        buffer.putDouble(0, x);
    	byte[] result = buffer.array();
    	buffer.clear();
        return result;
    }

    public static double bytesToDouble(byte[] bytes) {
    	if (bytes.length < Double.BYTES)
    		return 0;
    	ByteBuffer buffer;    
    	buffer = ByteBuffer.allocate(Double.BYTES);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip
        double result = buffer.getDouble();
        buffer.clear();
        return result;
    }
    
    public static byte[] stringToBytes(String s) {
    	if (s == null)
    		return new byte[] {};
    	else
    		return s.getBytes();
    }
    
    public static String bytesToString(byte[] bytes) {
        return new String(bytes);
    }

    public static String byteArrayToString(byte[] bytes) {
    	StringBuilder result = new StringBuilder();
    	
		result.append("[");
    	for (byte b : bytes) {
    		if (result.length() > 1)
    			result.append(", ");
    		result.append(b);
    	}
		result.append("]");
    	
    	return result.toString();
    }
}
