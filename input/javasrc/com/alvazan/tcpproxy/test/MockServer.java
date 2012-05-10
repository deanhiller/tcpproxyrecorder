package com.alvazan.tcpproxy.test;

import java.net.InetSocketAddress;

public class MockServer {

	private InetSocketAddress s;
	private InetSocketAddress s2;

	public MockServer(InetSocketAddress s, InetSocketAddress s2) {
		this.s = s;
		this.s2 = s2;
	}

	public void start() {
		
	}

}
