package com.iotracks.iofabric.message_bus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.iotracks.iofabric.utils.configuration.Configuration;

public class MessageArchive {
	private final byte HEADER_SIZE = 33;
	private final short MAXIMUM_MESSAGE_PER_FILE = 1000;

	private final String name;
	private String diskDirectory;
	private String currentFileName;
	private RandomAccessFile indexFile;
	private RandomAccessFile dataFile;
	
	public MessageArchive(String name) {
		this.name = name;
		init();
	}
	
	protected void init() {
		currentFileName = "";
		diskDirectory = Configuration.getDiskDirectory() + "/archive";
		File lastFile = null;
		long lastFileTimestamp = 0;
		final File workingDirectory = new File(diskDirectory);
		if (!workingDirectory.exists())
			workingDirectory.mkdirs();
		
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.substring(name.indexOf(".")).equals(".idx");
			}
		};
		
		for (File file : workingDirectory.listFiles(filter)) {
			if (!file.isFile())
				continue;
			String filename = file.getName();
			if (filename.substring(0, name.length()).equals(name)) {
				String timestampStr = filename.substring(name.length() + 1, filename.indexOf("."));
				long timestamp = Long.parseLong(timestampStr);
				if (timestamp > lastFileTimestamp) {
					lastFileTimestamp = timestamp;
					lastFile = file; 
				}
			}
		}
		
		if (lastFileTimestamp > 0 && lastFile.length() < ((HEADER_SIZE + Long.BYTES) * MAXIMUM_MESSAGE_PER_FILE))
			currentFileName = lastFile.getPath();
	}
	
	private void openFiles(long timestamp) throws FileNotFoundException {
		if (currentFileName.equals(""))
			currentFileName = diskDirectory + "/" + name + "_" + timestamp + ".idx";
		indexFile = new RandomAccessFile(new File(currentFileName), "rw");
		dataFile = new RandomAccessFile(new File(currentFileName.substring(0, currentFileName.indexOf(".")) + ".iomsg"), "rw");
	}
	
	protected void save(byte[] message, long timestamp) {
		if (indexFile == null)
			try {
				openFiles(timestamp);
			} catch (Exception e) {
				e.printStackTrace(System.out);
				return;
			}
		
		try {
			if (indexFile.length() >= ((HEADER_SIZE + Long.BYTES) * MAXIMUM_MESSAGE_PER_FILE)) {
				close();
				openFiles(timestamp);
			}
			indexFile.seek(indexFile.length());
			dataFile.seek(dataFile.length());
			long dataPos = dataFile.getFilePointer();
			
			indexFile.write(message, 0, HEADER_SIZE);
			indexFile.writeLong(dataPos);
			dataFile.write(message, HEADER_SIZE, message.length - HEADER_SIZE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.out);
		}
	}
	
	public void close() throws Exception {
		currentFileName = "";
		if (indexFile != null)
			indexFile.close();
		if (dataFile != null)
			dataFile.close();
		currentFileName = "";
	}
}
