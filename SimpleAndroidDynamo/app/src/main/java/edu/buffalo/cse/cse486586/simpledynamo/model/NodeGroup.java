package edu.buffalo.cse.cse486586.simpledynamo.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static edu.buffalo.cse.cse486586.simpledynamo.SimpleDynamoProvider.DUPLICATES;

/**
 * Influenced by the Chord SigComm paper.
 */

public class NodeGroup {

    private ArrayList<Node> nodeList;
    private HashMap<String, Node> portToNodeMap;

    public NodeGroup(List<String> systemPorts) {
        nodeList = new ArrayList<Node>();
        portToNodeMap = new HashMap<String, Node>();

        for (String sysPort : systemPorts) {
            Node node = new Node(sysPort);
            nodeList.add(node);
            portToNodeMap.put(sysPort, node);
        }
        Collections.sort(nodeList);

        for(int i = 0; i < nodeList.size(); i++) {
            ArrayList<Node> preferenceList = new ArrayList<Node>();
            ArrayList<Node> previousList = new ArrayList<Node>();
            Node node = nodeList.get(i);
            for (int j = 0; j < DUPLICATES; j++) {
                int prefIndex = (i + j) % nodeList.size();
                int prevIndex = (prefIndex + 3) % nodeList.size();
                preferenceList.add(nodeList.get(prefIndex));
                previousList.add(nodeList.get(prevIndex));
            }
            node.setPreferenceList(preferenceList);
            node.setPreviousList(previousList);
        }

        for (Node node : nodeList) {
            String out = "Port: " + node.getPort() + " ID: " + node.getId() + "  |  Prev List: ";

            for (Node prefNode : node.getPreviousList()) {
                out += "( " + prefNode.getPort() + " , " + prefNode.getId() + " ), ";
            }
            Log.i("NODE GROUP", out);
        }
    }

    public boolean isHeldBy(String keyId, String port) {
        Node node = getNode(port);
        for (Node curNode : node.getPreviousList()) {
            if (isSuccessor(keyId, curNode.getId())) {
                Log.i("HELD", "FOR: " + node.getId() + "  HOLDER: " + curNode.getId() + "  ID: " + keyId);
                return true;
            }
        }
        Log.i("NOT HELD", keyId);
        return false;
    }

    public Node getNode(String port) {
        return portToNodeMap.get(port);
    }

    public Node getSuccessor(String id) {
        Node prev = nodeList.get(nodeList.size() - 1);
        Node cur = nodeList.get(0);
        for (int i = 1; i < nodeList.size(); i++) {
            if (isSuccessor(prev, cur, id)) {
                return cur;
            }
            prev = cur;
            cur = nodeList.get(i);
        }
        return cur;
    }

    public boolean isSuccessor(String keyId, String nodeId) {
        return getSuccessor(keyId).getId().equals(nodeId);
    }

    private static boolean isInBetween(Node node1, Node node2, String id) {
        boolean aboveNode1 = id.compareTo(node1.getId()) > 0;
        boolean belowNode2 = id.compareTo(node2.getId()) <= 0;
        return aboveNode1 && belowNode2;
    }

    private static boolean largestId(Node node1, Node node2, String id) {
        boolean node1AboveNode2 = node1.getId().compareTo(node2.getId()) > 0;
        boolean aboveNode1 = id.compareTo(node1.getId()) > 0;
        return node1AboveNode2 && aboveNode1;
    }

    private static boolean smallestId(Node node1, Node node2, String id) {
        boolean node1AboveNode2 = node1.getId().compareTo(node2.getId()) > 0;
        boolean belowNode2 = id.compareTo(node2.getId()) < 0;
        return node1AboveNode2 && belowNode2;
    }

    public static boolean isSuccessor(Node node1, Node node2, String id) {
        boolean idInBetween = isInBetween(node1, node2, id);
        boolean largestId = largestId(node1, node2, id);
        boolean smallestId = smallestId(node1, node2, id);
        return idInBetween || largestId || smallestId;
    }
}
