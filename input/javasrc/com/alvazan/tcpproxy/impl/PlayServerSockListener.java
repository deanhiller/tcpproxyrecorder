package com.alvazan.tcpproxy.impl;

import java.io.IOException;
import java.net.InetSocketAddress;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.xsoftware.api.nio.channels.RegisterableChannel;
import biz.xsoftware.api.nio.channels.TCPChannel;
import biz.xsoftware.api.nio.handlers.ConnectionListener;

import com.alvazan.tcpproxy.api.recorder.DemarcatorFactory;
import com.alvazan.tcpproxy.impl.file.Command;

public class PlayServerSockListener implements ConnectionListener {

	private static final Logger log = LoggerFactory.getLogger(PlayServerSockListener.class);
	
	@Inject
	private RecordingState state;
	@Inject
	private Provider<PlayDataListener> dataListFactory;
	
	private Command cmd;
	private DemarcatorFactory demFactory;
	
	public void setConnectCmd(Command cmd, DemarcatorFactory demFactory) {
		this.cmd = cmd;
		this.demFactory = demFactory;
	}

	@Override
	public void connected(TCPChannel channel) throws IOException {
		state.socketConnected(cmd, channel);
		PlayDataListener dataListener = dataListFactory.get();
		dataListener.setup(cmd, demFactory.createDemarcator(cmd.getChannelId()));
		try {
			channel.registerForReads(dataListener);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		state.putChannel(cmd, channel);
	}

	@Override
	public void connectFailed(RegisterableChannel channel, Throwable e) {
		log.warn("connection failure", e);
	}
}
