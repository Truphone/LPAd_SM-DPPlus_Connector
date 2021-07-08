package com.truphone.lpad.apdu.channel.simulator;

import com.truphone.apdu.channel.simulator.LpadApduChannelSimulator;
import com.truphone.apdu.channel.simulator.persistence.ExpectationPersistence;
import com.truphone.lpa.ApduTransmittedListener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class LpadApduChannelSimulatorTest {

    private LpadApduChannelSimulator lpadApduChannelSimulator;

    @Mock
    private ExpectationPersistence mockExpectationPersistence;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        lpadApduChannelSimulator = new LpadApduChannelSimulator(mockExpectationPersistence);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionLpadApduChannelSimulator() {
        new LpadApduChannelSimulator(null);
    }

    @Test
    public void shouldSetAnExpectationToTransmitAPDU() {
        lpadApduChannelSimulator.setTransmitAPDUExpectation("input", "output");

        verify(mockExpectationPersistence, times(1)).storeExpectationMapping("input", "output");
    }

    @Test
    public void shouldSetAnExpectationToTransmitAPDUs() {
        lpadApduChannelSimulator.setTransmitAPDUsExpectation(Arrays.asList("xpto","asdasd"), "output");

        verify(mockExpectationPersistence, times(1)).storeExpectationMapping(Arrays.asList("xpto","asdasd"), "output");
    }

    @Test
    public void shouldClearAllExpectations() {
        lpadApduChannelSimulator.clearAllExpectations();

        verify(mockExpectationPersistence, times(1)).clear();
    }

    @Test
    public void shouldTransmitAPDU() {
        when(mockExpectationPersistence.getValueOfAnExpectation("example")).thenReturn("result");

        assertEquals("result", lpadApduChannelSimulator.transmitAPDU("example"));
    }

    @Test
    public void shouldTransmitAPDUs() {
        when(mockExpectationPersistence.getValueOfAnExpectation(Arrays.asList("APDU1", "APDU2"))).thenReturn("result");

        assertEquals("result", lpadApduChannelSimulator.transmitAPDUS(Arrays.asList("APDU1", "APDU2")));
    }

    @Test
    public void shouldSendStatusWhenApduTransmittedListenerIsDefined() {
        ApduTransmittedListener mockApduTransmittedListener = mock(ApduTransmittedListener.class);

        lpadApduChannelSimulator.setApduTransmittedListener(mockApduTransmittedListener);

        lpadApduChannelSimulator.sendStatus();

        verify(mockApduTransmittedListener, atLeastOnce()).onApduTransmitted();
    }

    @Test
    public void shouldDefineAnApduTransmittedListener() {
        ApduTransmittedListener mockApduTransmittedListener = mock(ApduTransmittedListener.class);

        lpadApduChannelSimulator.setApduTransmittedListener(mockApduTransmittedListener);
        lpadApduChannelSimulator.removeApduTransmittedListener(mockApduTransmittedListener);

        assertNull(lpadApduChannelSimulator.getApduTransmittedListener());
    }

    @Test
    public void shouldRemoveApduTransmittedListener() {
        ApduTransmittedListener mockApduTransmittedListener = mock(ApduTransmittedListener.class);
        lpadApduChannelSimulator.removeApduTransmittedListener(mockApduTransmittedListener);
        assertNull(lpadApduChannelSimulator.getApduTransmittedListener());
    }

    @Test
    public void shouldDoNothingWhenTryingToRemoveAnApduTransmittedListenerUsingANullOne() {
        lpadApduChannelSimulator.removeApduTransmittedListener(null);
        assertNull(lpadApduChannelSimulator.getApduTransmittedListener());
    }
}