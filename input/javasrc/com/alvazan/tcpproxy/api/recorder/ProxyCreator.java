package com.alvazan.tcpproxy.api.recorder;

import java.io.File;

import com.alvazan.tcpproxy.impl.ProxyCreatorImpl;
import com.google.inject.ImplementedBy;

@ImplementedBy(ProxyCreatorImpl.class)
public interface ProxyCreator {

	/**
	 * Call this for as many sockets as you will proxy.  Your server may have 
	 *  <ol>
	 *    <li>clients connecting in to this proxy(which connects to your server) OR</li>
	 *    <li>Your server may be connecting out to this proxy(which connects to another server)</li>
	 *  </ol>
	 * @param proxy
	 * @param s 
	 * @param recordDirection Here we need to know which way to record for playback as we mark data as for playback or not for playback(we record both directions of course but only play back one of the directions)
	 */
	public void createTcpProxy(ProxyInfo info);

	//public void createUdpProxy(InetSocketAddress incomingAddress, InetSocketAddress addressToForwardTo, RecordingDirection direction, int bufferSize);
	
	public FileOutWrapper createFile(File file);
	
	/**
	 * We record in TWO files, because one has the commands and the other may have binary payloads and we don't knwo the size of the payloads so recording
	 * in two files we can record sizes when finished with the command in the command file ;).  This just makes it easy but a bit harder to correlate the
	 * commands with the stream file.
	 * @param cmdFile
	 * @param streamFile
	 * @return
	 */	
	public void startAll(FileOutWrapper cmdFile, FileOutWrapper streamFile);
	
	public void stopAll();
	
	public DemarcatorFactory createSimpleDelimeterOne(String delimeter);
	
}
