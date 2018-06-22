package com.truphone.lpa.modem.impl;

public class ModemTimeoutException extends ModemException {

	public ModemTimeoutException() {
		super("Modem time-out");
	}

	public ModemTimeoutException(final String message) {
		super(message);
	}

	public ModemTimeoutException(final Throwable cause) {
		super(cause);
	}

	public ModemTimeoutException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
