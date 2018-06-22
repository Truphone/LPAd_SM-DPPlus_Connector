package com.truphone.rspes29;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.truphone.rspes29.packet.message.request.AuthenticateClientReq;
import com.truphone.rspes29.packet.message.request.GetBoundProfilePackageReq;
import com.truphone.rspes29.packet.message.request.HandleNotificationReq;
import com.truphone.rspes29.packet.message.request.InitiateAuthenticationReq;
import com.truphone.rspes29.packet.message.response.AuthenticateClientResp;
import com.truphone.rspes29.packet.message.response.GetBoundProfilePackageResp;
import com.truphone.rspes29.packet.message.response.InitiateAuthenticationResp;
import com.truphone.util.LogStub;

import java.util.logging.Level;
import java.util.logging.Logger;


public class RspEs29Module {
    private static final Gson GS = new GsonBuilder().disableHtmlEscaping().create();
    private static final Logger LOG = Logger.getLogger(RspEs29Module.class.getName());
    private static final String INITIATE_AUTHENTICATION_PATH = "/gsma/rsp2/es9plus/initiateAuthentication";
    private static final String AUTHENTICATE_CLIENT_PATH = "/gsma/rsp2/es9plus/authenticateClient";
    private static final String GET_BOUND_PROFILE_PACKAGE_PATH = "/gsma/rsp2/es9plus/getBoundProfilePackage";
    private static final String HANDLE_NOTIFICATION_PATH = "/gsma/rsp2/es9plus/handleNotification";

    private String rspServerUrl;

    public void configure(String rspServerUrl) {

        this.rspServerUrl = rspServerUrl;
    }

    public InitiateAuthenticationResp initiateAuthentication(String euiccChallenge, String euiccInfo1,
                                                             String smdpAddress) {
        try {
            InitiateAuthenticationReq initiateAuthenticationReq = new InitiateAuthenticationReq();
            initiateAuthenticationReq.setEuiccChallenge(euiccChallenge);
            initiateAuthenticationReq.setEuiccInfo1(euiccInfo1);
            initiateAuthenticationReq.setSmdpAddress(smdpAddress);
            String body = GS.toJson(initiateAuthenticationReq);

            if (LogStub.getInstance().isDebugEnabled()) {
                LogStub.getInstance().logDebug(LOG, "RSP Request: " + body);
            }

            HttpResponse result = new HttpRSPClient().clientRSPRequest(body, rspServerUrl, INITIATE_AUTHENTICATION_PATH);
            if (result != null && !"".equals(result.getContent())) {
                String response = toJsonString(result.getContent());
                if (LogStub.getInstance().isDebugEnabled()) {
                    LogStub.getInstance().logDebug(LOG, "RSP Response: " + response);
                }
                InitiateAuthenticationResp initiateAuthenticationResp = GS.fromJson(response, InitiateAuthenticationResp.class);
                return initiateAuthenticationResp;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error contacting RSP Server", e);

            throw new RuntimeException("Unable to communicate with RSP Server");
        }
        return null;
    }

    public AuthenticateClientResp authenticateClient(String transactionId, String authenticateServerResponse) {
        try {
            AuthenticateClientReq authenticateClientReq = new AuthenticateClientReq();
            authenticateClientReq.setTransactionId(transactionId);
            authenticateClientReq.setAuthenticateServerResponse(authenticateServerResponse);
            String body = GS.toJson(authenticateClientReq);

            if (LogStub.getInstance().isDebugEnabled()) {
                LogStub.getInstance().logDebug(LOG, "RSP Request: " + body);
            }

            HttpResponse result = new HttpRSPClient().clientRSPRequest(body, rspServerUrl, AUTHENTICATE_CLIENT_PATH);
            if (result != null && !"".equals(result.getContent())) {
                String response = toJsonString(result.getContent());

                if (LogStub.getInstance().isDebugEnabled()) {
                    LogStub.getInstance().logDebug(LOG, "RSP Response: " + response);
                }

                return GS.fromJson(response, AuthenticateClientResp.class);
            } else {
                LOG.severe("Error contacting RSP Server");

                throw new RuntimeException("Unable to communicate with RSP Server");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error contacting RSP Server", e);

            throw new RuntimeException("Unable to communicate with RSP Server");
        }
    }

    public GetBoundProfilePackageResp getBoundProfilePackage(String transactionId, String prepareDownloadResponse) {
        try {
            GetBoundProfilePackageReq getBoundProfilePackageReq = new GetBoundProfilePackageReq();
            getBoundProfilePackageReq.setTransactionId(transactionId);
            getBoundProfilePackageReq.setPrepareDownloadResponse(prepareDownloadResponse);
            String body = GS.toJson(getBoundProfilePackageReq);

            if (LogStub.getInstance().isDebugEnabled()) {
                LogStub.getInstance().logDebug(LOG, "RSP Request: " + body);
            }

            HttpResponse result = new HttpRSPClient().clientRSPRequest(body, rspServerUrl, GET_BOUND_PROFILE_PACKAGE_PATH);
            if (result != null && !"".equals(result.getContent())) {
                String response = toJsonString(result.getContent());

                if (LogStub.getInstance().isDebugEnabled()) {
                    LogStub.getInstance().logDebug(LOG, "RSP Response: " + response);
                }

                return GS.fromJson(response, GetBoundProfilePackageResp.class);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error contacting RSP Server", e);

            throw new RuntimeException("Unable to communicate with RSP Server");
        }

        return null;
    }

    /**
     * ES9+.handleNotification
     */
    public void handleNotification(String pendingNotification) {
        try {
            HandleNotificationReq handleNotificationReq = new HandleNotificationReq();

            handleNotificationReq.setPendingNotification(pendingNotification);

            String body = GS.toJson(handleNotificationReq);

            if (LogStub.getInstance().isDebugEnabled()) {
                LogStub.getInstance().logDebug(LOG, "RSP Request: " + body);
            }

            HttpResponse result = new HttpRSPClient().clientRSPRequest(body, rspServerUrl, HANDLE_NOTIFICATION_PATH);

            if (result != null && result.getStatusCode() == 204) {
                if (LogStub.getInstance().isDebugEnabled()) {
                    LogStub.getInstance().logDebug(LOG, "RSP Response was 204 ");
                }

            } else {
                LOG.severe("Error contacting RSP Server or not 204: " + result);

                throw new RuntimeException("Unable to handle notification with RSP Server");
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error contacting RSP Server", e);

            throw new RuntimeException("Unable to handle notification with RSP Server");
        }
    }

    private String toJsonString(String msg) {
        int index = msg.indexOf("{");

        return msg.substring(index);
    }
}