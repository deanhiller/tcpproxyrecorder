package com.alvazan.tcpproxy.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.xsoftware.api.nio.ChannelService;
import biz.xsoftware.api.nio.channels.Channel;
import biz.xsoftware.api.nio.channels.RegisterableChannel;
import biz.xsoftware.api.nio.channels.TCPChannel;
import biz.xsoftware.api.nio.channels.TCPServerChannel;
import biz.xsoftware.api.nio.handlers.ConnectionListener;
import biz.xsoftware.api.nio.handlers.DataListener;

public class SemiRealServer implements ConnectionListener {

	private static final Logger log = LoggerFactory.getLogger(SemiRealServer.class);
	public static final String DELIMITER1 = "abc";
	public static final String DELIMITER2 = "d";
	
	private ChannelService chanMgr;
	private InetSocketAddress localListenAddress;
	private InetSocketAddress remoteAddress;
	private TCPServerChannel server;
	private List<Channel> channels = new ArrayList<Channel>();
	
	public SemiRealServer(InetSocketAddress listenAddress, InetSocketAddress sendToAddress, ChannelService chanMgr) {
		this.localListenAddress = listenAddress;
		this.remoteAddress = sendToAddress;
		this.chanMgr = chanMgr;
	}

	public void start() {
		try {
			startImpl();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void startImpl() throws IOException, InterruptedException {
		server = chanMgr.createTCPServerChannel("semiRealSvr", null);
		server.bind(localListenAddress);
		server.registerServerSocketChannel(this);
	}
	public void stop() {
		server.close();
		for(Channel c : channels) {
			c.close();
		}
	}

	@Override
	public void connected(TCPChannel channel) throws IOException {
		try {
			channels.add(channel);
			channel.registerForReads(new ServerListener());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void connectFailed(RegisterableChannel channel, Throwable e) {
	}

	private void process(Channel channel, ByteBuffer b, String delimiter) {
		byte[] data = new byte[b.remaining()];
		b.get(data);
		String s = new String(data);
		StateHolder state = findOrCreateState(channel);
		
		String fullData = state.getData()+s;
		
		while(fullData.contains(delimiter)) {
			int index = fullData.indexOf(delimiter);
			int endDelim = index+delimiter.length();
			String cmd = fullData.substring(0, index);
			fullData = fullData.substring(endDelim);
			runCommand(channel, cmd);
		}		
	}

	private StateHolder findOrCreateState(Channel channel) {
		StateHolder state = (StateHolder) channel.getSession().get("data");
		if(state == null) {
			state = new StateHolder();
			channel.getSession().put("data", state);
		}
		return state;
	}
	
	private void runCommand(Channel channel, String cmd) {
		try {
			runCommandImpl(channel, cmd);
		} catch (Exception e) {
			log.warn("Exception run cmd", e);
		}
	}
	private void runCommandImpl(Channel channel, String cmd) throws IOException, InterruptedException {
		StateHolder state = (StateHolder) channel.getSession().get("data");
		if(cmd.contains("connect")) {
			TCPChannel remoteChannel = chanMgr.createTCPChannel("svrToSvr", null);
			remoteChannel.connect(remoteAddress);
			remoteChannel.registerForReads(new ClientListener());
			state.setRemoteChannel(remoteChannel);
			
			StateHolder remoteState = findOrCreateState(remoteChannel);
			remoteState.setRemoteChannel(channel);
		} else if(cmd.contains("testsplit")) {
			Channel clientChannel = state.getRemoteChannel();
			
			//let's write to both WITHOUT putting the demarcation in just yet!!!
			String s = "|hiToClient33";
			clientChannel.write(createBuffer(s));
			String s2 = "|hiToServer44";
			channel.write(createBuffer(s2));
			
			Thread.sleep(500);
			
			clientChannel.write(createBuffer("|clientLast5"+DELIMITER1));
			channel.write(createBuffer("|serverLast6"+DELIMITER2));
		}
	}
	
	public ByteBuffer createBuffer(String s) {
		byte[] data = s.getBytes();
		ByteBuffer buf = ByteBuffer.allocate(data.length);
		buf.put(data);
		buf.flip();
		return buf;
	}
	
	private class ServerListener implements DataListener {

		@Override
		public void incomingData(Channel channel, ByteBuffer b)
				throws IOException {
			process(channel, b, DELIMITER1);
		}
		
		@Override
		public void farEndClosed(Channel clientChannel) {
			StateHolder state = (StateHolder) clientChannel.getSession().get("data");
			Channel chan = state.getRemoteChannel();
			chan.close();
		}

		@Override
		public void failure(Channel channel, ByteBuffer data, Exception e) {
			log.warn(channel+"failure", e);
		}
	}
	
	private class ClientListener implements DataListener {

		@Override
		public void incomingData(Channel channel, ByteBuffer b)
				throws IOException {
			process(channel, b, DELIMITER2);
		}

		@Override
		public void farEndClosed(Channel serverChannel) {
			StateHolder state = (StateHolder) serverChannel.getSession().get("data");
			Channel chan = state.getRemoteChannel();
			chan.close();
		}

		@Override
		public void failure(Channel channel, ByteBuffer data, Exception e) {
			log.warn(channel+"failure", e);
		}
	}


}
