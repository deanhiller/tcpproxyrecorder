package com.alvazan.tcpproxy.impl;

import java.net.InetSocketAddress;

import javax.inject.Inject;
import javax.inject.Provider;

import biz.xsoftware.api.nio.ChannelManager;
import biz.xsoftware.api.nio.Settings;
import biz.xsoftware.api.nio.channels.TCPServerChannel;

import com.alvazan.tcpproxy.api.recorder.ProxyInfo;

public class Proxy {

	@Inject
	private ChannelManager chanMgr;
	@Inject
	private Provider<ServerSockListener> factory;
	private int incomingPort;
	private TCPServerChannel channel;
	private ProxyInfo info;

	public int getIncomingPort() {
		return incomingPort;
	}

	public void setIncomingPort(int incomingPort) {
		this.incomingPort = incomingPort;
	}

	public void start() {
		Settings settings = new Settings(null, null);
		try {
			channel = chanMgr.createTCPServerChannel("port"+incomingPort, settings );
			InetSocketAddress addr = new InetSocketAddress(incomingPort);
			ServerSockListener listener = factory.get();
			listener.setInfo(info);
			listener.setIncomingPort(incomingPort);
			channel.registerServerSocketChannel(listener);
			channel.bind(addr);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void stop() {
		channel.close();
	}

	public void setInfo(ProxyInfo info) {
		this.info = info;
	}

	public ProxyInfo getInfo() {
		return info;
	}
	


}
