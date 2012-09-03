package com.alvazan.tcpproxy.impl.file;

import com.alvazan.tcpproxy.api.recorder.FileOutWrapper;

public interface FileWriter {

	void setFiles(FileOutWrapper cmdFile, FileOutWrapper stream);

	void open();

	void close();

	void writeCommand(Command cmd);

	void addToStream(Command cmd, byte[] buffer);

}
