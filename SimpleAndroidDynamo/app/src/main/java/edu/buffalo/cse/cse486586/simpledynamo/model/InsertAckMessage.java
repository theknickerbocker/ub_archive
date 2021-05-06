package edu.buffalo.cse.cse486586.simpledynamo.model;

import static edu.buffalo.cse.cse486586.simpledynamo.SimpleDynamoProvider.genHash;

/**
 * Created by kevinrathbun on 5/11/18.
 */

public class InsertAckMessage extends Message {

    public InsertAckMessage(String srcPort, String destPort, VectorClock vectorClock, String data) {
        super(srcPort, destPort, vectorClock, data);
    }

    @Override
    void setType() {
        this.type = MessageType.INSERT_ACK;
    }

    @Override
    void handleData(String data) {

    }
}
