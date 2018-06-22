package com.truphone.lpa.modem.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;


public class AtUtil {
    private static final Logger LOG = Logger.getLogger(AtUtil.class.getName());

    private static final String AT_CGSN = "AT+CGSN";
    public static final String OK = "ok";
    public static final String CME_ERROR = "cme error";
    public static final String ERROR = "error";
    public static final String EOF = "<eof>";

    private static final int DATA_WAITING_CHECK_PERIOD_MS = Integer
            .parseInt(System.getProperty(AtUtil.class.getName()
                    + ".check_period_ms", "10"));

    public static void send(final PrintWriter writer, final String line) {

        LOG.finest("sending: " + line);

        writer.print(line);
        writer.print("\r");
        writer.flush();
    }

    public static String receive(final BufferedReader reader,
                                 final int timeOutMs) throws IOException, ModemTimeoutException {
        int tries = timeOutMs / DATA_WAITING_CHECK_PERIOD_MS;
        while (!reader.ready()) {
            try {
                Thread.sleep(DATA_WAITING_CHECK_PERIOD_MS);
            } catch (InterruptedException e) {
                LOG.severe("reception time-out (sleep interrupted)");

                throw new ModemTimeoutException(
                        "waiting for serial port data interrupted");
            }

            if (timeOutMs >= 0 && tries-- <= 0) {
                LOG.severe("reception time-out");

                throw new ModemTimeoutException(
                        "time-out waiting for serial port data");
            }
        }

        final String line = reader.readLine();
        LOG.fine("Received from Modem: " + line);

        return line;
    }

    public static List<String> receiveAll(final BufferedReader reader,
                                          final int timeOutMs) throws IOException, ModemTimeoutException {
        final ArrayList<String> lines = new ArrayList<String>();
        boolean firstLineReceived = false;
        while (true) {
            final String line = receive(reader, firstLineReceived ? timeOutMs : timeOutMs );
            firstLineReceived = true;
            if (line == null) {
                lines.add(EOF);
                break;
            } else {
                lines.add(line);
            }

            final String lowerCaseLine = line.toLowerCase(Locale.ENGLISH);
            if (lowerCaseLine.startsWith(OK) || lowerCaseLine.startsWith(ERROR)
                    || lowerCaseLine.startsWith(CME_ERROR)) {
                break;
            }
        }

        return lines;
    }

    public static void flushReader(final BufferedReader reader) throws IOException {
        try {
            LOG.fine("Flushing input...");

            receiveAll(reader, 100);
        } catch (ModemTimeoutException e) {
        }
    }

    public static List<String> execAtCommand(final String command,
                                             final PrintWriter writer, final BufferedReader reader,
                                             final int timeOutMs) throws IOException, ModemTimeoutException {
        send(writer, command);
        return receiveAll(reader, timeOutMs);
    }

    public static boolean isSuccessful(final List<String> commandResponse) {
        return !commandResponse.isEmpty()
                && OK.equalsIgnoreCase(commandResponse.get(commandResponse.size() - 1));
    }

    public static String describeStatus(final List<String> commandResponse) {
        if (commandResponse.isEmpty()) {
            return "empty response";
        }

        final String lastResponseLine = commandResponse.get(commandResponse
                .size() - 1);
        if (lastResponseLine.startsWith(CME_ERROR)) {
            return lastResponseLine.charAt(CME_ERROR.length()) == ':' ? "terminal error: "
                    + lastResponseLine.substring(CME_ERROR.length() + 1).trim()
                    : "terminal error w/o additional info";
        } else if (lastResponseLine.equalsIgnoreCase(ERROR)) {
            return "general error";
        } else if (lastResponseLine.equalsIgnoreCase(OK)) {
            return "no error";
        } else if (lastResponseLine.equalsIgnoreCase(EOF)) {
            return "premature end of transmission (incomplete response)";
        } else {
            return "unknown status: " + lastResponseLine;
        }
    }

    public static List<String> filterResponseLines(final String command,
                                                   final List<String> commandResponse) {
        boolean captureLines = false;
        final List<String> filteredResponse = new ArrayList<String>();
        for (final String responseLine : commandResponse) {
            if (responseLine.isEmpty() || responseLine.charAt(0) == '^') {
                continue;
            }

            if (responseLine.equals(command)) {
                captureLines = true;
            } else if (captureLines) {
                filteredResponse.add(responseLine.trim());
            }
        }

        return filteredResponse;
    }

    public static String getSingleResponseLine(final String command,
                                               final List<String> commandResponse) {
        final List<String> filteredResponse = filterResponseLines(command,
                commandResponse);
        return filteredResponse.size() < 2 ? null : filteredResponse.get(0);
    }

    public static String execSimpleAtCommand(final String command,
                                             final PrintWriter writer, final BufferedReader reader,
                                             final int timeOutMs) throws IOException, ModemTimeoutException {
        List<String> commandResponse = Collections.emptyList();
        try {
            commandResponse = execAtCommand(command, writer, reader, timeOutMs);
            if (isSuccessful(commandResponse)) {
                return getSingleResponseLine(command, commandResponse);
            } else {
                throw new IllegalStateException(
                        "command execution unsuccessful"
                                + describeStatus(commandResponse));
            }
        } catch (Exception e) {
            throw new IOException(String.format(
                    "Failed to execute simple AT command (%s): %s", command,
                    describeStatus(commandResponse)), e);
        }
    }

    public static String execAtCgsn(final PrintWriter writer,
                                    final BufferedReader reader, final int timeOutMs)
            throws IOException, ModemTimeoutException {
        return execSimpleAtCommand(AT_CGSN, writer, reader, timeOutMs);
    }

    public static String parseResponse(String response) {

        if (response == null || !response.contains("+CSIM")) {
            return response;
        }

        int start = response.indexOf("\"");
        int end = response.indexOf("\"", start + 1);

        return response.substring(start + 1, end);
    }

}
