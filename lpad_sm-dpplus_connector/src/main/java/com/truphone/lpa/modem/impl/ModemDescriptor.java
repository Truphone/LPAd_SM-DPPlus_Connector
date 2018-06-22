package com.truphone.lpa.modem.impl;

import com.fazecast.jSerialComm.SerialPort;
import com.truphone.util.PropUtil;
import com.truphone.util.TextUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;


public class ModemDescriptor {

    private static final String IMEI_PREFIX_IN_ATI = "imei: ";
    private static final Logger LOG = Logger.getLogger(ModemDescriptor.class
            .getName());

    /**
     * Kind of the modem.
     */
    public enum Kind {
        /**
         * Roaming modem kind.
         */
        ROAMING_MODEM,
        /**
         * HotSpot modem kind.
         */
        HOTSPOT_MODEM,
    }

    /**
     * Modem kind.
     */
    private final Kind kind_;
    /**
     * Either Mobile Broadband interface or device ID.
     */
    private final String mbnId_;
    /**
     * Name of the serial port related to the modem.
     */
    private final String serialPortName_;
    /**
     * Regex for matching the description of the serial port of the modem.
     */
    private final Pattern serialPortDescrRegex_;
    /**
     * IMEI checking mode for the modem's serial port detection.
     */
    private final String serialPortImeiCheck_;
    /**
     * Regex for matching the MAC address of the network interface of the modem.
     */
    private final Pattern niMacAddressRegex_;
    /**
     * Regex for matching the display name of the network interface of the
     * modem.
     */
    private final Pattern niNameRegex_;

    /**
     * Serial port for AT commands: baud rate in BPS.
     */
    private final int serialPortBaudRate_;
    /**
     * Serial port for AT commands: parity configuration.
     */
    private final int serialPortParity_;
    /**
     * Serial port for AT commands: number of data bits.
     */
    private final int serialPortDataBits_;
    /**
     * Serial port for AT commands: number of stop bits.
     */
    private final int serialPortStopBits_;
    /**
     * Serial port for AT commands: I/O timeout (millis).
     */
    private final int serialPortTimeOutMs_;

    /**
     * Default APN.
     */
    private final String apn_;
    /**
     * Default APN user name.
     */
    private final String apnUser_;
    /**
     * Default APN password.
     */
    private final String apnPassword_;

    /**
     * 'none'
     */
    public static final String NONE = "none";
    /**
     * 'auto'
     */
    public static final String AUTO = "auto";
    /**
     * 'by_descr'
     */
    public static final String BY_DESCR = "by_descr";

    /**
     * Property: MBN ID (={@value #PROP_MBN_ID}).
     */
    public static final String PROP_MBN_ID = "mbn_id";
    /**
     * Property: network interface MAC regex (={@value #PROP_NI_MAC_REGEX}).
     */
    public static final String PROP_NI_MAC_REGEX = "ni.mac_regex";
    /**
     * Property: network interface name regex (={@value #PROP_NI_NAME_REGEX}).
     */
    public static final String PROP_NI_NAME_REGEX = "ni.name_regex";
    /**
     * Property: serial port name (={@value #PROP_SERIAL_PORT}).
     */
    public static final String PROP_SERIAL_PORT = "serial_port";
    /**
     * Property: serial port IMEI checking mode (=
     * {@value #PROP_SERIAL_PORT_IMEI_CHECK}).
     */
    public static final String PROP_SERIAL_PORT_IMEI_CHECK = "serial_port.imei_check";
    /**
     * Property: serial port description regex (=
     * {@value #PROP_SERIAL_PORT_DESCR_REGEX}).
     */
    public static final String PROP_SERIAL_PORT_DESCR_REGEX = "serial_port.descr_regex";
    /**
     * Property: serial port baud rate (={@value #PROP_SERIAL_PORT_BAUD_RATE}).
     */
    public static final String PROP_SERIAL_PORT_BAUD_RATE = "serial_port.baud_rate";
    /**
     * Property: serial port data bits count (=
     * {@value #PROP_SERIAL_PORT_DATA_BITS}).
     */
    public static final String PROP_SERIAL_PORT_DATA_BITS = "serial_port.data_bits";
    /**
     * Property: serial port stop bits count (=
     * {@value #PROP_SERIAL_PORT_STOP_BITS}).
     */
    public static final String PROP_SERIAL_PORT_STOP_BITS = "serial_port.stop_bits";
    /**
     * Property: serial port parity configuration (=
     * {@value #PROP_SERIAL_PORT_PARITY}).
     */
    public static final String PROP_SERIAL_PORT_PARITY = "serial_port.parity";
    /**
     * Property: serial port timeout in millis (={@value #PROP_SERIAL_PORT}).
     */
    public static final String PROP_SERIAL_PORT_TIMEOUT_MS = "serial_port.timeout_ms";
    /**
     * Property: APN (={@value #PROP_APN}).
     */
    public static final String PROP_APN = "apn";
    /**
     * Property: APN user name (={@value #PROP_APN_USER}).
     */
    public static final String PROP_APN_USER = "apn.user";
    /**
     * Property: APN password (={@value #PROP_APN_PASSWORD}).
     */
    public static final String PROP_APN_PASSWORD = "apn.password";

