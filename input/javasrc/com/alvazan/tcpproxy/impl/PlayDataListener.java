package com.alvazan.tcpproxy.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.xsoftware.api.nio.channels.Channel;
import biz.xsoftware.api.nio.handlers.DataListener;

import com.alvazan.tcpproxy.api.recorder.PacketDemarcator;
import com.alvazan.tcpproxy.api.recorder.PacketReadListener;
import com.alvazan.tcpproxy.impl.file.Action;
import com.alvazan.tcpproxy.impl.file.Command;

public class PlayDataListener implements DataListener, PacketReadListener {

	private static final Logger log = LoggerFactory.getLogger(PlayDataListener.class);
	
	@Inject
	private RecordingState state;
	private Command connectCmd;
	private PacketDemarcator packetDemarcator;
	private ByteBuffer b = ByteBuffer.allocate(5000);
	
	public void setup(Command c, PacketDemarcator packetDemarcator) {
		this.connectCmd = c;
		this.packetDemarcator = packetDemarcator;
		packetDemarcator.addListener(this);
	}

	@Override
	public void incomingData(Channel channel, ByteBuffer b) throws IOException {
		byte[] data = new byte[b.remaining()];
		b.get(data);
		packetDemarcator.feedMoreData(data);
	}

	@Override
	public void farEndClosed(Channel channel) {
		Command c = connectCmd.copy();
		c.setAddress(null); //address no recorded so wipe that out
		c.setAction(Action.DISCONNECT);
		state.socketClosed(c);
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		log.warn(channel+"failure on channel", e);
	}
	
	@Override
	public void passPartialBytes(byte[] buffer) {
		b.put(buffer);
	}

	@Override
	public void demarcatePacketHere() {
		b.flip();
		Command c = connectCmd.copy();
		c.setAction(Action.WRITE);
		c.setAddress(null);//address is not recorded so wipe that out
		byte[] data = new byte[b.remaining()];
		b.get(data);
		c.setPayload(data);
		c.setPayloadSize(data.length);
		state.socketIncomingData(c);
		
		b.clear();
	}
}
