package com.alvazan.tcpproxy.impl;

import java.io.IOException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.xsoftware.api.nio.channels.RegisterableChannel;
import biz.xsoftware.api.nio.channels.TCPChannel;
import biz.xsoftware.api.nio.handlers.ConnectionListener;

import com.alvazan.tcpproxy.impl.file.Command;

public class PlayServerSockListener implements ConnectionListener {

	private static final Logger log = LoggerFactory.getLogger(PlayServerSockListener.class);
	
	@Inject
	private RecordingState state;
	
	private Command cmd;
	
	public void setConnectCmd(Command cmd) {
		this.cmd = cmd;
	}

	@Override
	public void connected(TCPChannel channel) throws IOException {
		state.socketConnected(cmd, channel);
	}

	@Override
	public void connectFailed(RegisterableChannel channel, Throwable e) {
		log.warn("connection failure", e);
	}
}
