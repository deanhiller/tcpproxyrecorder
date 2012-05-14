package com.alvazan.tcpproxy.test;

import java.io.File;

import com.alvazan.tcpproxy.api.recorder.FileWrapper;
import com.alvazan.tcpproxy.api.recorder.TCPProxy;

public class IntegTestBasic extends TestBasic {

	@Override
	protected void assertMocksStep3() {
	}

	@Override
	protected void assertMocksStep2(String cmd) {
	}

	@Override
	protected void assertMocks() {
	}

	@Override
	protected FileWrapper createCommandsWrapper(TCPProxy proxy) {
		File f = new File("output");
		if(!f.exists())
			f.mkdir();
		return proxy.createFile(new File("output/commands.txt"));
	}
	
	@Override
	protected FileWrapper createStreamWrapper(TCPProxy proxy) {
		return proxy.createFile(new File("output/stream.txt"));
	}

}
