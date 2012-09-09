package com.alvazan.tcpproxy.test;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.xsoftware.api.nio.channels.Channel;
import biz.xsoftware.api.nio.handlers.DataListener;

public class Empty implements DataListener {

	private static final Logger log = LoggerFactory.getLogger(Empty.class);
	
	@Override
	public void incomingData(Channel channel, ByteBuffer b) throws IOException {
	}

	@Override
	public void farEndClosed(Channel channel) {
		log.info("far end closed="+channel);
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
	}

}
