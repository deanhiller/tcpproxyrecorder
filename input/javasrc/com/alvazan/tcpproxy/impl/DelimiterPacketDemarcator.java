package com.alvazan.tcpproxy.impl;

import java.nio.ByteBuffer;

import com.alvazan.tcpproxy.api.recorder.PacketDemarcator;
import com.alvazan.tcpproxy.api.recorder.PacketReadListener;

public class DelimiterPacketDemarcator implements PacketDemarcator {

	private PacketReadListener listener;
	private String delimiter;
	private int delimiterSize;

	public DelimiterPacketDemarcator(String delim) {
		delimiter = delim;
		delimiterSize = delim.length();
	}
	@Override
	public void addListener(PacketReadListener listener) {
		this.listener = listener;
	}

	@Override
	public void feedMoreData(ByteBuffer buffer) {
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		buffer.rewind();
		
		String s = new String(bytes);
		int index = s.indexOf(delimiter);
		if(index < 0) {
			listener.passMoreData(bytes);
			return;
		} else if(index != s.length()-1-delimiterSize)
			throw new RuntimeException("The delimeter is not the last character in the payload so we need to split the payload and feed it to the listener twice");
		
		listener.passMoreData(bytes);
		listener.demarcatePacketHere();		
	}

}
