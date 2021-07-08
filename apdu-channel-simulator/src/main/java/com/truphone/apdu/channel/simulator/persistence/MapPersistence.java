package com.truphone.apdu.channel.simulator.persistence;

import java.util.Map;

public class MapPersistence implements ExpectationPersistence {

    private final Map bag;

    public MapPersistence(final Map storageUnit) {
        if(storageUnit == null) {
            throw new IllegalArgumentException("Storage unit must be defined");
        }
        this.bag = storageUnit;
    }

    public <K,V> void storeExpectationMapping(final K inputKey, final V inputValue) {
        validateInputKey(inputKey);

        this.bag.put(inputKey, inputValue);
    }

    public <K,V> V getValueOfAnExpectation(final K inputKey) {
        validateInputKey(inputKey);
        Object result = this.bag.get(inputKey);
        return result != null ? (V) result : null;
    }

    public void clear() {
        this.bag.clear();
    }

    private void validateInputKey(final Object inputKey) {
        if(inputKey == null) {
            throw new IllegalArgumentException("Input key must not be null");
        }
    }
}
