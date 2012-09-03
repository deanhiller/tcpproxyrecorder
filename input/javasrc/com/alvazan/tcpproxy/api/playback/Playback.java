package com.alvazan.tcpproxy.api.playback;

import java.io.File;

import com.alvazan.tcpproxy.impl.PlaybackImpl;
import com.google.inject.ImplementedBy;

@ImplementedBy(PlaybackImpl.class)
public interface Playback {

	public FileInWrapper createFile(File file);
	
	public void initialize(FileInWrapper file, FileInWrapper stream);
	
	/**
	 * Will feed in next packet.  In some cases, if next packet is larger than maxWriteSize, it will send
	 * multiple packets until the whole payload has been sent.
	 */
	public void step();

	public void verify();
	
}
