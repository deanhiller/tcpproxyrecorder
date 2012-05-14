package com.alvazan.tcpproxy.impl.file;

public enum ChannelType {

	TCP("tcp"), UDP("udp"), BROADCAST_UDP("broadcast");
	
	private String value;
	
	private ChannelType(String val) {
		this.value = val;
	}

	public String getValue() {
		return value;
	}
	
}
