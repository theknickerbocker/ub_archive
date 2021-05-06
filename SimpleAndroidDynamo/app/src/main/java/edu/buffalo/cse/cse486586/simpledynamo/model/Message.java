package edu.buffalo.cse.cse486586.simpledynamo.model;


import static edu.buffalo.cse.cse486586.simpledynamo.SimpleDynamoProvider.DELIMITER;

/**
 * Used to standardize the network communications.
 */

public abstract class Message implements Comparable<Message> {

    MessageType type;
    String srcPort;
    String destPort;
    VectorClock vectorClock;
    String data;

    public Message(String srcPort, String destPort, VectorClock vectorClock, String data) {
        setType();
        this.srcPort = srcPort;
        this.destPort = destPort;
        this.vectorClock = vectorClock.clone();
        this.data = data;
        handleData(data);
    }

    abstract void setType();
    abstract void handleData(String data);

    public MessageType getMessageType() {
        return type;
    }

    public String getSourcePort() {
        return srcPort;
    }

    public String getDestinationPort() {
        return destPort;
    }

    public VectorClock getClock() {
        return vectorClock;
    }

    public String getData() {
        return data;
    }

    public String getMessageContent() {
        return (getMessageType().toString() + "\n" +
                getSourcePort()+ "\n" +
                getDestinationPort() + "\n" +
                getClock().toString() + "\n" +
                getData() + "\n");
    }

    public boolean isSource(String port) {
        return port.equals(srcPort);
    }

    String[] split(String str) {
        return str.split(DELIMITER);
    }

    @Override
    public int compareTo(Message other) {
        return 0;
    }

    @Override
    public String toString() {
        return ("TYPE: " + getMessageType().toString() +
                "  SRC: " + getSourcePort()+
                "  DST: " + getDestinationPort() +
                "  CLK: " + getClock().toString() +
                "  DATA: " + getData());
    }
}

