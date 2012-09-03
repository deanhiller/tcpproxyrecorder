package com.alvazan.tcpproxy.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.xsoftware.api.nio.channels.Channel;
import biz.xsoftware.api.nio.handlers.DataListener;

import com.alvazan.tcpproxy.impl.file.Command;

public class PlayDataListener implements DataListener {

	private static final Logger log = LoggerFactory.getLogger(PlayDataListener.class);
	
	@Inject
	private RecordingState state;
	private Command connectCmd;

	public void setConnectCmd(Command c) {
		this.connectCmd = c;
	}

	@Override
	public void incomingData(Channel channel, ByteBuffer b) throws IOException {
		state.socketIncomingData(connectCmd, channel, b);
	}

	@Override
	public void farEndClosed(Channel channel) {
		state.socketClosed(connectCmd, channel);
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		log.warn(channel+"failure on channel", e);
	}
}
