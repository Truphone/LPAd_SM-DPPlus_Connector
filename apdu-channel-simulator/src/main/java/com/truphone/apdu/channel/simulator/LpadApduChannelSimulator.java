package com.truphone.apdu.channel.simulator;

import com.truphone.apdu.channel.simulator.persistence.ExpectationPersistence;
import com.truphone.lpa.ApduTransmittedListener;

import java.util.List;

public class LpadApduChannelSimulator implements ApduChannelSimulator {
    private final ExpectationPersistence expectationPersistence;
    private ApduTransmittedListener apduTransmittedListener;

    public LpadApduChannelSimulator(final ExpectationPersistence expectationPersistence) {
        if (expectationPersistence == null) {
            throw new IllegalArgumentException("Expectation persistence must be defined");
        }

        this.apduTransmittedListener = null;
        this.expectationPersistence = expectationPersistence;
    }

    public void setTransmitAPDUExpectation(final String input, final String result) {
        this.expectationPersistence.storeExpectationMapping(input, result);
    }

    public void setTransmitAPDUsExpectation(final List<String> input, final String result) {
        this.expectationPersistence.storeExpectationMapping(input, result);
    }

    public void clearAllExpectations() {
        this.expectationPersistence.clear();
    }

    public String transmitAPDU(final String s) {
        notifyApduListener();
        return this.expectationPersistence.getValueOfAnExpectation(s);
    }

    public String transmitAPDUS(final List<String> list) {
        notifyApduListener();
        return this.expectationPersistence.getValueOfAnExpectation(list);
    }

    public void sendStatus() {
        notifyApduListener();
    }

    public void setApduTransmittedListener(final ApduTransmittedListener apduTransmittedListener) {
        this.apduTransmittedListener = apduTransmittedListener;
    }

    public ApduTransmittedListener getApduTransmittedListener() {
        return apduTransmittedListener;
    }

    public void removeApduTransmittedListener(final ApduTransmittedListener apduTransmittedListener) {
        if (apduTransmittedListener != null) {
            this.apduTransmittedListener = null;
        }
    }

    private void notifyApduListener() {
        if(this.apduTransmittedListener != null) {
            apduTransmittedListener.onApduTransmitted();
        }
    }
}
