package edu.buffalo.cse.cse486586.simpledynamo.model;

/**
 * Created by kevinrathbun on 4/12/18.
 */

public class QueryResponseMessage extends QueryMessage {

    public QueryResponseMessage(String srcPort, String destPort, VectorClock vectorClock, String data) {
        super(srcPort, destPort, vectorClock, data);
    }

    @Override
    void setType() {
        this.type = MessageType.RES_QUERY;
    }
}
