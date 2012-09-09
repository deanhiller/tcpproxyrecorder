package com.alvazan.tcpproxy.impl.file;

import java.util.HashMap;
import java.util.Map;

public enum ChannelType {

	TCP("tcp"), UDP("udp"), BROADCAST_UDP("broadcast");
	
	private static Map<String, ChannelType> valToType = new HashMap<String, ChannelType>();
	static {
		for(ChannelType t : ChannelType.values()) {
			valToType.put(t.getValue(), t);
		}
	}
	private String value;
	
	private ChannelType(String val) {
		this.value = val;
	}

	public String getValue() {
		return value;
	}
	
	public static ChannelType translate(String val) {
		return valToType.get(val);
	}
	
}
