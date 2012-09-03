package com.alvazan.tcpproxy.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.xsoftware.api.nio.channels.Channel;
import biz.xsoftware.api.nio.channels.TCPChannel;

import com.alvazan.tcpproxy.impl.file.Action;
import com.alvazan.tcpproxy.impl.file.Command;

public class RecordingState {
	private static final Logger log = LoggerFactory.getLogger(RecordingState.class);
	
	private List<Command> originalRecording = new ArrayList<Command>();
	private InputStream stream;
	
	private Map<String, Channel> channelIdToChannel = new HashMap<String, Channel>();
	
	public void socketConnected(Command connectCmd, TCPChannel channel) {
		for(Command cmd : originalRecording) {
			if(cmd.isNeedsPlayback()) {
				log.warn("We have to go PAST a playback command to match this recording meaning playback is happening differently!!!");
			}else if(cmd.getAction() == Action.CONNECT && cmd.getAddress().equals(connectCmd)) {
				log.info("match on cmd="+cmd);
				process(cmd, channel);
				break;
			} else {
				log.info("no match on cmd="+cmd);
			}
		}
	}

	private void process(Command cmd, Channel channel) {
		putChannel(cmd, channel);
		originalRecording.remove(cmd);
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
			discardUnmatched();
		
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

	private void discardUnmatched() {
		log.warn("We have to discard events that should have happened(were recorded and did not happen)");
		while(originalRecording.size() != 0) {
			Command cmd = originalRecording.get(0);
			if(!cmd.isNeedsPlayback()) {
				log.warn("Discarding command that was supposed to be verified but did not happen="+cmd);
				originalRecording.remove(0);
			} else
				return;
		}
	}

	public Channel getChannel(String channelId) {
		return channelIdToChannel.get(channelId);
	}

	public void socketClosed(Command connectCmd, Channel channel) {
		
	}

	public void socketIncomingData(Command connectCmd, Channel channel,
			ByteBuffer b) {
		int remaining = b.remaining();
		Command cmd = originalRecording.remove(0);
		log.info("validating cmd="+cmd);
		if(!cmd.getChannelId().equals(connectCmd.getChannelId()))
			throw new IllegalStateException("Validation is failing.  write data for channel="+connectCmd.getChannelId()+" came in but expected was="+cmd);
		else if(cmd.getPayloadSize() != remaining)
			throw new IllegalStateException("Payload size is incorrect for cmd="+cmd+"  actual size was="+remaining+" expected="+cmd.getPayloadSize());
		
		byte[] data = new byte[remaining];
		read(data);

		byte[] actual = new byte[remaining];
		b.get(actual);
		
		if(!Arrays.equals(data, actual)) {
			String exp = new String(data);
			String act = new String(actual);
			throw new IllegalStateException("Payloads did not match.  expected="+exp+" actual="+act);
		}
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

}
