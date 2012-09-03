package com.alvazan.tcpproxy.api;

import com.alvazan.tcpproxy.api.playback.Playback;
import com.alvazan.tcpproxy.api.recorder.ProxyCreator;
import com.alvazan.tcpproxy.impl.tcp.ProductionBindings;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class ProxyFactory {

	/**
	 * 
	 * @param isAggregating If set to true, it will cache and combine all your packets according to your delemiters of the packets
	 * so that you can view the commands as a single WRITE statement.  false, means it will be spread out so many pieces of write commands
	 * are actually the write of one payload....isAggregating = true ALSO means that the stream file will contain the full payload as one rather
	 * than payloads being intermingled which can be very confusing.
	 * @return
	 */
	public static ProxyCreator getRecordingInstance(boolean isAggregating) {
		Injector injector = Guice.createInjector(new ProductionBindings(isAggregating));
		return injector.getInstance(ProxyCreator.class);
	}

	public static Playback getPlaybackInstance() {
		Injector injector = Guice.createInjector(new ProductionBindings(true));
		return injector.getInstance(Playback.class);		
	}
}
