package edu.buffalo.cse.cse486586.simpledynamo.model;

/**
 * Created by kevinrathbun on 5/8/18.
 */

public class Entry {

    private String key;
    private String value;
    private VectorClock clock;

    public Entry(String key, String value, VectorClock clock) {
        this.key = key;
        this.value = value;
        this.clock = clock.clone();
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public VectorClock getClock() {
        return clock;
    }
}
