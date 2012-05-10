package com.alvazan.tcpproxy.impl;

import java.io.IOException;
import java.net.InetSocketAddress;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.xsoftware.api.nio.ChannelManager;
import biz.xsoftware.api.nio.channels.RegisterableChannel;
import biz.xsoftware.api.nio.channels.TCPChannel;
import biz.xsoftware.api.nio.handlers.ConnectionListener;

import com.alvazan.tcpproxy.api.recorder.Direction;
import com.alvazan.tcpproxy.api.recorder.PacketDemarcator;
import com.alvazan.tcpproxy.api.recorder.ProxyInfo;
import com.alvazan.tcpproxy.impl.file.Action;
import com.alvazan.tcpproxy.impl.file.Command;
import com.alvazan.tcpproxy.impl.file.FileWriter;

public class ServerSockListener implements ConnectionListener {

	private static final Logger log = LoggerFactory.getLogger(ServerSockListener.class);
	@Inject
	private FileWriter writer;
	@Inject
	private ChannelManager chanMgr;
	@Inject
	private Provider<SocketDataListener> factory;
	
	private InetSocketAddress address;
	private ProxyInfo info;

	@Override
	public void connected(TCPChannel channel) throws IOException {
		boolean isRecordAndPlayback = true;
		if(info.getDirection() == Direction.FROM_SERVERSOCKET)
			isRecordAndPlayback = false;
		
		Command cmd= new Command(channel, Action.CONNECT, address, 0, isRecordAndPlayback);
		writer.writeCommand(cmd);
		
		TCPChannel proxyChannel = chanMgr.createTCPChannel("proxyfor="+channel, null );
		proxyChannel.connect(info.getAddressToForwardTo());
		
		try {
			
			PacketDemarcator real = info.getDemarcatorFactory().createDemarcator(channel+"");
			PacketDemarcator proxy = info.getDemarcatorFactory().createDemarcator(proxyChannel+"");
			
			SocketDataListener realListener = factory.get();
			realListener.setup(channel, proxyChannel, info, isRecordAndPlayback, real);				
			
			SocketDataListener proxyListener = factory.get();
			proxyListener.setup(proxyChannel, channel, info, !isRecordAndPlayback, proxy);
			
			proxyChannel.registerForReads(proxyListener);
			channel.registerForReads(realListener);

		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void connectFailed(RegisterableChannel channel, Throwable e) {
		log.warn("Channel connect failed, we can't simulate this", e);
	}

	public void setInfo(ProxyInfo info) {
		this.info = info;
	}

	public void setIncomingPort(int incomingPort2) {
		address = new InetSocketAddress(incomingPort2);
	}
}
