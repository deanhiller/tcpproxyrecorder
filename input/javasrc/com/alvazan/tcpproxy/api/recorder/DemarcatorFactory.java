package com.alvazan.tcpproxy.api.recorder;

public interface DemarcatorFactory {

	/**
	 * 
	 * @param id for logging only as we will give unique channel ids
	 * @return
	 */
	PacketDemarcator createDemarcator(String id);
}
