package com.alvazan.tcpproxy.test;

import junit.framework.Assert;

import com.alvazan.tcpproxy.api.recorder.FileWrapper;

public class MockWrapper implements FileWrapper {

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
		Assert.assertEquals("[session 0] ,CONNECT,tcp,0.0.0.0:7775,0,true\n", written);
	}

	public void assertSize(int size) {
		Assert.assertEquals(size, written.length());
	}

	public void assertStreamContents() {
		Assert.assertEquals("connectabctestsplitdhiToClientclientLastabchiToServerserverLastd", written);
	}
	
	public void assertWriteCommandAtEnd() {
		Assert.assertTrue(written.endsWith("[session 0] ,WRITE,tcp,null,10,true\n[session 0] ,CONNECT,tcp,0.0.0.0:7776,0,false\n"));
	}

	public void assertSplitCommands() {
		Assert.assertTrue(written.endsWith("[proxyfor=[session 0] ] ,WRITE,tcp,null,10,true\n" +
				"[proxyfor=[session 0] ] ,WRITE,tcp,null,23,false\n" +
				"[session 0] ,WRITE,tcp,null,21,false\n"));
	}

}
