package edu.buffalo.cse.cse486586.simpledynamo.model;

/**
 * Used to serialize the class type across communication.
 */

public enum MessageType {

    REQ_QUERY,
    RES_QUERY,
    DELETE,
    INSERT,
    INSERT_ACK;

    @Override
    public String toString() {
        return super.toString();
    }

}
