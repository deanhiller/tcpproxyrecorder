package com.alvazan.tcpproxy.api.recorder;


public interface PacketDemarcator {

	public void addListener(PacketReadListener listener);
	
	public void feedMoreData(byte[] buffer);
	
}
