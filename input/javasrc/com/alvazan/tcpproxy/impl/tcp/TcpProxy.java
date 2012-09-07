package com.alvazan.tcpproxy.impl.tcp;

import java.net.InetSocketAddress;

import javax.inject.Inject;
import javax.inject.Provider;

import biz.xsoftware.api.nio.ChannelManager;
import biz.xsoftware.api.nio.Settings;
import biz.xsoftware.api.nio.channels.Channel;
import biz.xsoftware.api.nio.channels.TCPServerChannel;

import com.alvazan.tcpproxy.api.recorder.ProxyInfo;
import com.alvazan.tcpproxy.api.recorder.RecordingDirection;

public class TcpProxy {

	@Inject
	private ChannelManager chanMgr;
	@Inject
	private ServerSockListener listener;
	private TCPServerChannel channel;
	private ProxyInfo info;

	public void start() {
		Settings settings = new Settings(null, null);
		try {
			InetSocketAddress addr = info.getAddressToForwardTo();
			if(info.getDirection() == RecordingDirection.FROM_SERVERSOCKET)
				addr = info.getIncomingAddress();
			
			String name = parse(addr);
			
			channel = chanMgr.createTCPServerChannel(name, settings );
			listener.setInfo(info);
			channel.bind(info.getIncomingAddress());
			channel.registerServerSocketChannel(listener);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String parse(InetSocketAddress addr) {
		String name = addr.getHostName();
		return name+":"+addr.getPort();
	}

	public void stop() {
		channel.close();
		
		for(Channel c : listener.getChannels()) {
			c.close();
		}
	}

	public void setInfo(ProxyInfo info) {
		this.info = info;
	}

	public ProxyInfo getInfo() {
		return info;
	}


}
