package com.alvazan.tcpproxy.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.xsoftware.api.nio.ChannelService;
import biz.xsoftware.api.nio.channels.Channel;
import biz.xsoftware.api.nio.channels.TCPChannel;
import biz.xsoftware.api.nio.channels.TCPServerChannel;

import com.alvazan.tcpproxy.api.playback.FileInWrapper;
import com.alvazan.tcpproxy.api.playback.Playback;
import com.alvazan.tcpproxy.impl.file.Command;

public class PlaybackImpl implements Playback {

	private static final Logger log = LoggerFactory.getLogger(PlaybackImpl.class);
	
	@Inject
	private ChannelService chanMgr;
	@Inject
	private Provider<PlayServerSockListener> factory;
	@Inject
	private Provider<PlayDataListener> dataListFactory;
	@Inject
	private RecordingState state;
	
	private Set<InetSocketAddress> boundAddresses = new HashSet<InetSocketAddress>();
	
	@Override
	public FileInWrapper createFile(File file) {
		return new FileInWrapperImpl(file);
	}

	@Override
	public void initialize(FileInWrapper file, FileInWrapper stream) {
		try {
			initializeImpl(file, stream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void initializeImpl(FileInWrapper file, FileInWrapper stream) throws IOException {
		BufferedReader firstPass = createReader(file);
		InputStream str = stream.openInputStream();
		state.setInputStream(str);
		
		//We need to read all the commands in to get an idea of how many sockets to start up
		String line = firstPass.readLine();
		do {
			Command cmd = checkForAddServerSocket(line);
			line = firstPass.readLine();
			state.addLine(cmd);
			
		} while(line != null);
		
		firstPass.close();
	}

	private Command checkForAddServerSocket(String line) {
		Command cmd = Command.parse(line);
		if(!cmd.isNeedsPlayback()) {
			createServerSocketIfNeeded(cmd);
		}
		return cmd;
	}
	private void createServerSocketIfNeeded(Command cmd) {
		try {
			createServerSocketIfNeededImpl(cmd);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void createServerSocketIfNeededImpl(Command cmd) throws IOException, InterruptedException {
		InetSocketAddress addr = cmd.getAddress();
		if(boundAddresses.contains(addr))
			return; //We are already bound and if we get here it means we will have multiple clients connecting then as two clients recorded a connect
		
		PlayServerSockListener listener = factory.get();
		listener.setConnectCmd(cmd);
		TCPServerChannel channel = chanMgr.createTCPServerChannel("mockServer", null);
		channel.bind(addr);
		channel.registerServerSocketChannel(listener);
		boundAddresses.add(addr);
	}

	private BufferedReader createReader(FileInWrapper file) {
		InputStream cmds = file.openInputStream();
		InputStreamReader inReader = new InputStreamReader(cmds);
		return new BufferedReader(inReader);
	}

	@Override
	public void step() {
		List<Command> cmds = state.getNextCommands();
		for(Command c : cmds) {
			runCommand(c);
		}
	}

	private void runCommand(Command c) {
		log.info("Running cmd="+c);
		switch (c.getAction()) {
		case CONNECT:
			connect(c);
			break;
		case WRITE:
			write(c);
			break;
		case DISCONNECT:
			disconnect(c);
			break;
		default:
			break;
		}
	}

	private void disconnect(Command c) {
		Channel channel = state.getChannel(c.getChannelId());
		channel.close();
	}

	private void write(Command c) {
		Channel channel = state.getChannel(c.getChannelId());
		int size = c.getPayloadSize();
		byte[] data = new byte[size];
		try {
			state.read(data);
			ByteBuffer buffer = ByteBuffer.wrap(data);
			channel.write(buffer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void connect(Command c) {
		try {
			PlayDataListener dataListener = dataListFactory.get();
			dataListener.setConnectCmd(c);
			TCPChannel channel = chanMgr.createTCPChannel(c.getChannelId(), null);
			channel.connect(c.getAddress());
			channel.registerForReads(dataListener);
			state.putChannel(c, channel);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void verify() {
	}

}
