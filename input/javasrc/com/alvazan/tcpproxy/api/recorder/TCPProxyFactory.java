package com.alvazan.tcpproxy.api.recorder;

import com.alvazan.tcpproxy.impl.ProductionBindings;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class TCPProxyFactory {

	public static TCPProxy getInstance() {
		Injector injector = Guice.createInjector(new ProductionBindings());
		return injector.getInstance(TCPProxy.class);
	}

}
