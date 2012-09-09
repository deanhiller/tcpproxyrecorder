package com.alvazan.tcpproxy.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import biz.xsoftware.api.nio.ChannelService;
import biz.xsoftware.api.nio.ChannelServiceFactory;
import biz.xsoftware.api.nio.channels.RegisterableChannel;
import biz.xsoftware.api.nio.channels.TCPChannel;
import biz.xsoftware.api.nio.channels.TCPServerChannel;
import biz.xsoftware.api.nio.handlers.ConnectionListener;

import com.alvazan.tcpproxy.api.ProxyFactory;
import com.alvazan.tcpproxy.api.recorder.DemarcatorFactory;
import com.alvazan.tcpproxy.api.recorder.FileOutWrapper;
import com.alvazan.tcpproxy.api.recorder.ProxyCreator;
import com.alvazan.tcpproxy.api.recorder.ProxyInfo;
import com.alvazan.tcpproxy.api.recorder.RecordingDirection;

public class TestBasicRecord {

	private MockOutWrapper stream;
	private MockOutWrapper cmds;
	private ProxyCreator proxy;
	private ChannelService chanMgr;
	private SemiRealServer server1;
	private InetAddress local;
	private InetSocketAddress semiRealListen;
	private InetSocketAddress proxyServerSendToAddr;
	private TCPChannel client;
	private TCPServerChannel server;

	@Before
	public void setup() throws IOException {
		proxy = ProxyFactory.getRecordingInstance(true);
		local = InetAddress.getByName("localhost");
		semiRealListen = new InetSocketAddress(local, 4445);
		proxyServerSendToAddr = new InetSocketAddress(local, 7776);
		
		chanMgr = ChannelServiceFactory.createRawChannelManager("semiReal");
		chanMgr.start();

		server1 = new SemiRealServer(semiRealListen, proxyServerSendToAddr, chanMgr);
		server1.start();
		
		client = chanMgr.createTCPChannel("client", null);
		server = chanMgr.createTCPServerChannel("server", null);
	}
	@After
	public void teardown() throws IOException, InterruptedException {
		proxy.stopAll();
		server1.stop();
		server.close();
		chanMgr.stop();
		Thread.sleep(1000);
	}
	
	@Test
	public void testNoDemarcator() throws IOException, InterruptedException {

		InetSocketAddress proxySemiRealListen = new InetSocketAddress(7775);

		InetSocketAddress bottomestMockServerAddr = new InetSocketAddress(local, 4446);
		ProxyInfo prox1 = createInfo(proxySemiRealListen, null, semiRealListen, RecordingDirection.TO_SERVERSOCKET);
		ProxyInfo prox2 = createInfo(proxyServerSendToAddr, null, bottomestMockServerAddr, RecordingDirection.FROM_SERVERSOCKET);
		proxy.createTcpProxy(prox1);
		proxy.createTcpProxy(prox2);
		
		FileOutWrapper cmds1 = createCommandsWrapper(proxy);
		FileOutWrapper stream1 = createStreamWrapper(proxy);
		proxy.startAll(cmds1, stream1);
		
		MockConnectListener mockListener = new MockConnectListener();
		server.bind(bottomestMockServerAddr);
		server.registerServerSocketChannel(mockListener);

		//now we can throw some data at the system
		client.connect(new InetSocketAddress(local, 7775));
		
		Thread.sleep(500);
		
		assertMocks();
		mockListener.assertConnected(false);

		String cmd = "|connect111"+SemiRealServer.DELIMITER1;
		client.write(createBuffer(cmd));
		
		Thread.sleep(500);
		
		mockListener.assertConnected(true);
		assertMocksStep2(cmd);
		
		TCPChannel bottomChannel = mockListener.getBottomServerChannel();
		String cmd2 = "|testsplit2"+SemiRealServer.DELIMITER2;
		bottomChannel.write(createBuffer(cmd2));
		
		Thread.sleep(1000);
		
		assertMocksStep3b();
		
		client.close();
	}
	
