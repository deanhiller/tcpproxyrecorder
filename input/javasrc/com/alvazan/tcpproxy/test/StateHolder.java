package com.alvazan.tcpproxy.test;

import biz.xsoftware.api.nio.channels.Channel;

public class StateHolder {

	private String data ="";
	private Channel channel;

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setRemoteChannel(Channel channel2) {
		this.channel = channel2;
	}

	public Channel getRemoteChannel() {
		return channel;
	}
	
	
}
