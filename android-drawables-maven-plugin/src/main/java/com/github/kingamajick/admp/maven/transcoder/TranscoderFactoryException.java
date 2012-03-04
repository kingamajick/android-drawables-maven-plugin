package com.github.kingamajick.admp.maven.transcoder;

public class TranscoderFactoryException extends Exception {

	private static final long serialVersionUID = 1L;

	public TranscoderFactoryException() {
	}

	public TranscoderFactoryException(final String message) {
		super(message);
	}

	public TranscoderFactoryException(final Throwable cause) {
		super(cause);
	}

	public TranscoderFactoryException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
