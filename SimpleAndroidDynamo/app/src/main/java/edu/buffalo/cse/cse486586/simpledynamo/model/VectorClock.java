package edu.buffalo.cse.cse486586.simpledynamo.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kevinrathbun on 5/1/18.
 */

public class VectorClock {

    private static final String ENTRY_DELIM = ",";
    private static final String VALUE_DELIM = ":";
    private HashMap<String,Integer> map;

    public VectorClock(List<String> sysPorts) {
        map = new HashMap<String, Integer>();
        for (String port : sysPorts) {
            map.put(port, 0);
        }
    }

    private VectorClock(HashMap<String,Integer> map) {
        this.map = map;
    }

    public int increment(String port) {
        int value = map.get(port) + 1;
        map.put(port, value);
        return value;
    }

    public int get(String port) {
        return map.get(port);
    }

    public int size() {
        return map.size();
    }

    public void update(VectorClock clock) {
        for (String key : map.keySet()) {
            int max = Math.max(get(key), clock.get(key));
            map.put(key, max);
        }
    }

    public boolean greaterThan(VectorClock clock) {
        if (this.size() != clock.size()) {
            return false;
        }
        for (String key : map.keySet()) {
            int value = this.get(key);
            int clockValue = clock.get(key);
            if (value < clockValue) {
                return false;
            }
        }
        if (this.equals(clock)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VectorClock)) {
            return false;
        }

        VectorClock clock = (VectorClock) obj;
        if (this.size() != clock.size()) {
            return false;
        }
        for (String key : map.keySet()) {
            if (this.get(key) != clock.get(key)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public VectorClock clone() {
        return valueOf(this.toString());
    }

    public static VectorClock valueOf(String str) {
        String[] entryArray = str.split(ENTRY_DELIM);
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        for (String entry : entryArray) {
            String[] pair = entry.split(VALUE_DELIM);
            map.put(pair[0], Integer.valueOf(pair[1]));
        }
        return new VectorClock(map);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();

        ArrayList<String> keys = new ArrayList<String>();
        for (String key : map.keySet()) {
            keys.add(key);
        }
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = Integer.toString(map.get(key));
            out.append(key + VALUE_DELIM + value + ENTRY_DELIM);
        }

        return out.toString();
    }
}
