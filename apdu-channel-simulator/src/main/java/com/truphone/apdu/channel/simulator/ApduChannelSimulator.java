package com.truphone.apdu.channel.simulator;

import com.truphone.lpa.ApduChannel;
import com.truphone.lpa.ApduTransmittedListener;

import java.util.List;

public interface ApduChannelSimulator extends ApduChannel {

    /**
     * Define the expected result for a given APDU
     *
     * @param apdu   APDU used to invoke the {@link ApduChannelSimulator#transmitAPDU}
     * @param result Result that must be retrieved for given APDU
     */
    void setTransmitAPDUExpectation(String apdu, String result);

    /**
     * Define the expected result for a given list of APDUs
     *
     * @param apdus  APDUs list used to invoke the {@link ApduChannelSimulator#transmitAPDUS(List)}
     * @param result Result that must be retrieved for given APDUs list
     */
    void setTransmitAPDUsExpectation(List<String> apdus, String result);

    /**
     * Clear all expectations
     */
    void clearAllExpectations();

    ApduTransmittedListener getApduTransmittedListener();

}
