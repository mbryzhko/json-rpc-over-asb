package org.bma.asb.support;

public class AsbException extends RuntimeException {

	private static final long serialVersionUID = 8311528639698685767L;

	public AsbException() {
		super();
	}

	public AsbException(String message, Throwable cause) {
		super(message, cause);
	}

	public AsbException(String message) {
		super(message);
	}

	public AsbException(Throwable cause) {
		super(cause);
	}

}
