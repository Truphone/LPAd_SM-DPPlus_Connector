package com.truphone.lpa.impl;

import com.truphone.lpa.ApduChannel;
import com.truphone.lpa.apdu.ApduUtils;
import com.truphone.lpa.progress.Progress;
import com.truphone.lpa.progress.ProgressStep;
import com.truphone.lpa.dto.asn1.rspdefinitions.GetEuiccDataResponse;
import com.truphone.util.LogStub;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GetEidWorker {
    private static final Logger LOG = Logger.getLogger(GetEidWorker.class.getName());

    private final Progress progress;
    private final ApduChannel apduChannel;

    GetEidWorker(Progress progress, ApduChannel apduChannel) {

        this.progress = progress;
        this.apduChannel = apduChannel;
    }

    String run() {

        progress.setTotalSteps(3);
        progress.stepExecuted(ProgressStep.GET_EID_RETRIEVING, "getEID retrieving...");

        if (LogStub.getInstance().isDebugEnabled()) {
            LogStub.getInstance().logDebug(LOG, LogStub.getInstance().getTag() + " - Getting EID");
        }

        String eidApdu = ApduUtils.getEIDApdu();

        if (LogStub.getInstance().isDebugEnabled()) {
            LogStub.getInstance().logDebug(LOG, LogStub.getInstance().getTag() + " - EID APDU: " + eidApdu);
        }

        String eidapduResponseStr = apduChannel.transmitAPDU(eidApdu);

        if (LogStub.getInstance().isDebugEnabled()) {
            LogStub.getInstance().logDebug(LOG, LogStub.getInstance().getTag() + " - Response: " + eidapduResponseStr);
        }

        return convertGetEuiccData(eidapduResponseStr, progress);
    }

    private String convertGetEuiccData(String eidapduResponseStr, Progress progress) {

        progress.stepExecuted(ProgressStep.GET_EID_CONVERTING, "getEID converting...");

        GetEuiccDataResponse eidResponse = new GetEuiccDataResponse();

        try {
            if (LogStub.getInstance().isDebugEnabled()) {
                LogStub.getInstance().logDebug(LOG, LogStub.getInstance().getTag() + " - Decoding response: " + eidapduResponseStr);
            }

            InputStream is = new ByteArrayInputStream(Hex.decodeHex(eidapduResponseStr.toCharArray()));

            if (LogStub.getInstance().isDebugEnabled()) {
                LogStub.getInstance().logDebug(LOG, LogStub.getInstance().getTag() + " - Decoding with GetEuiccDataResponse");
            }

            eidResponse.decode(is, true);

            if (LogStub.getInstance().isDebugEnabled()) {
                LogStub.getInstance().logDebug(LOG, LogStub.getInstance().getTag() + " - EID is: " + eidResponse.getEidValue().toString());
            }

            progress.stepExecuted(ProgressStep.GET_EID_CONVERTED, "getEID converted...");

            return eidResponse.getEidValue().toString();
        } catch (DecoderException e) {
            LOG.log(Level.SEVERE, LogStub.getInstance().getTag() + " - " + e.getMessage(), e);
            LOG.log(Level.SEVERE, LogStub.getInstance().getTag() + " -  Unable to retrieve EID. Exception in Decoder:" + e.getMessage());

            throw new RuntimeException("Unable to retrieve EID");
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, LogStub.getInstance().getTag() + " - " + ioe.getMessage(), ioe);

            throw new RuntimeException("Invalid EID response, unable to retrieve EID");
        }
    }
}
