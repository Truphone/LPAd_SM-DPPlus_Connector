package com.truphone.lpa.modem.impl;

import com.fazecast.jSerialComm.SerialPort;
import com.truphone.lpa.ApduChannel;
import com.truphone.lpa.ApduTransmittedListener;
import com.truphone.lpa.apdu.ApduUtils;
import com.truphone.util.LogStub;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Modem implements ApduChannel {
    private static final String NO_DATA = "6D00";
    private static final String PROP_SUCCESS = "9900";
    private static final int TIMEOUT_MS = 25000;
    private static final String ISDR_AID = "01A4040010A0000005591010FFFFFFFF8900000100";
    private static final Logger LOG = Logger.getLogger(Modem.class.getName());

    private final SerialPort serialPort_;

    protected final ModemDescriptor descriptor_;
    public static final String SUCCESS_FLAG = "9000";

    public Modem(final ModemDescriptor descr) throws ModemException {
        descriptor_ = descr;
        try {
            serialPort_ = locateSerialPort();

        } catch (Exception e) {
            throw new ModemWithDescrException(descr, "Modem initialization failed", e);
        }
    }

    private SerialPort locateSerialPort() {
        if (!descriptor_.isSerialPortDefined()) {
            return null;
        }

        final SerialPort serialPort = descriptor_.locateSerialPort();
        if (serialPort == null) {
            throw new IllegalArgumentException(
                    String.format("Serial port not found (name: %s, descr. filter: %s, IMEI filter: %s",
                            descriptor_.getSerialPortName(), descriptor_.getSerialPortDescrRegex(),
                            descriptor_.getSerialPortImeiCheck()));
        } else {
            return serialPort;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {

            if (serialPort_ != null) {
                serialPort_.closePort();
            }
        } finally {
            super.finalize();
        }
    }

    public SerialPort getSerialPort() {
        return serialPort_;
    }

    private String proprietaryGetResponse(BufferedReader reader, PrintWriter writer) throws Exception {
        String selectResponseAPDU = getProprietaryResponseApdu();
        StringBuilder result = new StringBuilder();

        if (LogStub.getInstance().isDebugEnabled()) {
            LOG.fine("SELECT RESPONSE (Prop): " + selectResponseAPDU);
        }

        String atCommand = "AT+CSIM=" + selectResponseAPDU.length() + "," + selectResponseAPDU;

        if (LogStub.getInstance().isDebugEnabled()) {
            LOG.fine("AT COMMAND: " + atCommand);
        }

        String response = AtUtil.execSimpleAtCommand(atCommand, writer, reader, TIMEOUT_MS);

        response = AtUtil.parseResponse(response);

        if (LogStub.getInstance().isDebugEnabled()) {
            LOG.fine("RESPONSE: " + response);
        }

        while (!StringUtils.equals("6F", response.substring(response.length() - 4, response.length() - 2))) {
            result.append(response.substring(0, response.length() - 4));
            selectResponseAPDU = getProprietaryResponseApdu();
            String selectATCommand = "AT+CSIM=" + selectResponseAPDU.length() + "," + selectResponseAPDU;

            if (LogStub.getInstance().isDebugEnabled()) {
                LOG.fine("AT COMMAND: " + selectATCommand);
            }

            response = AtUtil.execSimpleAtCommand(selectATCommand, writer, reader, TIMEOUT_MS);

            if (LogStub.getInstance().isDebugEnabled()) {
                LOG.fine("RESPONSE: " + response);
            }

            response = AtUtil.parseResponse(response);
        }

        result.append("9000");

        if (LogStub.getInstance().isDebugEnabled()) {
            LOG.fine("RESULT: " + result.toString());
        }

        return result.toString();
    }

    private String getResponse(BufferedReader reader, PrintWriter writer) throws Exception {
        String selectResponseAPDU = ApduUtils.getResponse();
        String atCommand = "AT+CSIM=" + selectResponseAPDU.length() + "," + selectResponseAPDU;

        if (LogStub.getInstance().isDebugEnabled()) {
            LOG.fine("AT COMMAND: " + atCommand);
        }

        String response = AtUtil.execSimpleAtCommand(atCommand, writer, reader, TIMEOUT_MS);

        if (LogStub.getInstance().isDebugEnabled()) {
            LOG.fine("RESPONSE: " + response);
        }

        String parsedResult = AtUtil.parseResponse(response);

        if (LogStub.getInstance().isDebugEnabled()) {
            LOG.fine("RESULT: " + response);
        }

        return parsedResult;
    }

    public String transmitAPDUS(List<String> apdus) {

        if (LogStub.getInstance().isDebugEnabled()) {
            LOG.fine("Opening serial port");
        }

        descriptor_.openSerialPort(serialPort_, descriptor_.getSerialPortTimeOutMs());
        final BufferedReader reader = new BufferedReader(new InputStreamReader(serialPort_.getInputStream()));
        final PrintWriter writer = new PrintWriter(serialPort_.getOutputStream());
        try {
            AtUtil.flushReader(reader);

            if (LogStub.getInstance().isDebugEnabled()) {
                LOG.fine("Selecting ISD-R " + ISDR_AID);
            }

            String isdrResult = AtUtil.execSimpleAtCommand("AT+CSIM=42, " + ISDR_AID, writer, reader, TIMEOUT_MS);

            if (LogStub.getInstance().isDebugEnabled()) {
                LOG.fine("Selecting ISD-R Result " + isdrResult);
            }

            String response = "";
            for (int i = 0; i < apdus.size() - 1; i++) {
                String apdu = apdus.get(i);
                String atCommand = "AT+CSIM=" + apdu.length() + "," + apdu;

                if (LogStub.getInstance().isDebugEnabled()) {
                    LOG.fine("AT COMMAND: " + atCommand);
                }

                response = AtUtil.execSimpleAtCommand(atCommand, writer, reader, TIMEOUT_MS);

                if (LogStub.getInstance().isDebugEnabled()) {
                    LOG.fine("RESPONSE: " + response);
                }

                response = AtUtil.parseResponse(response);

                if (LogStub.getInstance().isDebugEnabled()) {
                    LOG.fine("RESULT: " + response);
                }

                if (response.endsWith(SUCCESS_FLAG)) {
                    if (LogStub.getInstance().isDebugEnabled()) {
                        LOG.fine("SUCESS: Response is 9000");
                    }
                } else {
                    throw new RuntimeException("error: apduPart response is not 9000");
                }
            }

            String apdu = apdus.get(apdus.size() - 1);
            String atCommand = "AT+CSIM=" + apdu.length() + "," + apdu;

            if (LogStub.getInstance().isDebugEnabled()) {
                LOG.fine("AT COMMAND: " + atCommand);
            }

            response = AtUtil.execSimpleAtCommand(atCommand, writer, reader, TIMEOUT_MS);

            if (LogStub.getInstance().isDebugEnabled()) {
                LOG.fine("FINAL RESPONSE: " + response);
            }

            response = AtUtil.parseResponse(response);

            if (LogStub.getInstance().isDebugEnabled()) {
                LOG.fine("FINAL RESULT: " + response);

            }
            if (PROP_SUCCESS.equals(response)) {
                if (LogStub.getInstance().isDebugEnabled()) {
                    LOG.fine("PROPRIETARY RESPONSE FLOW");
                }

                return proprietaryGetResponse(reader, writer);
            } else {
                if (LogStub.getInstance().isDebugEnabled()) {
                    LOG.fine("STANDARD RESPONSE FLOW");
                }

                String selectResponse = getResponse(reader, writer);

                if (NO_DATA.equals(selectResponse)) {
                    if (LogStub.getInstance().isDebugEnabled()) {
                        LOG.fine("Received: " + selectResponse + " converting to null response");
                    }
                    return null;
                } else {
                    return selectResponse;
                }
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error executing at commands", e);

            throw new RuntimeException("Unable to execute AT Command");
        } finally {
            serialPort_.closePort();
        }
    }

    public void sendStatus() {
        try {
            if (LogStub.getInstance().isDebugEnabled()) {
                LOG.fine("Send Status Command");
            }

            descriptor_.openSerialPort(serialPort_, descriptor_.getSerialPortTimeOutMs());
            final BufferedReader reader = new BufferedReader(new InputStreamReader(serialPort_.getInputStream()));
            final PrintWriter writer = new PrintWriter(serialPort_.getOutputStream());

            AtUtil.flushReader(reader);
            String apdu = ApduUtils.getSendStatusAPDU();

            String atCommand = "AT+CSIM=" + apdu.length() + "," + apdu;

            if (LogStub.getInstance().isDebugEnabled()) {
                LOG.fine("AT COMMAND: " + atCommand);
            }

            String response = AtUtil.execSimpleAtCommand(atCommand, writer, reader, TIMEOUT_MS);

            if (LogStub.getInstance().isDebugEnabled()) {
                LOG.fine("RESPONSE: " + response);
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error executing at commands", e);

            throw new RuntimeException("Unable to execute AT Command");

        } finally {
            serialPort_.closePort();
        }
    }

    @Override
    public void setApduTransmittedListener(ApduTransmittedListener apduTransmittedListener) {
    }

    @Override
    public void removeApduTransmittedListener(ApduTransmittedListener apduTransmittedListener) {
    }

    public String transmitAPDU(String apdu) {
        try {
            if (LogStub.getInstance().isDebugEnabled()) {
                LOG.fine("Sending APDU");
            }

            descriptor_.openSerialPort(serialPort_, descriptor_.getSerialPortTimeOutMs());
            final BufferedReader reader = new BufferedReader(new InputStreamReader(serialPort_.getInputStream()));
            final PrintWriter writer = new PrintWriter(serialPort_.getOutputStream());

            AtUtil.flushReader(reader);

            if (LogStub.getInstance().isDebugEnabled()) {
                LOG.fine("Selecting ISD-R " + ISDR_AID);
            }

            String isdrResult = AtUtil.execSimpleAtCommand("AT+CSIM=42, " + ISDR_AID, writer, reader, TIMEOUT_MS);

            if (LogStub.getInstance().isDebugEnabled()) {
                LOG.fine("ISD-R Result: " + isdrResult);
            }

            String atCommand = "AT+CSIM=" + apdu.length() + "," + apdu;

            if (LogStub.getInstance().isDebugEnabled()) {
                LOG.fine("AT COMMAND: " + atCommand);
            }

            String response = AtUtil.execSimpleAtCommand(atCommand, writer, reader, TIMEOUT_MS);

            if (LogStub.getInstance().isDebugEnabled()) {
                LOG.fine("RESPONSE: " + response);
            }

            response = AtUtil.parseResponse(response);

            if (LogStub.getInstance().isDebugEnabled()) {
                LOG.fine("RESULT: " + response);
            }

            String selectResponse = null;

            if (response.equals(PROP_SUCCESS)) {
                if (LogStub.getInstance().isDebugEnabled()) {
                    LOG.fine("PROPRIETARY RESPONSE FLOW");
                }

                selectResponse = proprietaryGetResponse(reader, writer);
            } else {
                if (LogStub.getInstance().isDebugEnabled()) {
                    LOG.fine("STANDARD RESPONSE FLOW");
                }

                selectResponse = getResponse(reader, writer);

                if (NO_DATA.equals(selectResponse)) {
                    if (LogStub.getInstance().isDebugEnabled()) {
                        LOG.fine("Received: " + selectResponse + " converting to null response");
                    }

                    return null;
                } else {
                    return selectResponse;
                }
            }

            return selectResponse;

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error executing at commands", e);

            throw new RuntimeException("Unable to execute AT Command");
        } finally {
            serialPort_.closePort();
        }
    }

    public String transmitATCommand(String at) {
        try {
            if (LogStub.getInstance().isDebugEnabled()) {
                LOG.fine("Sending AT Command");
            }

            descriptor_.openSerialPort(serialPort_, descriptor_.getSerialPortTimeOutMs());
            final BufferedReader reader = new BufferedReader(new InputStreamReader(serialPort_.getInputStream()));
            final PrintWriter writer = new PrintWriter(serialPort_.getOutputStream());

            AtUtil.flushReader(reader);
            String atCommand = "AT" + at;

            if (LogStub.getInstance().isDebugEnabled()) {
                LOG.fine("Sending AT Command: " + atCommand);
            }
            String response = AtUtil.execSimpleAtCommand(atCommand, writer, reader, TIMEOUT_MS);

            if (LogStub.getInstance().isDebugEnabled()) {
                LOG.fine("RESULT: " + response);
            }

            return response;

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error executing at commands", e);

            throw new RuntimeException("Unable to execute AT Command");

        } finally {
            serialPort_.closePort();
        }

    }

    public String getIP() {
        String eResponse = transmitATCommand("+CGPADDR=1");
        return eResponse;
    }

    public String getMSISDN() {
        String eResponse = transmitATCommand("+CNUM");
        return eResponse;
    }

    public String getServiceProvider() {
        String eResponse = transmitATCommand("#SPN");
        return eResponse;
    }

    public String getICCID() {
        String eResponse = transmitATCommand("#CCID");
        return eResponse;
    }

    public String getServiceQuality() {
        String eResponse = transmitATCommand("+CSQ");
        return eResponse;
    }

    public String getManufacturerIdentification() {
        String eResponse = transmitATCommand("+GMI");
        return eResponse;
    }

    public String getModelIdentification() {
        String eResponse = transmitATCommand("+GMM");
        return eResponse;
    }

    public String getModelRevision() {
        String eResponse = transmitATCommand("+GMR");
        return eResponse;
    }

    public String getModelSerial() {
        String eResponse = transmitATCommand("+GSN");
        return eResponse;
    }

    public String getAPN() {
        String eResponse = transmitATCommand("+CGDCONT?");
        return eResponse;
    }

    public String getIMSI() {
        String eResponse = transmitATCommand("#CIMI");
        return eResponse;
    }

    public String getRegistrationStatus() {
        String eResponse = transmitATCommand("+CREG?");
        return eResponse;
    }

    private String getProprietaryResponseApdu() {
        StringBuilder apdu = new StringBuilder();
        apdu.append("D0").append("C0").append("00").append("00").append("00");
        return apdu.toString();
    }

    public String getIMEI() {
        String eResponse = transmitATCommand("+CGSN");
        return eResponse;
    }

    public String getMCC() {
        String eResponse = transmitATCommand("#RFSTS");
        eResponse = eResponse.replaceAll("#RFSTS: ", "");
        String[] attributes = eResponse.split(",");
        if (attributes.length > 0) {
            String mccMnc = attributes[0].replace("\"", "");
            String mcc = mccMnc.split(" ")[0];
            return mcc;
        }
        return "";
    }

}
