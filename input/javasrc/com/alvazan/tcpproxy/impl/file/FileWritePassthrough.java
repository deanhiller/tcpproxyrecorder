package com.alvazan.tcpproxy.impl.file;

import javax.inject.Singleton;

import com.alvazan.tcpproxy.api.recorder.FileOutWrapper;
@Singleton
public class FileWritePassthrough implements FileWriter {

	private FileOutWrapper cmdFile;
	private FileOutWrapper stream;

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
		if(cmd.getAction() == Action.WRITE)
			return; //This is done when we write the stream out
		byte[] cmdData = cmd.createCommandStr();
		cmdFile.write(cmdData);
	}

	/**
	 * We have to cache the data coming in so two streams don't get intertwined and instead get written out with the command instead.
	 * 
	 * @param channel
	 * @param buffer
	 * @param cmd 
	 */
	public void addToStream(Command cmd, byte[] buffer) {
		byte[] data = cmd.createCommandStr();
		cmdFile.write(data);
		stream.write(buffer);
	}

}
