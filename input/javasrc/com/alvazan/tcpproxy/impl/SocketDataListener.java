package com.alvazan.tcpproxy.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.xsoftware.api.nio.channels.Channel;
import biz.xsoftware.api.nio.channels.TCPChannel;
import biz.xsoftware.api.nio.handlers.DataListener;

import com.alvazan.tcpproxy.api.recorder.PacketDemarcator;
import com.alvazan.tcpproxy.api.recorder.PacketReadListener;
import com.alvazan.tcpproxy.api.recorder.ProxyInfo;
import com.alvazan.tcpproxy.impl.file.Action;
import com.alvazan.tcpproxy.impl.file.Command;
import com.alvazan.tcpproxy.impl.file.FileWriter;

public class SocketDataListener implements DataListener, PacketReadListener {

	private static final Logger log = LoggerFactory.getLogger(SocketDataListener.class);
	
	@Inject
	private FileWriter writer;

	private TCPChannel incomingChannel;
	private TCPChannel otherChannel;
	private boolean isRecordForPlayback;
	private PacketDemarcator demarcator;
	private long bytesWritten = 0;
	
	@Override
	public void incomingData(Channel channel, ByteBuffer b) throws IOException {
		ByteBuffer newBuf = ByteBuffer.allocate(b.remaining());
		newBuf.put(b);
		b.rewind();

		//We can't support two sockets streaming data at the same time.  Each socket must pass all the pieces of the
		//payload and then demarcate(or we need to modify to multiple files for recording which we can do later if needed).  We
		//throw an exception if multiple streams try to write without demarcation
		synchronized(SocketDataListener.class) {
			//just keep feeding data and expect them to feed us the data calling passMoreData
			demarcator.feedMoreData(newBuf);
		}
		
		otherChannel.write(b);
	}

	@Override
	public void farEndClosed(Channel channel) {
		Command cmd = new Command(incomingChannel, Action.DISCONNECT, null, 0, isRecordForPlayback);
		writer.writeCommand(cmd);
	}

	@Override
	public void passMoreData(byte[] buffer) {
		bytesWritten += buffer.length;
		writer.addToStream(incomingChannel, buffer);
	}

	@Override
	public void demarcatePacketHere() {
		//reset byte counter here
		Command cmd = new Command(incomingChannel, Action.WRITE, null, bytesWritten, isRecordForPlayback);
		writer.writeCommand(cmd);
		bytesWritten = 0;
	}
	
	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		log.warn("channel failure on incoming data(we can't simulate this)="+channel, e);
	}

	public void setup(TCPChannel incomingChannel, TCPChannel outChannel, ProxyInfo info,
			boolean isRecordAndPlayback, PacketDemarcator demarcator) {
		this.otherChannel = outChannel;
		this.incomingChannel = incomingChannel;
		this.isRecordForPlayback = isRecordAndPlayback;
		demarcator.addListener(this);
	}

}
