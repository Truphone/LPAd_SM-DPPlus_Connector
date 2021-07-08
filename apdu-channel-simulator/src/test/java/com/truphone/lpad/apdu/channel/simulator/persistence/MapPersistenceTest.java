package com.truphone.lpad.apdu.channel.simulator.persistence;


import com.truphone.apdu.channel.simulator.persistence.MapPersistence;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MapPersistenceTest {

    private MapPersistence mapPersistence;

    @Before
    public void setUp() {
        mapPersistence = new MapPersistence(new HashMap());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenTryingToConstructWithANullMap() {
        new MapPersistence(null);
    }

    @Test
    public void shouldStoreExpectationMappingValidKeyAndValue() {
        mapPersistence.storeExpectationMapping("key", "value");

        assertEquals("value", mapPersistence.getValueOfAnExpectation("key"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenTryingToStoreExpectationMappingWithNullKey() {
        mapPersistence.storeExpectationMapping(null, "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenTryingToGetExpectationMappingWithNullKey() {
        mapPersistence.getValueOfAnExpectation(null);
    }

    @Test
    public void shouldClearAllExpectactions() {
        mapPersistence.storeExpectationMapping("a", "b");
        mapPersistence.storeExpectationMapping("b", "c");

        assertEquals("b", mapPersistence.getValueOfAnExpectation("a"));
        assertEquals("c", mapPersistence.getValueOfAnExpectation("b"));

        mapPersistence.clear();

        assertNull(mapPersistence.getValueOfAnExpectation("a"));
        assertNull(mapPersistence.getValueOfAnExpectation("b"));
    }
}