    /**
     * Constructs the object by taking properties from the provided object.
     *
     * @param kind
     * @param props
     */
    public ModemDescriptor(final Kind kind, final Properties props) {
        try {
            kind_ = kind;
            mbnId_ = PropUtil.getMandatoryProperty(props, PROP_MBN_ID);
            final String niMacAddressRegex = props
                    .getProperty(PROP_NI_MAC_REGEX);
            final String niNameRegex = props.getProperty(PROP_NI_NAME_REGEX);
            if (niMacAddressRegex == null && niNameRegex == null) {
                throw new IllegalArgumentException(String.format(
                        "At least %s or %s property must be specified",
                        PROP_NI_MAC_REGEX, PROP_NI_NAME_REGEX));
            }
            niMacAddressRegex_ = niMacAddressRegex == null ? null : Pattern
                    .compile(niMacAddressRegex, Pattern.CASE_INSENSITIVE);
            niNameRegex_ = niNameRegex == null ? null : Pattern.compile(
                    niNameRegex, Pattern.CASE_INSENSITIVE);

            serialPortName_ = PropUtil.getMandatoryProperty(props,
                    PROP_SERIAL_PORT);
            final String serialPortDescrRegex = props
                    .getProperty(PROP_SERIAL_PORT_DESCR_REGEX);
            serialPortDescrRegex_ = serialPortDescrRegex == null ? null
                    : Pattern.compile(serialPortDescrRegex,
                    Pattern.CASE_INSENSITIVE);
            serialPortImeiCheck_ = props.getProperty(
                    PROP_SERIAL_PORT_IMEI_CHECK, NONE);
            serialPortBaudRate_ = PropUtil.getIntProperty(props,
                    PROP_SERIAL_PORT_BAUD_RATE, 115200);
            serialPortDataBits_ = PropUtil.getIntProperty(props,
                    PROP_SERIAL_PORT_DATA_BITS, 8);
            serialPortStopBits_ = PropUtil.getIntProperty(props,
                    PROP_SERIAL_PORT_STOP_BITS, SerialPort.ONE_STOP_BIT);
            serialPortParity_ = PropUtil.getIntProperty(props,
                    PROP_SERIAL_PORT_PARITY, SerialPort.NO_PARITY);
            serialPortTimeOutMs_ = PropUtil.getIntProperty(props,
                    PROP_SERIAL_PORT_TIMEOUT_MS, 200);

            final String apn = props.getProperty(PROP_APN);
            apn_ = apn == null || apn.isEmpty() ? null : apn;
            apnUser_ = props.getProperty(PROP_APN_USER);
            apnPassword_ = props.getProperty(PROP_APN_PASSWORD);
        } catch (Exception e) {
            throw new IllegalArgumentException("Bad modem descriptor for "
                    + kind, e);
        }
    }

