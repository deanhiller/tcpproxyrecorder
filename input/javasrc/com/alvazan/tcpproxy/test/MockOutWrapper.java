package com.alvazan.tcpproxy.test;

import junit.framework.Assert;

import com.alvazan.tcpproxy.api.recorder.FileOutWrapper;

public class MockOutWrapper implements FileOutWrapper {

	private String written = "";
	
	@Override
	public void open() {

	}

	@Override
	public void write(byte[] contents) {
		String s = new String(contents);
		written += s;
	}

	@Override
	public void close() {

	}

	public void assertConnectCmd() {
		Assert.assertEquals("CONNECT,[p][localhost:4445][session 0] ,tcp,localhost:4445,0,true\n", written);
	}

	public void assertSize(int size) {
		Assert.assertEquals(size, written.length());
	}

	public void assertStreamContents() {
		Assert.assertEquals("Didn't match", "connectabctestsplitdhiToClientclientLastabchiToServerserverLastd", written);
	}
	
	public void assertWriteCommandAtEnd() {
		Assert.assertTrue(written.endsWith("WRITE,[p][localhost:4445][session 0] ,tcp,null,10,true\nCONNECT,[p][localhost:7776][session 0] ,tcp,localhost:7776,0,false\n"));
	}

	public void assertSplitCommands() {
		Assert.assertTrue(written.endsWith("WRITE,[p][localhost:7776][session 0] ,tcp,null,10,true\n" +
				"WRITE,[p][localhost:4445][session 0] ,tcp,null,23,false\n" +
				"WRITE,[p][localhost:7776][session 0] ,tcp,null,21,false\n"));
	}

}
