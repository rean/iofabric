package com.iotracks.iofabric.utils;

import java.io.File;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.StandardOpenOption;

public class MemoryMappedFileService {
	
	private final int BUFFER_SIZE = 10240;

	public String getString(String filename) throws Exception {
		File mapFile = new File(filename);
		mapFile.deleteOnExit();

		FileChannel channel = FileChannel.open(mapFile.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE,
				StandardOpenOption.CREATE);

		MappedByteBuffer buffer = channel.map(MapMode.READ_WRITE, 0, BUFFER_SIZE);
		CharBuffer charBuf = buffer.asCharBuffer();

		char c;
		String string = "";
		while ((c = charBuf.get()) != 0) {
			string += c;
		}
		
		charBuf.put(0, '\0');
		channel.close();
		return string;
	}

	public void sendString(String filename, String str) throws Exception {
		File mapFile = new File(filename);
		mapFile.deleteOnExit();
		if (mapFile.exists())
			mapFile.delete();

		FileChannel channel = FileChannel.open(mapFile.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE,
				StandardOpenOption.CREATE);

		MappedByteBuffer buffer = channel.map(MapMode.READ_WRITE, 0, BUFFER_SIZE);
		CharBuffer charBuf = buffer.asCharBuffer();

		char[] string = (str + "\0").toCharArray();
		charBuf.put(string);

		while (charBuf.get(0) != '\0')
			;
		channel.close();
	}

}
