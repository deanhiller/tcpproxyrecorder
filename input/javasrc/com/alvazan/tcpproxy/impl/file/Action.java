package com.alvazan.tcpproxy.impl.file;

public enum Action {

	CONNECT("connect"), WRITE("write"), DISCONNECT("disconnect");
	
	private String cmd;

	private Action(String cmd) {
		this.cmd = cmd;
	}
	
	public String getValue() {
		return cmd;
	}
}
