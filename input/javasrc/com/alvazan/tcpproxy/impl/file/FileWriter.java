package com.alvazan.tcpproxy.impl.file;

import biz.xsoftware.api.nio.channels.Channel;

import com.alvazan.tcpproxy.api.recorder.FileWrapper;

public class FileWriter {

	private FileWrapper cmdFile;
	private FileWrapper stream;

	private Channel currentChannel;
	
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

	public void writeCommand(Command cmd) {
		String addr = cmd.getAddress().getHostString();
		int port = cmd.getAddress().getPort();
		String command = "["+cmd.getAction()+","+cmd.getAddress()+","+addr+":"+port+","+cmd.getPayloadSize()+","+cmd.isNeedsPlayback()+"]\n";
		cmdFile.write(command.getBytes());
		
		currentChannel = null;
	}

	public void addToStream(Channel incomingChannel, byte[] buffer) {
		if(currentChannel != null && currentChannel != incomingChannel) {
			throw new IllegalStateException("We do not support two sockets both writing data at the same time unless we rewrite to support multiple sockets.  please let us know");
		}
		
		stream.write(buffer);
	}

}
