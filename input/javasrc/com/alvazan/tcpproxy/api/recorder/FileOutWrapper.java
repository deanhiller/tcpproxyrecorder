package com.alvazan.tcpproxy.api.recorder;


/**
 * This purely exists because we can't mock the java File interface :(
 * 
 * @author dhiller
 *
 */
public interface FileOutWrapper {

	public void open();
	
	public void write(byte[] contents);
	
	public void close();
}
