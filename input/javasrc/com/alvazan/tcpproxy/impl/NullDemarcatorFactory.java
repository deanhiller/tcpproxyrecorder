package com.alvazan.tcpproxy.impl;

import com.alvazan.tcpproxy.api.recorder.DemarcatorFactory;
import com.alvazan.tcpproxy.api.recorder.PacketDemarcator;
import com.alvazan.tcpproxy.api.recorder.PacketReadListener;

public class NullDemarcatorFactory implements DemarcatorFactory {

	@Override
	public PacketDemarcator createDemarcator(String id) {
		return new NullPacketDemarcator();
	}
	private static class NullPacketDemarcator implements PacketDemarcator {
		private PacketReadListener listener;

		@Override
		public void addListener(PacketReadListener listener) {
			this.listener = listener;
		}

		@Override
		public void feedMoreData(byte[] buffer) {
			listener.passMoreData(buffer);
			listener.demarcatePacketHere();
		}
	}
}
