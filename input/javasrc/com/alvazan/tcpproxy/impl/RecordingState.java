package com.alvazan.tcpproxy.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import biz.xsoftware.api.nio.channels.Channel;
import biz.xsoftware.api.nio.channels.TCPChannel;

import com.alvazan.tcpproxy.impl.file.Action;
import com.alvazan.tcpproxy.impl.file.Command;

@Singleton
public class RecordingState {
	//private static final Logger log = LoggerFactory.getLogger(RecordingState.class);
	
	private List<Command> originalRecording = new ArrayList<Command>();
	private InputStream stream;
	
	private Map<String, Channel> channelIdToChannel = new HashMap<String, Channel>();
	private List<Command> incomingCommands = new ArrayList<Command>();
	
	public void socketConnected(Command connectCmd, TCPChannel channel) {
		putChannel(connectCmd, channel);
		incomingCommand(connectCmd);
	}

	private void incomingCommand(Command cmd) {
		synchronized(this) {
			incomingCommands.add(cmd);
			this.notifyAll();			
		}
	}
	
	public void putChannel(Command cmd, Channel channel) {
		channelIdToChannel.put(cmd.getChannelId(), channel);		
	}
	
	public void addLine(Command cmd) {
		originalRecording.add(cmd);
	}

	public List<Command> getNextCommands() {
		if(originalRecording.size() == 0)
			throw new IllegalStateException("No more commands to run");
		Command theCmd = originalRecording.get(0);
		if(!theCmd.isNeedsPlayback())
			throw new IllegalStateException("bug, should not get here as you should have called verify first!!!");
		
		List<Command> toPlay = new ArrayList<Command>();
		while(originalRecording.size() != 0) {
			Command c = originalRecording.get(0);
			if(c.isNeedsPlayback()) {
				originalRecording.remove(0);
				toPlay.add(c);
			} else
				return toPlay;
		}
		
		throw new IllegalStateException("Bug, should never get here");
	}

	public Channel getChannel(String channelId) {
		return channelIdToChannel.get(channelId);
	}

	public void socketClosed(Command cmd) {
		incomingCommand(cmd);
	}

	public void socketIncomingData(Command dataCmd) {
		incomingCommand(dataCmd);		
	}

	public void setInputStream(InputStream str) {
		this.stream = str;
	}

	public synchronized void read(byte[] data) {
		try {
			stream.read(data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void verify() {
		//first, let's count up how many commands we need to verify
		Command theCmd = originalRecording.get(0);
		if(theCmd.isNeedsPlayback()) {
			if(incomingCommands.size() > 0)
				throw new IllegalStateException("You have commands that were not expected that came in="+incomingCommands+" commands left="+originalRecording);
			return;
		}
		
		Map<String, List<Command>> expected = fetchExpectedCommands();
		
		long timeoutTime = 30000;
		long time = System.currentTimeMillis();
		//wait for expected commands...
		synchronized(this) {
			while(incomingCommands.size() < expected.size()) {
				try {
					this.wait(timeoutTime);
					long now = System.currentTimeMillis();
					if(now > time + timeoutTime)
						throw new IllegalStateException("In "+timeoutTime+"ms the expected commands did not ALL come in." +
								"  Still waiting on some.  commands that did come in="+incomingCommands+" expected="
								+expected+" left over list="+originalRecording);
				} catch (InterruptedException e) {
					throw new RuntimeException("bug?");
				}
			}
		}

		if(incomingCommands.size() > expected.size())
			throw new IllegalStateException("Too many commands came in.  We did not expect that many.  commands="+incomingCommands+" expected="+expected+" left over list="+originalRecording);

		for(Command c: incomingCommands) {
			List<Command> cmds = expected.get(c.getChannelId());
			if(cmds == null)
				throw new IllegalStateException("Commands not expected="+cmds+" expected="+expected+" leftover="+originalRecording);
			c.validate(cmds, expected, incomingCommands);

			if(c.getAction() == Action.WRITE) {
				validateStream(c);
			}
		}
		incomingCommands.clear();
	}

	private void validateStream(Command c) {
		byte[] data = new byte[c.getPayloadSize()];
		read(data);
		
		byte[] actual = c.getPayload();
		
		if(!Arrays.equals(data, actual)) {
			String exp = new String(data);
			String act = new String(actual);
			throw new IllegalStateException("Payloads did not match for cmd="+c+" expected="+exp+" actual="+act);
		}		
	}

	private Map<String, List<Command>> fetchExpectedCommands() {
		Map<String, List<Command>> expectedCmds = new HashMap<String, List<Command>>();
		while(originalRecording.size() > 0) {
			Command c = originalRecording.get(0);
			if(!c.isNeedsPlayback()) {
				originalRecording.remove(0);
				putCommand(c, expectedCmds);
			} else
				return expectedCmds;
		}
		return expectedCmds;
	}

	private void putCommand(Command c, Map<String, List<Command>> expectedCmds) {
		List<Command> list = expectedCmds.get(c.getChannelId());
		if(list == null) {
			list = new ArrayList<Command>();
			expectedCmds.put(c.getChannelId(), list);
		}
		list.add(c);
	}

}
