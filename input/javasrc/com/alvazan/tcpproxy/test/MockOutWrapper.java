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

	public void assertStreamContents2() {
		Assert.assertEquals("Didn't match", "|connect111abc|testsplit2d|hiToClient33|hiToServer44|clientLast5abc|serverLast6d", written);
	}
	
	public void assertStreamContents1() {
		Assert.assertEquals("Didn't match", "|connect111abc|testsplit2d|hiToClient33|clientLast5abc|hiToServer44|serverLast6d", written);
	}
	
	public void assertWriteCommandAtEnd() {
		Assert.assertTrue(written.endsWith("WRITE,[p][localhost:4445][session 0] ,tcp,null,14,true\nCONNECT,[p][localhost:7776][session 0] ,tcp,localhost:7776,0,false\n"));
	}

	public void assertSplitCommands() {
		Assert.assertTrue(written.endsWith("WRITE,[p][localhost:7776][session 0] ,tcp,null,12,true\n" +
				"WRITE,[p][localhost:4445][session 0] ,tcp,null,28,false\n" +
				"WRITE,[p][localhost:7776][session 0] ,tcp,null,26,false\n"));
	}

	public void assertSplitCommands2() {
		Assert.assertTrue(written.endsWith("WRITE,[p][localhost:7776][session 0] ,tcp,null,12,true\n"+
										   "WRITE,[p][localhost:4445][session 0] ,tcp,null,13,false\n"+
										   "WRITE,[p][localhost:7776][session 0] ,tcp,null,13,false\n"+
										   "WRITE,[p][localhost:4445][session 0] ,tcp,null,15,false\n"+
										   "WRITE,[p][localhost:7776][session 0] ,tcp,null,13,false\n"));		
	}

}
