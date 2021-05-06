package edu.buffalo.cse.cse486586.simpledynamo.model;

import static edu.buffalo.cse.cse486586.simpledynamo.SimpleDynamoProvider.GLOBAL_ALL;
import static edu.buffalo.cse.cse486586.simpledynamo.SimpleDynamoProvider.genHash;

/**
 * Created by kevinrathbun on 4/7/18.
 */

public class DeleteMessage extends Message {

    private String selection;
    private String hash;

    public DeleteMessage(String srcPort, String destPort, VectorClock vectorClock, String data) {
        super(srcPort, destPort, vectorClock, data);
    }

    @Override
    void setType() {
        this.type = MessageType.DELETE;
    }

    @Override
    void handleData(String data) {
        selection = data;
        hash = genHash(selection);
    }

    public String getSelection() {
        return selection;
    }

    public String getHash() {
        return hash;
    }

    public boolean isDeleteAll() {
        return selection.equals(GLOBAL_ALL);
    }
}
