package edu.buffalo.cse.cse486586.simpledynamo.model;

import android.content.ContentValues;

import static edu.buffalo.cse.cse486586.simpledynamo.SimpleDynamoProvider.COLUMNS;
import static edu.buffalo.cse.cse486586.simpledynamo.SimpleDynamoProvider.genHash;

public class InsertMessage extends Message {

    String hash;
    String key;
    String value;
    VectorClock valueClock;

    public InsertMessage(String srcPort, String destPort, VectorClock vectorClock, String data) {
        super(srcPort, destPort, vectorClock, data);
    }

    @Override
    void setType() {
        this.type = MessageType.INSERT;
    }

    @Override
    void handleData(String data) {
        String[] parts = split(data);
        key = parts[0];
        value = parts[1];
        valueClock = VectorClock.valueOf(parts[2]);
        hash = genHash(key);
    }

    public ContentValues getValues() {
        ContentValues values = new ContentValues();
        values.put(COLUMNS[0], key);
        values.put(COLUMNS[1], value);
        values.put(COLUMNS[2], valueClock.toString());
        return values;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getHash() {
        return hash;
    }

    public VectorClock getValueClock() {
        return valueClock;
    }
}
