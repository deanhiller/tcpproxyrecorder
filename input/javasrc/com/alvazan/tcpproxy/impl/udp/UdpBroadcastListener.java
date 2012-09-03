package com.alvazan.tcpproxy.impl.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.xsoftware.api.nio.channels.DatagramChannel;
import biz.xsoftware.api.nio.handlers.DatagramListener;

import com.alvazan.tcpproxy.api.recorder.ProxyInfo;

public class UdpBroadcastListener implements DatagramListener {

	private static final Logger log = LoggerFactory.getLogger(UdpBroadcastListener.class);
	
	private ProxyInfo info;

	public void setInfo(ProxyInfo info) {
		this.info = info;
	}

	@Override
	public void incomingData(DatagramChannel channel,
			InetSocketAddress fromAddr, ByteBuffer b) throws IOException {
		
	}

	@Override
	public void failure(DatagramChannel channel, InetSocketAddress fromAddr,
			ByteBuffer data, Throwable e) {
		log.warn(channel+" failure.  fromAddr="+fromAddr, e);
	}
}