	@Test
	public void testBasic() throws IOException, InterruptedException {
		
		DemarcatorFactory factory1 = proxy.createSimpleDelimeterOne(SemiRealServer.DELIMITER1);
		DemarcatorFactory factory2 = proxy.createSimpleDelimeterOne(SemiRealServer.DELIMITER2);
		InetSocketAddress proxySemiRealListen = new InetSocketAddress(7775);
		InetSocketAddress bottomestMockServerAddr = new InetSocketAddress(local, 4446);
		ProxyInfo prox1 = createInfo(proxySemiRealListen, factory1, semiRealListen, RecordingDirection.TO_SERVERSOCKET);
		ProxyInfo prox2 = createInfo(proxyServerSendToAddr, factory2, bottomestMockServerAddr, RecordingDirection.FROM_SERVERSOCKET);
		proxy.createTcpProxy(prox1);
		proxy.createTcpProxy(prox2);
		
		FileOutWrapper cmds1 = createCommandsWrapper(proxy);
		FileOutWrapper stream1 = createStreamWrapper(proxy);
		proxy.startAll(cmds1, stream1);

		MockConnectListener mockListener = new MockConnectListener();
		server.bind(bottomestMockServerAddr);
		server.registerServerSocketChannel(mockListener);

		//now we can throw some data at the system
		client.connect(new InetSocketAddress(local, 7775));
		
		Thread.sleep(500);
		
		assertMocks();
		mockListener.assertConnected(false);

		String cmd = "|connect111"+SemiRealServer.DELIMITER1;
		client.write(createBuffer(cmd));
		
		Thread.sleep(500);
		
		mockListener.assertConnected(true);
		assertMocksStep2(cmd);
		
		TCPChannel bottomChannel = mockListener.getBottomServerChannel();
		bottomChannel.registerForReads(new Empty());
		
		String cmd2 = "|testsplit2"+SemiRealServer.DELIMITER2;
		bottomChannel.write(createBuffer(cmd2));
		
		Thread.sleep(1000);
		
		assertMocksStep3a();
		
		client.close();
		
		Thread.sleep(500);
		
		assertCloses();
	}
	protected void assertCloses() {
		cmds.assertCloseCommands();
	}

	protected void assertMocksStep3b() {
		stream.assertStreamContents2();
		cmds.assertSplitCommands2();
	}
	protected void assertMocksStep3a() {
		stream.assertStreamContents1();
		cmds.assertSplitCommands();
	}

	protected void assertMocksStep2(String cmd) {
		stream.assertSize(cmd.length());
		cmds.assertWriteCommandAtEnd();
	}

	protected void assertMocks() {
		cmds.assertConnectCmd();
		stream.assertSize(0);
	}

	protected FileOutWrapper createStreamWrapper(ProxyCreator proxy) {
		stream = new MockOutWrapper();
		return stream;
	}

	protected FileOutWrapper createCommandsWrapper(ProxyCreator proxy) {
		cmds = new MockOutWrapper();
		return cmds;
	}

	private ByteBuffer createBuffer(String cmd) {
		byte[] bytes = cmd.getBytes();
		ByteBuffer buf = ByteBuffer.allocate(bytes.length);
		buf.put(bytes);
		buf.flip();
		return buf;
	}

	private class MockConnectListener implements ConnectionListener {
		private boolean isConnected;
		private TCPChannel veryBottomChannel;

		@Override
		public void connected(TCPChannel channel) throws IOException {
			isConnected = true;
			this.veryBottomChannel = channel;
		}

		public TCPChannel getBottomServerChannel() {
			return veryBottomChannel;
		}

		public void assertConnected(boolean isConnected) {
			Assert.assertEquals(isConnected, this.isConnected);
		}

		@Override
		public void connectFailed(RegisterableChannel channel, Throwable e) {
			
		}
	}
	
	private ProxyInfo createInfo(InetSocketAddress incomingAddr, DemarcatorFactory factory, InetSocketAddress toAddr, RecordingDirection dir) {
		ProxyInfo prox1 = new ProxyInfo();
		prox1.setIncomingAddress(incomingAddr);
		prox1.setAddressToForwardTo(toAddr);
		prox1.setDemarcatorFactory(factory);
		prox1.setDirection(dir);
		return prox1;
	}
}
