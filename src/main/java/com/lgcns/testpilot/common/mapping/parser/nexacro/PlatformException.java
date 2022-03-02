package com.lgcns.testpilot.common.mapping.parser.nexacro;

public class PlatformException extends Exception {
	private static final long serialVersionUID = -9053992308490260338L;

	public PlatformException() { 
//nothing todo
 }

	public PlatformException(final String message) {
		super(message);
	}

	public PlatformException(final String message, final Throwable cause) {
		super(message);
		initCause(cause);
	}
}