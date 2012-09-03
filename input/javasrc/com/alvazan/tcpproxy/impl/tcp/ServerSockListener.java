package com.alvazan.tcpproxy.impl.tcp;

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

import com.alvazan.tcpproxy.api.recorder.PacketDemarcator;
import com.alvazan.tcpproxy.api.recorder.ProxyInfo;
import com.alvazan.tcpproxy.api.recorder.RecordingDirection;
import com.alvazan.tcpproxy.impl.file.Action;
import com.alvazan.tcpproxy.impl.file.ChannelType;
import com.alvazan.tcpproxy.impl.file.Command;
import com.alvazan.tcpproxy.impl.file.FileWriter;

public class ServerSockListener implements ConnectionListener {

	private static final Logger log = LoggerFactory.getLogger(ServerSockListener.class);
	public static final String ID = "id";
	
	@Inject
	private FileWriter writer;
	@Inject
	private ChannelManager chanMgr;
	@Inject
	private Provider<SocketDataListener> factory;
	
	private ProxyInfo info;

	@Override
	public void connected(TCPChannel channel) throws IOException {
		channel.toString();
		boolean isRecordAndPlayback = true;
		if(info.getDirection() == RecordingDirection.FROM_SERVERSOCKET)
			isRecordAndPlayback = false;

		channel.getSession().put(ID, ""+channel);
		
		//Depending on direction, need to record the correct address
		InetSocketAddress address = info.getAddressToForwardTo();
		if(info.getDirection() == RecordingDirection.FROM_SERVERSOCKET)
			address = info.getIncomingAddress();
		
		String channelId = (String) channel.getSession().get("id");
		Command cmd= new Command(channelId, ChannelType.TCP, Action.CONNECT, address, 0, isRecordAndPlayback);
		writer.writeCommand(cmd);
		
		//We need to make this channel have same info as channel that came into us....
		Object id = channel.getSession().get(ID);
		TCPChannel proxyChannel = chanMgr.createTCPChannel("{id:"+channel+"}", null );
		proxyChannel.getSession().put(ID, id);
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

}
