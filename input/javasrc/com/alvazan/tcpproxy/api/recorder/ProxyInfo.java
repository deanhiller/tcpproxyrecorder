package com.alvazan.tcpproxy.api.recorder;

import java.net.InetSocketAddress;


public class ProxyInfo {
	private InetSocketAddress addressToForwardTo;
	private DemarcatorFactory demarcatorFactory;
	private Direction direction;
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
	public Direction getDirection() {
		return direction;
	}
	public void setDirection(Direction direction) {
		this.direction = direction;
	}
	

	
}
