package e2e;


import com.truphone.apdu.channel.simulator.LpadApduChannelSimulator;
import com.truphone.apdu.channel.simulator.persistence.MapPersistence;
import com.truphone.lpa.ApduTransmittedListener;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TransmitApduTestIT {

    private LpadApduChannelSimulator lpadApduChannelSimulator;

    @Before
    public void setUp() {
        lpadApduChannelSimulator = new LpadApduChannelSimulator(new MapPersistence(new HashMap()));
    }

    @Test
    public void testSendStatus() {
        ApduTransmittedListener mockApduTransmittedListener = mock(ApduTransmittedListener.class);

        lpadApduChannelSimulator.setApduTransmittedListener(mockApduTransmittedListener);
        lpadApduChannelSimulator.sendStatus();

        verify(mockApduTransmittedListener, times(1)).onApduTransmitted();
    }

    @Test
    public void testTransmittApdu() {
        lpadApduChannelSimulator.setTransmitAPDUExpectation("APDU_SUCCESS", "asdsdasd23123easd");

        LpadApduTransmittedListener lpadApduTransmittedListener = new LpadApduTransmittedListener();

        lpadApduChannelSimulator.setApduTransmittedListener(lpadApduTransmittedListener);
        lpadApduChannelSimulator.sendStatus();

        assertTrue(lpadApduTransmittedListener.isApduTransmitted());
        assertEquals("asdsdasd23123easd", lpadApduChannelSimulator.transmitAPDU("APDU_SUCCESS"));
    }

    @Test
    public void testTransmittApdus() {
        lpadApduChannelSimulator.setTransmitAPDUsExpectation(Arrays.asList("APDU_SUCCESS", "XPTO"), "asdsdasd23123easd");

        LpadApduTransmittedListener lpadApduTransmittedListener = new LpadApduTransmittedListener();

        lpadApduChannelSimulator.setApduTransmittedListener(lpadApduTransmittedListener);
        lpadApduChannelSimulator.sendStatus();

        assertTrue(lpadApduTransmittedListener.isApduTransmitted());
        assertEquals("asdsdasd23123easd", lpadApduChannelSimulator.transmitAPDUS(Arrays.asList("APDU_SUCCESS", "XPTO")));
    }

    @Test
    public void shouldReturnNullWhenTransmittApdusExpectationWasNotSet() {
        lpadApduChannelSimulator.setApduTransmittedListener(new LpadApduTransmittedListener());
        lpadApduChannelSimulator.sendStatus();

        assertNull(lpadApduChannelSimulator.transmitAPDUS(Arrays.asList("asdasd", "zxczxc")));
    }

    @Test
    public void shouldReturnNullWhenTransmittApduExpectationWasNotSet() {
        lpadApduChannelSimulator.setApduTransmittedListener(new LpadApduTransmittedListener());
        lpadApduChannelSimulator.sendStatus();

        assertNull(lpadApduChannelSimulator.transmitAPDU("asdasd"));
    }
}
