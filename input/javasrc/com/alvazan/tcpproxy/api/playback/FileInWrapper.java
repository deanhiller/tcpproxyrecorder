package com.alvazan.tcpproxy.api.playback;

import java.io.InputStream;

public interface FileInWrapper {

	public InputStream openInputStream();
	
	public void close();
}
