package com.truphone.apdu.channel.simulator.persistence;

public interface ExpectationPersistence {

    <K, V> void storeExpectationMapping(K inputKey, V inputValue);

    <K, V> V getValueOfAnExpectation(K inputKey);

    void clear();
}
