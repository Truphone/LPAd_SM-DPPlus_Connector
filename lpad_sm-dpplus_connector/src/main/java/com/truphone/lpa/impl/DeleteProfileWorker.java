package com.truphone.lpa.impl;

import com.truphone.lpa.ApduChannel;
import com.truphone.lpa.apdu.ApduUtils;
import com.truphone.lpa.dto.asn1.rspdefinitions.DeleteProfileResponse;
import com.truphone.lpa.progress.Progress;
import com.truphone.lpa.progress.ProgressStep;
import com.truphone.util.LogStub;
import com.truphone.util.ToTLV;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

class DeleteProfileWorker {
    private static final Logger LOG = Logger.getLogger(DeleteProfileWorker.class.getName());

    private final String iccid;
    private final Progress progress;
    private final ApduChannel apduChannel;

    DeleteProfileWorker(String iccid, Progress progress, ApduChannel apduChannel) {

        this.iccid = iccid;
        this.progress = progress;
        this.apduChannel = apduChannel;
    }

    String run() {
        String eResponse = transmitDeleteProfile(iccid, progress);

        return convertDeleteProfile(iccid, progress, eResponse);
    }

    private String convertDeleteProfile(String iccid, Progress progress, String eResponse) {

        progress.stepExecuted(ProgressStep.DELETE_PROFILE_CONVERTING_RESPONSE, "Converting response");

        DeleteProfileResponse deleteProfileResponse = new DeleteProfileResponse();

        try {
            InputStream is = new ByteArrayInputStream(Hex.decodeHex(eResponse.toCharArray()));

            deleteProfileResponse.decode(is);


            if (LogStub.getInstance().isDebugEnabled()) {
                LogStub.getInstance().logDebug(LOG, LogStub.getInstance().getTag() + " - Delete response: " + deleteProfileResponse);
            }

            if (LocalProfileAssistantImpl.PROFILE_RESULT_SUCESS.equals(deleteProfileResponse.getDeleteResult().toString())) {
                if (LogStub.getInstance().isDebugEnabled()) {
                    LogStub.getInstance().logDebug(LOG, LogStub.getInstance().getTag() + " - iccid:" + iccid + " profile deleted");
                    LogStub.getInstance().logDebug(LOG, LogStub.getInstance().getTag() + " - iccid:" + iccid + " Refreshing SIM card on Delete.");
                }

                apduChannel.sendStatus();

                progress.stepExecuted(ProgressStep.DELETE_PROFILE_DELETED, iccid + " deleted successfully");
            } else {
                progress.stepExecuted(ProgressStep.DELETE_PROFILE_NOT_DELETED, iccid + " profile not deleted");

                LOG.info(LogStub.getInstance().getTag() + " - iccid:" + iccid + " profile not deleted");
            }

            return deleteProfileResponse.getDeleteResult().toString();
        } catch (IOException ioe) {
            LOG.severe(LogStub.getInstance().getTag() + " - iccid:" + iccid + " profile failed to be deleted");

            throw new RuntimeException("Unable to delete profile: " + iccid + ", response: " + eResponse);
        } catch (DecoderException e) {
            LOG.log(Level.SEVERE, LogStub.getInstance().getTag() + " - " + e.getMessage(), e);
            LOG.log(Level.SEVERE, LogStub.getInstance().getTag() + " - iccid: " + iccid + " profile failed to be deleted. Exception in Decoder:" + e.getMessage());

            throw new RuntimeException("Unable to delete profile: " + iccid + ", response: " + eResponse);
        }
    }

    private String transmitDeleteProfile(String iccid, Progress progress) {

        progress.setTotalSteps(3);
        progress.stepExecuted(ProgressStep.DELETE_PROFILE_DELETING_PROFILE, iccid + " delete profile");

        if (LogStub.getInstance().isDebugEnabled()) {
            LogStub.getInstance().logDebug(LOG, LogStub.getInstance().getTag() + " - Deleting profile: " + iccid);
        }

        String apdu = ApduUtils.deleteProfileApdu(iccid);

        if (LogStub.getInstance().isDebugEnabled()) {
            LogStub.getInstance().logDebug(LOG, LogStub.getInstance().getTag() + " - Delete profile apdu: " + apdu);
        }

        String eResponse = apduChannel.transmitAPDU(apdu);

        if (LogStub.getInstance().isDebugEnabled()) {
            LogStub.getInstance().logDebug(LOG, LogStub.getInstance().getTag() + " - Delete Response: " + eResponse);
        }

        return eResponse;
    }

    public static void main(String[] args) {

        String x =  ToTLV.toTLV("BF33", ToTLV.toTLV("80", "00"));

        new DeleteProfileWorker(null, null, null).convertDeleteProfile("meuICCID", x);

    }

    public String convertDeleteProfile(String iccid, String eResponse) {


        DeleteProfileResponse deleteProfileResponse = new DeleteProfileResponse();

        try {
            InputStream is = new ByteArrayInputStream(Hex.decodeHex(eResponse.toCharArray()));

            deleteProfileResponse.decode(is);




            if (LocalProfileAssistantImpl.PROFILE_RESULT_SUCESS.equals(deleteProfileResponse.getDeleteResult().toString())) {
                if (LogStub.getInstance().isDebugEnabled()) {
                    LogStub.getInstance().logDebug(LOG, LogStub.getInstance().getTag() + " - iccid:" + iccid + " profile deleted");
                    LogStub.getInstance().logDebug(LOG, LogStub.getInstance().getTag() + " - iccid:" + iccid + " Refreshing SIM card on Delete.");
                }

//                apduChannel.sendStatus();

            } else {

                LOG.info(LogStub.getInstance().getTag() + " - iccid:" + iccid + " profile not deleted");
            }

            return deleteProfileResponse.getDeleteResult().toString();
        } catch (IOException ioe) {
            LOG.severe(LogStub.getInstance().getTag() + " - iccid:" + iccid + " profile failed to be deleted");

            throw new RuntimeException("Unable to delete profile: " + iccid + ", response: " + eResponse);
        } catch (DecoderException e) {
            LOG.log(Level.SEVERE, LogStub.getInstance().getTag() + " - " + e.getMessage(), e);
            LOG.log(Level.SEVERE, LogStub.getInstance().getTag() + " - iccid: " + iccid + " profile failed to be deleted. Exception in Decoder:" + e.getMessage());

            throw new RuntimeException("Unable to delete profile: " + iccid + ", response: " + eResponse);
        }
    }
}
