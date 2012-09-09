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
import java.util.Map;
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
import com.alvazan.tcpproxy.api.recorder.DemarcatorFactory;
import com.alvazan.tcpproxy.impl.file.Action;
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
	
	private Set<Integer> boundAddresses = new HashSet<Integer>();
	private Map<Integer, DemarcatorFactory> portToFactory;
	
	@Override
	public FileInWrapper createFile(File file) {
		return new FileInWrapperImpl(file);
	}

	@Override
	public void initialize(FileInWrapper file, FileInWrapper stream,
			Map<Integer, DemarcatorFactory> portToFactory) {
		try {
			this.portToFactory = portToFactory;
			chanMgr.start();
			initializeImpl(file, stream, portToFactory);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void initializeImpl(FileInWrapper file, FileInWrapper stream, Map<Integer, DemarcatorFactory> portToFactory) throws IOException {
		BufferedReader firstPass = createReader(file);
		InputStream str = stream.openInputStream();
		state.setInputStream(str);
		
		//We need to read all the commands in to get an idea of how many sockets to start up
		String line = firstPass.readLine();
		do {
			Command cmd = checkForAddServerSocket(line, portToFactory);
			line = firstPass.readLine();
			state.addLine(cmd);
			
		} while(line != null);
		
		firstPass.close();
	}

	private Command checkForAddServerSocket(String line, Map<Integer, DemarcatorFactory> portToFactory) {
		Command cmd = Command.parse(line);
		if(!cmd.isNeedsPlayback() && cmd.getAction() == Action.CONNECT) {
			createServerSocketIfNeeded(cmd, portToFactory);
		}
		return cmd;
	}
	private void createServerSocketIfNeeded(Command cmd, Map<Integer, DemarcatorFactory> portToFactory) {
		try {
			createServerSocketIfNeededImpl(cmd, portToFactory);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void createServerSocketIfNeededImpl(Command cmd, Map<Integer, DemarcatorFactory> portToFactory) throws IOException, InterruptedException {
		InetSocketAddress addr = cmd.getAddress();
		if(boundAddresses.contains(addr.getPort()))
			return; //We are already bound and if we get here it means we will have multiple clients connecting then as two clients recorded a connect
		
		DemarcatorFactory demFactory = portToFactory.get(addr.getPort());
		if(demFactory == null)
			throw new IllegalArgumentException("The parameter portToFactory MUST " +
					"have a factory for port="+addr.getPort()+" or verification won't work as packets " +
							"on one run could be 5 bytes then 7 bytes and on another run be 7 bytes then " +
							"5 bytes so demarcating packets makes it 12 packets every time");
		//Let's just bind to 0.0.0.0 every time to avoid issues
		InetSocketAddress newAddr = new InetSocketAddress("0.0.0.0", addr.getPort());
		PlayServerSockListener listener = factory.get();
		listener.setConnectCmd(cmd, demFactory);
		TCPServerChannel channel = chanMgr.createTCPServerChannel("mockServer", null);
		channel.bind(newAddr);
		channel.registerServerSocketChannel(listener);
		boundAddresses.add(addr.getPort());
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
			
			DemarcatorFactory factory = portToFactory.get(c.getAddress().getPort());
			if(factory == null)
				throw new IllegalArgumentException("Your parameter portToFactory in initialize method" +
						" did not have a factory for port="+c.getAddress().getPort()+" but MUST have one because" +
								" otherwise runs yeild different results like write 5 bytes, write 7 bytes on one run" +
								" or write 7 bytes, write 5 bytes on next run so by having a class to tell us when packets" +
								" end allows us to get the full 12 bytes every time and be consistent");
			
			dataListener.setup(c, factory.createDemarcator(c.getChannelId()));
			TCPChannel channel = chanMgr.createTCPChannel(c.getChannelId(), null);
			InetSocketAddress address = c.getAddress();
			channel.connect(address);
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
		state.verify();
	}

}
