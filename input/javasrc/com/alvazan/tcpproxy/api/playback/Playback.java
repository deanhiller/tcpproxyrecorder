package com.alvazan.tcpproxy.api.playback;

import com.alvazan.tcpproxy.api.recorder.FileWrapper;

public interface Playback {

	public void initialize(FileWrapper file, int maxWriteSize);
	
	/**
	 * Will feed in next packet.  In some cases, if next packet is larger than maxWriteSize, it will send
	 * multiple packets until the whole payload has been sent.
	 */
	public void step();
	
}
