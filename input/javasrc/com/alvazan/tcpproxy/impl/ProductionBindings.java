package com.alvazan.tcpproxy.impl;

import biz.xsoftware.api.nio.ChannelManager;
import biz.xsoftware.api.nio.ChannelService;
import biz.xsoftware.api.nio.ChannelServiceFactory;

import com.google.inject.Binder;
import com.google.inject.Module;

public class ProductionBindings implements Module {

	@Override
	public void configure(Binder binder) {
		ChannelService chanMgr = ChannelServiceFactory.createRawChannelManager("");
		binder.bind(ChannelService.class).toInstance(chanMgr);
		binder.bind(ChannelManager.class).toInstance(chanMgr);
	}

}
