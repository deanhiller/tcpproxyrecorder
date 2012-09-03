package com.alvazan.tcpproxy.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.alvazan.tcpproxy.api.playback.FileInWrapper;

public class MockInWrapper implements FileInWrapper {

	private String fileContents;

	@Override
	public void close() {
		
	}

	public void setContentsOfFile(String contents) {
		this.fileContents = contents;
	}

	@Override
	public InputStream openInputStream() {
		try {
			InputStream in = new ByteArrayInputStream(fileContents.getBytes("UTF-8"));
			return new MockInput(in);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	private class MockInput extends InputStream {
		private InputStream in;
		public MockInput(InputStream in) {
			this.in = in;
		}
		@Override
		public int read() throws IOException {
			return in.read();
		}
	}
}
