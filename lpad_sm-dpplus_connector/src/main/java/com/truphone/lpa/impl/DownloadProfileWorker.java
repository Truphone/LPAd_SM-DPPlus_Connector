package com.truphone.lpa.impl;

import com.truphone.es9plus.Es9PlusImpl;
import com.truphone.lpa.ApduChannel;
import com.truphone.lpa.impl.download.*;
import com.truphone.lpa.progress.DownloadProgress;
import com.truphone.util.LogStub;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

class DownloadProfileWorker {
    private static final Logger LOG = Logger.getLogger(DownloadProfileWorker.class.getName());

    private final DownloadProgress progress;
    private final Es9PlusImpl es9Module;
    private String matchingId;
    private ApduTransmitter apduTransmitter;

    DownloadProfileWorker(String matchingId, DownloadProgress progress, ApduChannel apduChannel, Es9PlusImpl es9Module) {

        this.matchingId = matchingId;
        this.progress = progress;
        this.es9Module = es9Module;
        apduTransmitter = new ApduTransmitter(apduChannel);
    }

    void run() throws Exception {
        AuthenticatingPhaseWorker authenticatingPhaseWorker = new AuthenticatingPhaseWorker(progress, apduTransmitter, es9Module);
        DownloadPhaseWorker downloadPhaseWorker = new DownloadPhaseWorker(progress, apduTransmitter, es9Module);

        LOG.info(LogStub.getInstance().getTag() + " - Downloading profile with matching Id: " + matchingId);

        InitialAuthenticationKeys initialAuthenticationKeys = new InitialAuthenticationKeys(matchingId,
                new ConnectingPhaseWorker(progress,
                        apduTransmitter).getEuiccConfiguredAddress(matchingId),
                authenticatingPhaseWorker.getEuiccInfo(),
                authenticatingPhaseWorker.getEuiccChallenge(matchingId));

        authenticatingPhaseWorker.initiateAuthentication(initialAuthenticationKeys);
        downloadAndInstallProfilePackage(initialAuthenticationKeys,
                downloadPhaseWorker.prepareDownload(authenticatingPhaseWorker.authenticateClient(initialAuthenticationKeys,
                        authenticatingPhaseWorker.authenticateWithEuicc(initialAuthenticationKeys))), downloadPhaseWorker);
    }


    private void downloadAndInstallProfilePackage(InitialAuthenticationKeys initialAuthenticationKeys,
                                                  String encodedPrepareDownloadResponse, DownloadPhaseWorker downloadPhaseWorker) throws IOException {
        String bpp = downloadPhaseWorker.getBoundProfilePackage(initialAuthenticationKeys, encodedPrepareDownloadResponse);
        Map<SbppApdu, List<String>> sbpp = new GeneratePhaseWorker(progress).generateSbpp(bpp);

        new InstallationPhaseWorker(progress, apduTransmitter).loadingSbppApdu(sbpp);
    }
}
