package com.alvazan.tcpproxy.api.recorder;

import java.net.InetSocketAddress;


public class ProxyInfo {
	private InetSocketAddress incomingAddress;
	private InetSocketAddress addressToForwardTo;
	private DemarcatorFactory demarcatorFactory;
	private RecordingDirection direction;
	
	public InetSocketAddress getIncomingAddress() {
		return incomingAddress;
	}
	public void setIncomingAddress(InetSocketAddress incomingAddress) {
		this.incomingAddress = incomingAddress;
	}
	public InetSocketAddress getAddressToForwardTo() {
		return addressToForwardTo;
	}
	public void setAddressToForwardTo(InetSocketAddress addressToForwardTo) {
		this.addressToForwardTo = addressToForwardTo;
	}
	
	public DemarcatorFactory getDemarcatorFactory() {
		return demarcatorFactory;
	}
	public void setDemarcatorFactory(DemarcatorFactory toServerDemarcator) {
		this.demarcatorFactory = toServerDemarcator;
	}
	public RecordingDirection getDirection() {
		return direction;
	}
	public void setDirection(RecordingDirection direction) {
		this.direction = direction;
	}
	

	
}
