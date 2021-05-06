package edu.buffalo.cse.cse486586.simpledynamo.model;

import static edu.buffalo.cse.cse486586.simpledynamo.SimpleDynamoProvider.genHash;

/**
 * Created by kevinrathbun on 4/7/18.
 */

public class QueryRequestMessage extends QueryMessage {

    public QueryRequestMessage(String srcPort, String destPort, VectorClock vectorClock, String data) {
        super(srcPort, destPort, vectorClock, data);
        genHash(getQueryId());
    }

    @Override
    void setType() {
        this.type = MessageType.REQ_QUERY;
    }
}
