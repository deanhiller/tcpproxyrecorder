package com.alvazan.tcpproxy.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import biz.xsoftware.api.nio.ChannelService;

import com.alvazan.tcpproxy.api.recorder.DemarcatorFactory;
import com.alvazan.tcpproxy.api.recorder.FileWrapper;
import com.alvazan.tcpproxy.api.recorder.ProxyInfo;
import com.alvazan.tcpproxy.api.recorder.TCPProxy;
import com.alvazan.tcpproxy.impl.file.FileWrapperImpl;
import com.alvazan.tcpproxy.impl.file.FileWriter;

public class TCPProxyImpl implements TCPProxy {

	@Inject
	private Provider<Proxy> factory;
	@Inject
	private ChannelService chanMgr;
	@Inject
	private FileWriter writer;
	private List<Proxy> proxies = new ArrayList<Proxy>();
	private boolean isRunning = false;
	
	@Override
	public void createProxy(int portToAcceptIncoming, ProxyInfo info) {
		for(Proxy proxy : proxies) {
			if(proxy.getIncomingPort() == portToAcceptIncoming) 
				throw new IllegalArgumentException("You already created a proxy that will bind to port="+portToAcceptIncoming);
			else if(proxy.getInfo().getAddressToForwardTo().equals(info.getAddressToForwardTo()))
				throw new IllegalArgumentException("You already created a proxy that will forward to address="+info.getAddressToForwardTo());
		}
		
		Proxy proxy = factory.get();
		proxy.setIncomingPort(portToAcceptIncoming);
		proxy.setInfo(info);
		proxies.add(proxy);
	}

	@Override
	public void startAll(FileWrapper cmdFile, FileWrapper stream) {
		if(isRunning)
			throw new IllegalStateException("You must stop this one since it is still running");
		writer.setFiles(cmdFile, stream);
		writer.open();
		
		try {
			chanMgr.start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		for(Proxy proxy : proxies) {
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
		
		for(Proxy proxy : proxies) {
			proxy.stop();
		}
		isRunning = false;
	}
	
	@Override
	public DemarcatorFactory createSimpleDelimeterOne(String delimeter) {
		return new DelimiterDemarcatorFactory(delimeter);
	}

	@Override
	public FileWrapper createFile(File file) {
		FileWrapperImpl impl = new FileWrapperImpl(file);
		return impl;
	}


}