    /**
     * @return Modem kind.
     */
    public Kind getKind() {
        return kind_;
    }

    /**
     * @return Mobile Broadband interface or device ID for the modem.
     */
    public String getMbnId() {
        return mbnId_;
    }

    /**
     * @return Modem control serial port name.
     */
    public String getSerialPortName() {
        return serialPortName_;
    }

    /**
     * @return Regex to filter out serial ports by their descriptions in auto
     * mode before querying for IMEI.
     */
    public Pattern getSerialPortDescrRegex() {
        return serialPortDescrRegex_;
    }

    /**
     * @return Serial port IMEI checking mode (mbn_id or direct IMEI).
     */
    public String getSerialPortImeiCheck() {
        return serialPortImeiCheck_;
    }

    /**
     * @return Baud rate of the modem control serial port.
     */
    public int getSerialPortBaudRate() {
        return serialPortBaudRate_;
    }

    /**
     * @return Parity check kind in a character frame of the modem control
     * serial port.
     * @see SerialPort#NO_PARITY
     * @see SerialPort#EVEN_PARITY
     * @see SerialPort#ODD_PARITY
     * @see SerialPort#MARK_PARITY
     */
    public int getSerialPortParity() {
        return serialPortParity_;
    }

    /**
     * @return Number of data bits in a character frame of the modem control
     * serial port.
     */
    public int getSerialPortDataBits() {
        return serialPortDataBits_;
    }

    /**
     * @return Number of stop bits in a character frame of the modem control
     * serial port.
     * @see SerialPort#ONE_STOP_BIT
     * @see SerialPort#ONE_POINT_FIVE_STOP_BITS
     * @see SerialPort#TWO_STOP_BITS
     */
    public int getSerialPortStopBits() {
        return serialPortStopBits_;
    }

    /**
     * @return Modem control serial port timeout in ms.
     */
    public int getSerialPortTimeOutMs() {
        return serialPortTimeOutMs_;
    }

    /**
     * @return Regex for matching the MAC address of a network interface.
     */
    public Pattern getNiMacAddressRegex() {
        return niMacAddressRegex_;
    }

    /**
     * @return Regex for matching the display name of a network interface.
     */
    public Pattern getNiNameRegex() {
        return niNameRegex_;
    }

