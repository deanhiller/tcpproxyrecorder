package com.alvazan.tcpproxy.impl.tcp;

import com.alvazan.tcpproxy.api.recorder.DemarcatorFactory;
import com.alvazan.tcpproxy.api.recorder.PacketDemarcator;

public class DelimiterDemarcatorFactory implements DemarcatorFactory {

	private String delimeter;

	public DelimiterDemarcatorFactory(String delimeter) {
		this.delimeter=delimeter;
	}
	@Override
	public PacketDemarcator createDemarcator(String id) {
		return new DelimiterPacketDemarcator(delimeter);
	}

}
