package com.alvazan.tcpproxy.api.recorder;

public class FileNotFound extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FileNotFound() {
	}

	public FileNotFound(String message) {
		super(message);
	}

	public FileNotFound(Throwable cause) {
		super(cause);
	}

	public FileNotFound(String message, Throwable cause) {
		super(message, cause);
	}

}
