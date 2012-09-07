package com.alvazan.tcpproxy.impl.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import com.alvazan.tcpproxy.api.recorder.FileOutWrapper;
@Singleton
public class FileWriteAggregation implements FileWriter {

	private FileOutWrapper cmdFile;
	private FileOutWrapper stream;

	private Map<String, List<byte[]>> channelToStream = new HashMap<String, List<byte[]>>();
	
	public void open() {
		cmdFile.open();
		stream.open();
	}

	public void close() {
		cmdFile.close();
		stream.close();
	}

	public void setFiles(FileOutWrapper cmdFile, FileOutWrapper stream) {
		this.cmdFile = cmdFile;
		this.stream = stream;
	}

	public void writeCommand(Command cmd) {
		String incomingChannel = cmd.getChannelId();
		byte[] cmdData = cmd.createCommandStr();
		cmdFile.write(cmdData);

		List<byte[]> list = channelToStream.get(incomingChannel);
		if(list != null) {
			for(byte[] data : list) {
				stream.write(data);
			}
			list.clear();
		}
	}

	/**
	 * We have to cache the data coming in so two streams don't get intertwined and instead get written out with the command instead.
	 * 
	 * @param channel
	 * @param buffer
	 * @param cmd 
	 */
	public void addToStream(Command cmd, byte[] buffer) {
		String channel = cmd.getChannelId();
		List<byte[]> list = channelToStream.get(channel);
		if(list == null) {
			list = new ArrayList<byte[]>();
			channelToStream.put(channel, list);
		}
		list.add(buffer);
	}

}
