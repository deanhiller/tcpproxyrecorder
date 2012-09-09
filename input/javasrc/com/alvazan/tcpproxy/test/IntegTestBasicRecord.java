package com.alvazan.tcpproxy.test;

import java.io.File;
import java.io.IOException;

import com.alvazan.tcpproxy.api.recorder.FileOutWrapper;
import com.alvazan.tcpproxy.api.recorder.ProxyCreator;

public class IntegTestBasicRecord extends TestBasicRecord {

	
	@Override
	public void testBasic() throws IOException, InterruptedException {
		super.testBasic();
	}

	@Override
	protected void assertMocksStep3a() {
	}

	@Override
	protected void assertMocksStep3b() {
	}

	@Override
	protected void assertMocksStep2(String cmd) {
	}

	@Override
	protected void assertMocks() {
	}

	@Override
	protected FileOutWrapper createCommandsWrapper(ProxyCreator proxy) {
		File f = new File("output");
		if(!f.exists())
			f.mkdir();
		return proxy.createFile(new File("output/commands.txt"));
	}
	
	@Override
	protected FileOutWrapper createStreamWrapper(ProxyCreator proxy) {
		return proxy.createFile(new File("output/stream.txt"));
	}

}
