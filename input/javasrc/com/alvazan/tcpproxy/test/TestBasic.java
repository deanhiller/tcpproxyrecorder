package com.alvazan.tcpproxy.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import junit.framework.Assert;

import org.junit.Test;

import biz.xsoftware.api.nio.ChannelService;
import biz.xsoftware.api.nio.ChannelServiceFactory;
import biz.xsoftware.api.nio.channels.RegisterableChannel;
import biz.xsoftware.api.nio.channels.TCPChannel;
import biz.xsoftware.api.nio.channels.TCPServerChannel;
import biz.xsoftware.api.nio.handlers.ConnectionListener;

import com.alvazan.tcpproxy.api.recorder.DemarcatorFactory;
import com.alvazan.tcpproxy.api.recorder.Direction;
import com.alvazan.tcpproxy.api.recorder.FileWrapper;
import com.alvazan.tcpproxy.api.recorder.ProxyInfo;
import com.alvazan.tcpproxy.api.recorder.TCPProxy;
import com.alvazan.tcpproxy.api.recorder.TCPProxyFactory;

public class TestBasic {

	private MockWrapper stream;
	private MockWrapper cmds;

	@Test
	public void testBasic() throws IOException, InterruptedException {
		TCPProxy proxy = TCPProxyFactory.getInstance();
		
		DemarcatorFactory factory1 = proxy.createSimpleDelimeterOne(SemiRealServer.DELIMITER1);
		DemarcatorFactory factory2 = proxy.createSimpleDelimeterOne(SemiRealServer.DELIMITER2);
		InetAddress local = InetAddress.getLocalHost();
		InetSocketAddress s = new InetSocketAddress(local, 4445);
		InetSocketAddress s2 = new InetSocketAddress(local, 4446);
		int proxyPort = 7775;
		int proxyPort2 = 7776;
		ProxyInfo prox1 = createInfo(factory1, s, Direction.TO_SERVERSOCKET);
		ProxyInfo prox2 = createInfo(factory2, s2, Direction.FROM_SERVERSOCKET);
		proxy.createProxy(proxyPort, prox1);
		proxy.createProxy(proxyPort2, prox2);

		FileWrapper cmds1 = createCommandsWrapper(proxy);
		FileWrapper stream1 = createStreamWrapper(proxy);
		proxy.startAll(cmds1, stream1);
		
		InetSocketAddress remoteProxyAddr = new InetSocketAddress(local, proxyPort2);
		ChannelService chanMgr = ChannelServiceFactory.createRawChannelManager("only");
		chanMgr.start();
		
		SemiRealServer server1 = new SemiRealServer(s, remoteProxyAddr, chanMgr);
		server1.start();

		TCPChannel client = chanMgr.createTCPChannel("client", null);
		TCPServerChannel server = chanMgr.createTCPServerChannel("server", null);
		MockConnectListener mockListener = new MockConnectListener();
		server.bind(s2);
		server.registerServerSocketChannel(mockListener);

		//now we can throw some data at the system
		client.connect(new InetSocketAddress(local, 7775));
		
		Thread.sleep(500);
		
		assertMocks();
		mockListener.assertConnected(false);

		String cmd = "connect"+SemiRealServer.DELIMITER1;
		client.write(createBuffer(cmd));
		
		Thread.sleep(500);
		
		mockListener.assertConnected(true);
		assertMocksStep2(cmd);
		
		TCPChannel bottomChannel = mockListener.getBottomServerChannel();
		String cmd2 = "testsplit"+SemiRealServer.DELIMITER2;
		bottomChannel.write(createBuffer(cmd2));
		
		Thread.sleep(1000);
		
		assertMocksStep3();
		
	}

	protected void assertMocksStep3() {
		stream.assertStreamContents();
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

	protected FileWrapper createStreamWrapper(TCPProxy proxy) {
		stream = new MockWrapper();
		return stream;
	}

	protected FileWrapper createCommandsWrapper(TCPProxy proxy) {
		cmds = new MockWrapper();
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
	
	private ProxyInfo createInfo(DemarcatorFactory factory, InetSocketAddress s, Direction dir) {
		ProxyInfo prox1 = new ProxyInfo();
		prox1.setAddressToForwardTo(s);
		prox1.setDemarcatorFactory(factory);
		prox1.setDirection(dir);
		return prox1;
	}
}
