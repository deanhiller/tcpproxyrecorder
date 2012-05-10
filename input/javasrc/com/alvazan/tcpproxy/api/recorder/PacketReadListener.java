package com.alvazan.tcpproxy.api.recorder;


public interface PacketReadListener {

	public void passMoreData(byte[] buffer);
	
	public void demarcatePacketHere();
	
}
