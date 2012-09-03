package com.alvazan.tcpproxy.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import biz.xsoftware.api.nio.ChannelService;

import com.alvazan.tcpproxy.api.recorder.DemarcatorFactory;
import com.alvazan.tcpproxy.api.recorder.FileOutWrapper;
import com.alvazan.tcpproxy.api.recorder.ProxyCreator;
import com.alvazan.tcpproxy.api.recorder.ProxyInfo;
import com.alvazan.tcpproxy.impl.file.FileWrapperImpl;
import com.alvazan.tcpproxy.impl.file.FileWriter;
import com.alvazan.tcpproxy.impl.tcp.DelimiterDemarcatorFactory;
import com.alvazan.tcpproxy.impl.tcp.TcpProxy;
import com.alvazan.tcpproxy.impl.udp.UdpProxy;

public class ProxyCreatorImpl implements ProxyCreator {

	@Inject
	private Provider<TcpProxy> factory;
	@Inject
	private Provider<UdpProxy> udpFactory;
	@Inject
	private ChannelService chanMgr;
	@Inject
	private FileWriter writer;
	private List<TcpProxy> proxies = new ArrayList<TcpProxy>();
	private List<UdpProxy> udpProxies = new ArrayList<UdpProxy>();
	private boolean isRunning = false;

	@Override
	public void createTcpProxy(ProxyInfo info) {
		for(TcpProxy proxy : proxies) {
			if(proxy.getInfo().getIncomingAddress().getPort() == info.getIncomingAddress().getPort()) 
				throw new IllegalArgumentException("You already created a proxy that will bind to port="+info.getIncomingAddress().getPort());
			else if(proxy.getInfo().getAddressToForwardTo().equals(info.getAddressToForwardTo()))
				throw new IllegalArgumentException("You already created a proxy that will forward to address="+info.getAddressToForwardTo());
		}
		
		TcpProxy proxy = factory.get();
		proxy.setInfo(info);
		proxies.add(proxy);
	}

//	@Override
//	public void createUdpBroadcastProxy(InetSocketAddress incomingAddress,
//			InetSocketAddress addressToForwardTo, RecordingDirection direction, int bufferSize) {
//		UdpProxy proxy = udpFactory.get();
//		ProxyInfo info = new ProxyInfo();
//		info.setIncomingAddress(incomingAddress);
//		info.setAddressToForwardTo(addressToForwardTo);
//		info.setDirection(direction);
//		proxy.setInfo(info);
//		proxy.setBufferSize(bufferSize);
//		udpProxies.add(proxy);
//	}
	
	@Override
	public void startAll(FileOutWrapper cmdFile, FileOutWrapper stream) {
		if(isRunning)
			throw new IllegalStateException("You must stop this one since it is still running");
		writer.setFiles(cmdFile, stream);
		writer.open();
		
		try {
			chanMgr.start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		for(TcpProxy proxy : proxies) {
			proxy.start();
		}
		for(UdpProxy proxy : udpProxies) {
			proxy.start();
		}
		isRunning = true;
	}

	public void stopAll() {
		writer.close();
		try {
			chanMgr.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		for(TcpProxy proxy : proxies) {
			proxy.stop();
		}
		for(UdpProxy proxy : udpProxies) {
			proxy.stop();
		}
		isRunning = false;
	}
	
	@Override
	public DemarcatorFactory createSimpleDelimeterOne(String delimeter) {
		return new DelimiterDemarcatorFactory(delimeter);
	}

	@Override
	public FileOutWrapper createFile(File file) {
		FileWrapperImpl impl = new FileWrapperImpl(file);
		return impl;
	}


}
