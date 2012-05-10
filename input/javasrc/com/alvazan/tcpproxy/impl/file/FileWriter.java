package com.alvazan.tcpproxy.impl.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import biz.xsoftware.api.nio.channels.Channel;

import com.alvazan.tcpproxy.api.recorder.FileWrapper;

public class FileWriter {

	private FileWrapper cmdFile;
	private FileWrapper stream;

	private Map<Channel, List<byte[]>> channelToStream = new HashMap<Channel, List<byte[]>>();
	
	public void open() {
		cmdFile.open();
		stream.open();
	}

	public void close() {
		cmdFile.close();
		stream.close();
	}

	public void setFiles(FileWrapper cmdFile, FileWrapper stream) {
		this.cmdFile = cmdFile;
		this.stream = stream;
	}

	public void writeCommand(Channel incomingChannel, Command cmd) {
		String addr = cmd.getAddress().getHostString();
		int port = cmd.getAddress().getPort();
		String command = "["+cmd.getAction()+","+cmd.getAddress()+","+addr+":"+port+","+cmd.getPayloadSize()+","+cmd.isNeedsPlayback()+"]\n";
		cmdFile.write(command.getBytes());

		List<byte[]> list = channelToStream.get(incomingChannel);
		if(list != null) {
			for(byte[] data : list) {
				stream.write(data);
			}
		}
	}

	/**
	 * We have to cache the data coming in so two streams don't get intertwined and instead get written out with the command instead.
	 * 
	 * @param channel
	 * @param buffer
	 */
	public void addToStream(Channel channel, byte[] buffer) {
		List<byte[]> list = channelToStream.get(channel);
		if(list == null) {
			list = new ArrayList<byte[]>();
			channelToStream.put(channel, list);
		}
		list.add(buffer);
	}

}
