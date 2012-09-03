package com.alvazan.tcpproxy.impl.tcp;

import biz.xsoftware.api.nio.ChannelManager;
import biz.xsoftware.api.nio.ChannelService;
import biz.xsoftware.api.nio.ChannelServiceFactory;

import com.alvazan.tcpproxy.impl.file.FileWriteAggregation;
import com.alvazan.tcpproxy.impl.file.FileWritePassthrough;
import com.alvazan.tcpproxy.impl.file.FileWriter;
import com.google.inject.Binder;
import com.google.inject.Module;

public class ProductionBindings implements Module {

	private boolean isAggregating;

	public ProductionBindings(boolean isAggregating) {
		this.isAggregating = isAggregating;
	}

	@Override
	public void configure(Binder binder) {
		ChannelService chanMgr = ChannelServiceFactory.createRawChannelManager("p");
		binder.bind(ChannelService.class).toInstance(chanMgr);
		binder.bind(ChannelManager.class).toInstance(chanMgr);
		
		if(isAggregating)
			binder.bind(FileWriter.class).to(FileWriteAggregation.class).asEagerSingleton();
		else
			binder.bind(FileWriter.class).to(FileWritePassthrough.class).asEagerSingleton();
	}

}