    /**
     * @param ni Network interface.
     * @return <code>true</code> if the given network interface matches this
     * descriptor.
     */
    public boolean isMatchingNetworkInteface(final NetworkInterface ni) {
        try {

            if (LOG.isLoggable(Level.FINEST)) {
                LOG.finest(String.format(
                        "Trying network interface, MAC: %s, name: %s",
                        TextUtil.toHexString(ni.getHardwareAddress()),
                        ni.getDisplayName()));
            }


            if (niMacAddressRegex_ != null) {
                final byte[] niMac = ni.getHardwareAddress();
                if (niMac == null
                        || !niMacAddressRegex_.matcher(
                        TextUtil.toHexString(niMac)).matches()) {

                    LOG.finest("MAC check failed");


                    return false;
                }
            }

            if (niNameRegex_ != null
                    && !niNameRegex_.matcher(ni.getDisplayName()).matches()) {

                LOG.finest("name check failed");

                return false;
            }

            LOG.finest("match found");

            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "check failed due to an exception", e);

            return false;
        }
    }

    /**
     * @return APN.
     */
    public String getApn() {
        return apn_;
    }

    /**
     * @return APN user name.
     */
    public String getApnUser() {
        return apnUser_;
    }

    /**
     * @return APN password.
     */
    public String getApnPassword() {
        return apnPassword_;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("kind: %s, MBN ID: %s", kind_, mbnId_);
    }

    public NetworkInterface locateNetworkInterface() {
        try {
            final List<NetworkInterface> allNis = Collections
                    .list(NetworkInterface.getNetworkInterfaces());
            for (final NetworkInterface ni : allNis) {
                if (isMatchingNetworkInteface(ni)) {
                    return ni;
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,
                    "exception while looking for the matching network interface",
                    e);
        }
        return null;
    }

    public boolean isSerialPortDefined() {
        return !NONE.equalsIgnoreCase(serialPortName_);
    }

    private String findImeiInAtiResponse(final List<String> atiResponse) {
        for (final String line : atiResponse) {
            if (line.toLowerCase().startsWith(IMEI_PREFIX_IN_ATI)) {
                return line.substring(IMEI_PREFIX_IN_ATI.length());
            }
        }
        return null;
    }

    private boolean checkSerialPortImei(final SerialPort serialPort) {
        try {
            LOG.finest("checking IMEI match via serial port: "
                    + serialPort.getSystemPortName());

            openSerialPort(serialPort, serialPortTimeOutMs_);
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(serialPort.getInputStream()));
            final PrintWriter writer = new PrintWriter(
                    serialPort.getOutputStream());

            try {
                AtUtil.flushReader(reader);
                AtUtil.execAtCommand("AT^CURC=0", writer, reader, 2000);
            } catch (Exception e) {
                LOG.log(Level.WARNING,
                        "exception caught while disabling unsolicited responses",
                        e);
            }

            final List<String> atiResponse = AtUtil.filterResponseLines("ATI",
                    AtUtil.execAtCommand("ATI", writer, reader, 2000));
            final String imei = findImeiInAtiResponse(atiResponse);

            final String refImei = serialPortImeiCheck_
                    .equalsIgnoreCase(PROP_MBN_ID) ? mbnId_
                    : serialPortImeiCheck_;

            LOG.finest(String.format("expecting: %s, got: %s", refImei, imei));

            return imei != null && imei.equals(refImei);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "exception while checking the serial port: "
                    + serialPort.getSystemPortName(), e);
            return false;
        } finally {
            serialPort.closePort();
        }
    }

    public SerialPort locateSerialPort() {
        if (NONE.equalsIgnoreCase(serialPortName_)) {
            return null;
        }

        final SerialPort[] allSerialPorts = SerialPort.getCommPorts();
        if (BY_DESCR.equalsIgnoreCase(serialPortName_)) {
            for (final SerialPort serialPort : allSerialPorts) {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.finest(String.format(
                            "Trying serial port %s (%s), match by description",
                            serialPort.getSystemPortName(),
                            serialPort.getDescriptivePortName()));
                }
                if (serialPortDescrRegex_.matcher(
                        serialPort.getDescriptivePortName()).matches()
                        && checkSerialPortImei(serialPort)) {
                    LOG.finest("match found");

                    return serialPort;
                } else {
                    LOG.finest("no match or IMEI check failed");

                }
            }
        } else {
            for (final SerialPort serialPort : allSerialPorts) {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.finest(String
                            .format("Trying serial port %s (%s), match by system port name",
                                    serialPort.getSystemPortName(),
                                    serialPort.getDescriptivePortName()));
                }

                if (serialPort.getSystemPortName().equalsIgnoreCase(
                        serialPortName_)) {
                    //&& checkSerialPortImei(serialPort)) {
                    LOG.finest("match found");

                    return serialPort;
                } else {
                    LOG.finest("no match or IMEI check failed");

                }
            }
        }

        return null;
    }

    public SerialPort openSerialPort(final SerialPort serialPort,
                                     final long timeout) {
        serialPort.setComPortParameters(getSerialPortBaudRate(),
                getSerialPortDataBits(), getSerialPortStopBits(),
                getSerialPortParity());
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING
                        | SerialPort.TIMEOUT_WRITE_BLOCKING, (int) timeout,
                (int) timeout);

        if (!serialPort.openPort()) {
            throw new IllegalStateException("failed to open serial port: "
                    + serialPort.getSystemPortName());
        }


        return serialPort;
    }

}
