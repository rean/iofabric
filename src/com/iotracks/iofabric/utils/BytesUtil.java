package com.iotracks.iofabric.utils;

import java.nio.ByteBuffer;

public class BytesUtil {
	
	private static ByteBuffer buffer;    

    public static byte[] longToBytes(long x) {
    	buffer = ByteBuffer.allocate(Long.BYTES);
    	buffer.putLong(0, x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
    	if (bytes.length < Long.BYTES)
    		return 0;
    	buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip 
        return buffer.getLong();
    }
    
    public static byte[] integerToBytes(int x) {
    	buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(0, x);
        return buffer.array();
    }

    public static int bytesToInteger(byte[] bytes) {
    	if (bytes.length < Integer.BYTES)
    		return 0;
    	buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip 
        return buffer.getInt();
    }
    
    public static byte[] shortToBytes(short x) {
    	buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.putShort(0, x);
        return buffer.array();
    }

    public static short bytesToShort(byte[] bytes) {
    	if (bytes.length < Short.BYTES)
    		return 0;
    	buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip 
        return buffer.getShort();
    }
    
    public static byte[] doubleToBytes(int x) {
    	buffer = ByteBuffer.allocate(Double.BYTES);
        buffer.putDouble(0, x);
        return buffer.array();
    }

    public static double bytesToDouble(byte[] bytes) {
    	if (bytes.length < Double.BYTES)
    		return 0;
    	buffer = ByteBuffer.allocate(Double.BYTES);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip 
        return buffer.getDouble();
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

}
