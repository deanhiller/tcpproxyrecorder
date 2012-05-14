package com.alvazan.tcpproxy.impl.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.alvazan.tcpproxy.api.recorder.FileNotFound;
import com.alvazan.tcpproxy.api.recorder.FileWrapper;

public class FileWrapperImpl implements FileWrapper {

	private File file;
	private FileOutputStream out;

	public FileWrapperImpl(File f) {
		this.file = f;
		
	}

	@Override
	public void open() {
		try {
			out = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			throw new FileNotFound(e.getMessage(), e);
		}
	}

	@Override
	public void write(byte[] contents) {
		try {
			out.write(contents);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		try {
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
