package com.alvazan.tcpproxy.test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.junit.Test;

import com.alvazan.tcpproxy.api.recorder.DemarcatorFactory;
import com.alvazan.tcpproxy.api.recorder.Direction;
import com.alvazan.tcpproxy.api.recorder.FileWrapper;
import com.alvazan.tcpproxy.api.recorder.ProxyInfo;
import com.alvazan.tcpproxy.api.recorder.TCPProxy;
import com.alvazan.tcpproxy.api.recorder.TCPProxyFactory;

public class TestBasic {

	@Test
	public void testBasic() throws UnknownHostException {
		TCPProxy proxy = TCPProxyFactory.getInstance();
		
		DemarcatorFactory factory1 = proxy.createSimpleDelimeterOne("$%^");
		DemarcatorFactory factory2 = proxy.createSimpleDelimeterOne("$$$");
		InetAddress local = InetAddress.getLocalHost();
		InetSocketAddress s = new InetSocketAddress(local, 4445);
		InetSocketAddress s2 = new InetSocketAddress(local, 4446);
		
		ProxyInfo prox1 = createInfo(factory1, s, Direction.TO_SERVERSOCKET);
		ProxyInfo prox2 = createInfo(factory2, s2, Direction.FROM_SERVERSOCKET);
		proxy.createProxy(7775, prox1);
		proxy.createProxy(7776, prox2);

		FileWrapper cmds = new MockWrapper();
		FileWrapper stream = new MockWrapper();
		proxy.startAll(cmds, stream);
		
		MockServer server = new MockServer(s, s2);
		server.start();
	}

	private ProxyInfo createInfo(DemarcatorFactory factory, InetSocketAddress s, Direction dir) {
		ProxyInfo prox1 = new ProxyInfo();
		prox1.setAddressToForwardTo(s);
		prox1.setDemarcatorFactory(factory);
		prox1.setDirection(dir);
		return prox1;
	}

}
