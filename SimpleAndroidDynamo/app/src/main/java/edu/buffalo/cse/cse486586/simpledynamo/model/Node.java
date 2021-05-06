package edu.buffalo.cse.cse486586.simpledynamo.model;


import java.util.ArrayList;

import static edu.buffalo.cse.cse486586.simpledynamo.SimpleDynamoProvider.DELIMITER;
import static edu.buffalo.cse.cse486586.simpledynamo.SimpleDynamoProvider.genHash;

/**
 * Created by kevinrathbun on 4/30/18.
 */

public class Node implements Comparable<Node>{

    private String nodeId;
    private String port;
    private ArrayList<Node> preferenceList;
    private ArrayList<Node> previousList;

    public Node(String port) {
        this.port = port;
        this.nodeId = genHash(Integer.toString(Integer.valueOf(port)/2));
        preferenceList = new ArrayList<Node>();
        previousList = new ArrayList<Node>();
    }

    public String getPort() {
        return port;
    }

    public String getId() {
        return nodeId;
    }

    public ArrayList<Node> getPreferenceList() {
        return preferenceList;
    }

    void setPreferenceList(ArrayList<Node> preferenceList) {
        this.preferenceList = preferenceList;
    }

    public void setPreviousList(ArrayList previousList) {
        this.previousList = previousList;
    }

    public ArrayList<Node> getPreviousList() {
        return previousList;
    }

    @Override
    public String toString() {
        return nodeId + DELIMITER + port;
    }

    @Override
    public int compareTo(Node node) {
        return getId().compareTo(node.getId());
    }
}
