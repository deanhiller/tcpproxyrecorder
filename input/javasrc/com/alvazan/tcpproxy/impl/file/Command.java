package com.alvazan.tcpproxy.impl.file;

import java.net.InetSocketAddress;

public class Command {

	private String channelId;
	private Action action;
	private InetSocketAddress address;
	private int payloadSize;
	private boolean needsPlayback;
	private ChannelType type;
	
	public Command() {}
	
	public Command(String channelId, ChannelType type, Action action, InetSocketAddress address,
			int payloadSize, boolean needsPlayback) {
		super();
		this.channelId = channelId;
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
	public String getChannelId() {
		return channelId;
	}
	public void setChannel(String channel) {
		this.channelId = channel;
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
	public int getPayloadSize() {
		return payloadSize;
	}
	public void setPayloadSize(int payloadSize) {
		this.payloadSize = payloadSize;
	}

	public boolean isNeedsPlayback() {
		return needsPlayback;
	}

	public void setNeedsPlayback(boolean needsPlayback) {
		this.needsPlayback = needsPlayback;
	}

	public static Command parse(String line) {
		
		String[] tokens = line.split(",");
		Command cmd = new Command();
		
		return null;
	}

	
	@Override
	public String toString() {
		String address = "null";
		if(getAddress() != null) {
			String addr = getAddress().getHostName();
			int port = getAddress().getPort();
			address = addr+":"+port;
		}
		
		String type = getType().getValue();
		String command = getAction()+","+channelId+","+type+","+address+","+getPayloadSize()+","+isNeedsPlayback()+"\n";
		return command;
	}

	public byte[] createCommandStr() {
		byte[] cmdData = this.toString().getBytes();
		return cmdData;
	}
	
}
