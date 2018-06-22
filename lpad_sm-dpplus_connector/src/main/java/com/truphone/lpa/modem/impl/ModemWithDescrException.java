package com.truphone.lpa.modem.impl;

public class ModemWithDescrException extends ModemException {

	private static final long serialVersionUID = 7064838017312196666L;

	private static final String makeMessage(final String message,
			final ModemDescriptor descr) {
		return String.format("%s [%s, id=%s, port={%s,%s,%s}, ni={%s,%s)]",
				(message == null ? "Modem error" : message), descr.getKind(),
				descr.getMbnId(), descr.getSerialPortName(),
				descr.getSerialPortDescrRegex(),
				descr.getSerialPortImeiCheck(), descr.getNiMacAddressRegex(),
				descr.getNiNameRegex());
	}

	public ModemWithDescrException(final ModemDescriptor descr) {
		this(descr, null, null);
	}

	public ModemWithDescrException(final ModemDescriptor descr,
			final String message) {
		this(descr, message, null);
	}

	public ModemWithDescrException(final ModemDescriptor descr,
			final Throwable cause) {
		this(descr, null, cause);
	}

	public ModemWithDescrException(final ModemDescriptor descr,
			final String message, final Throwable cause) {
		super(makeMessage(message, descr), cause);
	}

}
