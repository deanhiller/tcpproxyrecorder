package com.alvazan.tcpproxy.api.recorder;

public enum RecordingDirection {

	/**
	 * This is the common case where we open a server socket and record for playback stuff coming into the server socket but for the 
	 * stuff goign out, we record it but won't play it back.
	 */
	TO_SERVERSOCKET, 
	/**
	 * In some cases, we want to open a server socket and the first request going out from a server will be recorded but we won't play that back as
	 * the client of some other server is live, but we will record stuff goign back so we can play that back
	 */
	FROM_SERVERSOCKET;
}
