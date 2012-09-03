package com.alvazan.tcpproxy.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.junit.Test;

import biz.xsoftware.api.nio.ChannelService;
import biz.xsoftware.api.nio.ChannelServiceFactory;

import com.alvazan.tcpproxy.api.ProxyFactory;
import com.alvazan.tcpproxy.api.playback.Playback;

public class TestBasicPlayback {

	@Test
	public void testPlayback() throws IOException {
		//script is taken from IntegTestBasicRecord so we can play it back
		String script = "CONNECT,[p][localhost:4445][session 0] ,tcp,localhost:4445,0,true\nWRITE,[p][localhost:4445][session 0] ,tcp,null,10,true\nCONNECT,[p][localhost:7776][session 0] ,tcp,localhost:7776,0,false\nWRITE,[p][localhost:7776][session 0] ,tcp,null,10,true\nWRITE,[p][localhost:4445][session 0] ,tcp,null,23,false\nWRITE,[p][localhost:7776][session 0] ,tcp,null,21,false";
		//stream is taken from IntegTestBasicRecord 
		String streamContents = "connectabctestsplitdhiToClientclientLastabchiToServerserverLastd";
		
		Playback playback = ProxyFactory.getPlaybackInstance();
		
		MockInWrapper cmds = new MockInWrapper();
		MockInWrapper stream = new MockInWrapper();
		cmds.setContentsOfFile(script);
		stream.setContentsOfFile(streamContents);
		
		ChannelService chanMgr = ChannelServiceFactory.createRawChannelManager("semiReal");
		chanMgr.start();
		InetSocketAddress s = new InetSocketAddress(4445);
		InetAddress local = InetAddress.getLocalHost();
		InetSocketAddress s2 = new InetSocketAddress(local, 7776);
		SemiRealServer svc = new SemiRealServer(s, s2, chanMgr);
		svc.start();
		
		playback.initialize(cmds, stream);
		
		playback.step();
		
		playback.verify();
		
		playback.step();
		
		playback.verify();
		
		playback.step();
		
		playback.verify();
	}
}
