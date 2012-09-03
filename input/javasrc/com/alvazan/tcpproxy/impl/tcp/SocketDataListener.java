package com.alvazan.tcpproxy.impl.tcp;

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
import com.alvazan.tcpproxy.impl.file.ChannelType;
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
	private int bytesWritten = 0;
	
	@Override
	public void incomingData(Channel channel, ByteBuffer b) throws IOException {
		byte[] data = new byte[b.remaining()];
		b.get(data);
		b.rewind();

		//We can't support two sockets streaming data at the same time.  Each socket must pass all the pieces of the
		//payload and then demarcate(or we need to modify to multiple files for recording which we can do later if needed).  We
		//throw an exception if multiple streams try to write without demarcation
		synchronized(SocketDataListener.class) {
			//just keep feeding data and expect them to feed us the data calling passMoreData
			demarcator.feedMoreData(data);
		}
		
		otherChannel.write(b);
	}

	@Override
	public void farEndClosed(Channel channel) {
		String channelId = (String) incomingChannel.getSession().get("id");
		Command cmd = new Command(channelId, ChannelType.TCP, Action.DISCONNECT, null, 0, isRecordForPlayback);
		writer.writeCommand(cmd);
	}

	@Override
	public void passMoreData(byte[] buffer) {
		bytesWritten += buffer.length;
		String channelId = (String) incomingChannel.getSession().get("id");
		Command cmd = new Command(channelId, ChannelType.TCP, Action.WRITE, null, buffer.length, isRecordForPlayback);
		writer.addToStream(cmd, buffer);
	}

	@Override
	public void demarcatePacketHere() {
		//reset byte counter here
		String channelId = (String) incomingChannel.getSession().get("id");
		Command cmd = new Command(channelId, ChannelType.TCP, Action.WRITE, null, bytesWritten, isRecordForPlayback);
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
		this.demarcator = demarcator;
		demarcator.addListener(this);
	}

}
