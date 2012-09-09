package com.alvazan.tcpproxy.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import biz.xsoftware.api.nio.ChannelService;
import biz.xsoftware.api.nio.ChannelServiceFactory;

import com.alvazan.tcpproxy.api.ProxyFactory;
import com.alvazan.tcpproxy.api.playback.Playback;
import com.alvazan.tcpproxy.api.recorder.DemarcatorFactory;
import com.alvazan.tcpproxy.api.recorder.ProxyCreator;

public class TestBasicPlayback {

	@Test
	public void testPlayback() throws IOException {
		//script is taken from IntegTestBasicRecord so we can play it back
		//String script = "CONNECT,[p][localhost:4445][session 0] ,tcp,localhost:4445,0,true\nWRITE,[p][localhost:4445][session 0] ,tcp,null,10,true\nCONNECT,[p][localhost:7776][session 0] ,tcp,localhost:7776,0,false\nWRITE,[p][localhost:7776][session 0] ,tcp,null,10,true\nWRITE,[p][localhost:4445][session 0] ,tcp,null,23,false\nWRITE,[p][localhost:7776][session 0] ,tcp,null,21,false";
		String script = "CONNECT,[p][localhost:4445][session 0] ,tcp,localhost:4445,0,true\n" +
				"WRITE,[p][localhost:4445][session 0] ,tcp,null,14,true\n" +
				"CONNECT,[p][localhost:7776][session 0] ,tcp,localhost:7776,0,false\n" +
				"WRITE,[p][localhost:7776][session 0] ,tcp,null,12,true\n" +
				"WRITE,[p][localhost:4445][session 0] ,tcp,null,28,false\n" +
				"WRITE,[p][localhost:7776][session 0] ,tcp,null,26,false\n" +
				"DISCONNECT,[p][localhost:4445][session 0] ,tcp,null,0,true\n"+
				"DISCONNECT,[p][localhost:7776][session 0] ,tcp,null,0,false\n";
		
		
		//stream is taken from IntegTestBasicRecord 
		String streamContents = "|connect111abc|testsplit2d|hiToClient33|clientLast5abc|hiToServer44|serverLast6d";
		
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
		
		ProxyCreator proxy = ProxyFactory.getRecordingInstance(true);
		DemarcatorFactory factory1 = proxy.createSimpleDelimeterOne(SemiRealServer.DELIMITER1);
		DemarcatorFactory factory2 = proxy.createSimpleDelimeterOne(SemiRealServer.DELIMITER2);
		Map<Integer, DemarcatorFactory> portToFactory = new HashMap<Integer, DemarcatorFactory>();
		portToFactory.put(4445, factory1);
		portToFactory.put(7776, factory2);
		playback.initialize(cmds, stream, portToFactory);
		
		playback.step();
		
		playback.verify();
		
		playback.step();
		
		playback.verify();
		
		playback.step();
		
		playback.verify();
	}
}
