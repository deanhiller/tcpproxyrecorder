package com.alvazan.tcpproxy.impl.udp;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import javax.inject.Inject;
import javax.inject.Provider;

import biz.xsoftware.api.nio.ChannelManager;
import biz.xsoftware.api.nio.channels.DatagramChannel;

import com.alvazan.tcpproxy.api.recorder.ProxyInfo;

public class UdpProxy {

	@Inject
	private ChannelManager chanMgr;
	@Inject
	private Provider<UdpBroadcastListener> factory;
	private ProxyInfo info;
	private int bufferSize;
	private DatagramChannel channel;
	private DatagramChannel proxyChannel;

	public void setInfo(ProxyInfo info) {
		this.info = info;
	}

	public void start() {
		try {
			int port = info.getIncomingAddress().getPort();
			int outPort = info.getAddressToForwardTo().getPort();
			channel = chanMgr.createDatagramChannel("udpPort"+port+"To"+outPort, bufferSize);
			proxyChannel = chanMgr.createDatagramChannel("udpProxy"+port+"To"+outPort, bufferSize);
			
			UdpBroadcastListener listener = factory.get();
			//listener.setSendChannel(info.getAddressToForwardTo());

			UdpBroadcastListener proxyListener = factory.get();
			listener.setInfo(info);
			
			
			
			
			channel.bind(info.getIncomingAddress());
			channel.registerForReads(listener);
			
			//since this is udp, I need to bind another udp port that server can respond to.
			InetSocketAddress addr = new InetSocketAddress(InetAddress.getLocalHost(), 0);
			proxyChannel.bind(addr);
			proxyChannel.registerForReads(proxyListener);
			
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}

	public void stop() {
		channel.close();
		proxyChannel.close();
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

}
