package com.alvazan.tcpproxy.impl.file;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

public class Command {

	private String channelId;
	private Action action;
	private InetSocketAddress address;
	private int payloadSize;
	private boolean needsPlayback;
	private ChannelType type;
	
	private byte[] payload;
	
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
		Action action = Action.valueOf(tokens[0]);
		cmd.setAction(action);
		cmd.setChannel(tokens[1]);
		ChannelType type = ChannelType.translate(tokens[2]);
		cmd.setType(type);
		if(!"null".equals(tokens[3])) {
			String[] hostPort = tokens[3].split(":");
			int port = Integer.parseInt(hostPort[1]);
			InetSocketAddress addr = new InetSocketAddress(hostPort[0], port);
			cmd.setAddress(addr);
		}
		int payloadSize = Integer.parseInt(tokens[4]);
		cmd.setPayloadSize(payloadSize);
		boolean isNeedPlayback = Boolean.parseBoolean(tokens[5]);
		cmd.setNeedsPlayback(isNeedPlayback);
		return cmd;
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

	public void setPayload(byte[] data) {
		this.payload = data;
	}

	public byte[] getPayload() {
		return payload;
	}

	public void validate(List<Command> cmds, Map<String, List<Command>> expected, List<Command> actual) {
		Command cmd = cmds.remove(0);
		if(getPayloadSize() != cmd.getPayloadSize()
		    || getAction() != cmd.getAction()
		    || getType() != cmd.getType())
			throw new IllegalStateException("Non matching commands="+this+" that="+cmd+" expected="+expected+" actual="+actual);
		else if(getAddress() != null && cmd.getAddress() != null) {
			if(getAddress().getPort() != cmd.getAddress().getPort())
				throw new IllegalStateException("Non matching commands="+this+" that="+cmd+" expected="+expected+" actual="+actual);
		} else if(getAddress() == null && cmd.getAddress() != null
				|| (getAddress() != null && cmd.getAddress() == null))
			throw new IllegalStateException("Non matching commands="+this+" that="+cmd+" expected="+expected+" actual="+actual);
	}

	public Command copy() {
		Command c = new Command();
		c.setAction(getAction());
		c.setAddress(getAddress());
		c.setChannel(getChannelId());
		c.setNeedsPlayback(isNeedsPlayback());
		c.setPayload(getPayload());
		c.setPayloadSize(getPayloadSize());
		c.setType(getType());
		return c;
	}
	
}
