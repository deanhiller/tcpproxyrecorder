package com.alvazan.tcpproxy.api.recorder;


public interface PacketReadListener {

	public void passPartialBytes(byte[] buffer);
	
	public void demarcatePacketHere();
	
}
