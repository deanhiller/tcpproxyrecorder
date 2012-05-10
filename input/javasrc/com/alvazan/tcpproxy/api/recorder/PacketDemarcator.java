package com.alvazan.tcpproxy.api.recorder;

import java.nio.ByteBuffer;

public interface PacketDemarcator {

	public void addListener(PacketReadListener listener);
	
	public void feedMoreData(ByteBuffer buffer);
	
}
