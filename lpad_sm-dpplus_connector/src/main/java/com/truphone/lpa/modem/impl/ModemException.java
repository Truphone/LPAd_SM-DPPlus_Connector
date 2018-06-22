package com.truphone.lpa.modem.impl;

public class ModemException extends RilException {
	public ModemException() {
	}

	public ModemException(final String message) {
		super(message);
	}

	public ModemException(final Throwable cause) {
		super(cause);
	}

	public ModemException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
