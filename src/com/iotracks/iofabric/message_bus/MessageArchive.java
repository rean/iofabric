package com.iotracks.iofabric.message_bus;

import java.io.File;
import java.io.FilenameFilter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import com.iotracks.iofabric.utils.BytesUtil;
import com.iotracks.iofabric.utils.configuration.Configuration;

public class MessageArchive {
	private final byte HEADER_SIZE = 33;
	private final short MAXIMUM_MESSAGE_PER_FILE = 1000;
	private final int MAXIMUM_ARCHIVE_SIZE_MB = 1;

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
		diskDirectory = Configuration.getDiskDirectory() + "messages/archive/";
		
		File lastFile = null;
		long lastFileTimestamp = 0;
		final File workingDirectory = new File(diskDirectory);
		if (!workingDirectory.exists())
			workingDirectory.mkdirs();
		
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String fileName) {
				return fileName.substring(0, name.length()).equals(name) && fileName.substring(fileName.indexOf(".")).equals(".idx");
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
	
	private void openFiles(long timestamp) throws Exception {
		if (currentFileName.equals(""))
			currentFileName = diskDirectory + name + "_" + timestamp + ".idx";
		indexFile = new RandomAccessFile(new File(currentFileName), "rw");
		dataFile = new RandomAccessFile(new File(currentFileName.substring(0, currentFileName.indexOf(".")) + ".iomsg"), "rw");
	}
	
	protected void save(byte[] message, long timestamp) throws Exception {
		if (indexFile == null)
			openFiles(timestamp);
		
//		if (indexFile.length() >= ((HEADER_SIZE + Long.BYTES) * MAXIMUM_MESSAGE_PER_FILE)) {
		if ((message.length + dataFile.length()) >= (MAXIMUM_ARCHIVE_SIZE_MB * 1024 * 1024)) {
			close();
			openFiles(timestamp);
		}
		indexFile.seek(indexFile.length());
		dataFile.seek(dataFile.length());
		long dataPos = dataFile.getFilePointer();
		
		indexFile.write(message, 0, HEADER_SIZE);
		indexFile.writeLong(dataPos);
		dataFile.write(message, HEADER_SIZE, message.length - HEADER_SIZE);
	}
	
	public void close() {
		try {
			currentFileName = "";
			if (indexFile != null)
				indexFile.close();
			if (dataFile != null)
				dataFile.close();
			currentFileName = "";
		} catch (Exception e) {}
	}
	
	private int getDataSize(byte[] header) {
		int size = 0;
		size = header[2];
		size += BytesUtil.bytesToShort(BytesUtil.copyOfRange(header, 3, 5));
		size += header[5];
		size += header[6];
		size += header[7];
		size += header[8];
		size += header[9];
		size += header[10];
		size += BytesUtil.bytesToShort(BytesUtil.copyOfRange(header, 11, 13));
		size += BytesUtil.bytesToShort(BytesUtil.copyOfRange(header, 13, 15));
		size += header[15];
		size += BytesUtil.bytesToShort(BytesUtil.copyOfRange(header, 16, 18));
		size += BytesUtil.bytesToShort(BytesUtil.copyOfRange(header, 18, 20));
		size += BytesUtil.bytesToShort(BytesUtil.copyOfRange(header, 20, 22));
		size += header[22];
		size += header[23];
		size += header[24];
		size += BytesUtil.bytesToInteger(BytesUtil.copyOfRange(header, 25, 29));
		size += BytesUtil.bytesToInteger(BytesUtil.copyOfRange(header, 29, 33));
		return size;
	}

	public List<Message> messageQuery(long from, long to) {
		List<Message> result = new ArrayList<>();
		
		File workingDirectory = new File(diskDirectory);
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String fileName) {
				return fileName.substring(0, name.length()).equals(name) && fileName.substring(fileName.indexOf(".")).equals(".idx");
			}
		};
		File[] listOfFiles = workingDirectory.listFiles(filter);
		Arrays.sort(listOfFiles);
		
		Stack<File> resultSet = new Stack<>();
		int i = listOfFiles.length - 1;
		for (; i >= 0; i--) {
			File file = listOfFiles[i];
			if (!file.isFile())
				continue;
			long timestamp = Long.parseLong(file.getName().substring(name.length() + 1, file.getName().indexOf(".")));
			if (timestamp < from)
				break;
			if (timestamp >= from && timestamp <= to)
				resultSet.push(file);
		}
		if (i >= 0)
			resultSet.push(listOfFiles[i]);
		
		while (!resultSet.isEmpty()) {
			File file = resultSet.pop();
			String fileName = file.getName();
			try {
				RandomAccessFile indexFile = new RandomAccessFile(new File(diskDirectory + fileName), "r");
				RandomAccessFile dataFile = new RandomAccessFile(new File(diskDirectory + fileName.substring(0, fileName.indexOf(".")) + ".iomsg"), "r");
				while (indexFile.getFilePointer() < indexFile.length()) {
					byte[] header = new byte[HEADER_SIZE];
//					byte[] dataPosBytes = new byte[Long.BYTES];
					
					indexFile.read(header, 0, HEADER_SIZE);
//					long dataPos = indexFile.readLong();
					int dataSize = getDataSize(header);
					byte[] data = new byte[dataSize];
					dataFile.read(data, 0, dataSize);
					Message message = new Message(header, data);
					if (message.getTimestamp() < from)
						continue;
					result.add(message);
				}
				indexFile.close();
				dataFile.close();
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}
		
		return result;
	}
}
