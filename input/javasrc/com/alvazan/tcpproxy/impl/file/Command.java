package com.alvazan.tcpproxy.impl.file;

import java.net.InetSocketAddress;

import biz.xsoftware.api.nio.channels.Channel;

public class Command {

	private Channel channel;
	private Action action;
	private InetSocketAddress address;
	private long payloadSize;
	private boolean needsPlayback;
	private ChannelType type;
	
	public Command(Channel channel, ChannelType type, Action action, InetSocketAddress address,
			long payloadSize, boolean needsPlayback) {
		super();
		this.channel = channel;
		this.action = action;
		this.address = address;
		this.payloadSize = payloadSize;
		this.needsPlayback = needsPlayback;
		this.type = type;
	}

	public ChannelType getType() {
		return type;
	}
	public void setType(ChannelType type) {
		this.type = type;
	}
	public Channel getChannel() {
		return channel;
	}
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	public Action getAction() {
		return action;
	}
	public void setAction(Action command) {
		this.action = command;
	}
	public InetSocketAddress getAddress() {
		return address;
	}
	public void setAddress(InetSocketAddress address) {
		this.address = address;
	}
	public long getPayloadSize() {
		return payloadSize;
	}
	public void setPayloadSize(long payloadSize) {
		this.payloadSize = payloadSize;
	}

	public boolean isNeedsPlayback() {
		return needsPlayback;
	}

	public void setNeedsPlayback(boolean needsPlayback) {
		this.needsPlayback = needsPlayback;
	}

	
}
