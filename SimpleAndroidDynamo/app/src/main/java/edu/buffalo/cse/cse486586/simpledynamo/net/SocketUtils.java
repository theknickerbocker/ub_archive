package edu.buffalo.cse.cse486586.simpledynamo.net;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import edu.buffalo.cse.cse486586.simpledynamo.model.DeleteMessage;
import edu.buffalo.cse.cse486586.simpledynamo.model.InsertAckMessage;
import edu.buffalo.cse.cse486586.simpledynamo.model.InsertMessage;
import edu.buffalo.cse.cse486586.simpledynamo.model.Message;
import edu.buffalo.cse.cse486586.simpledynamo.model.MessageType;
import edu.buffalo.cse.cse486586.simpledynamo.model.QueryRequestMessage;
import edu.buffalo.cse.cse486586.simpledynamo.model.QueryResponseMessage;
import edu.buffalo.cse.cse486586.simpledynamo.model.VectorClock;

/**
 * Taken from PA2B with some slight modifications. Socket reading and writing methods
 * influenced by Android Developer and Oracle java docs for Socket class and Writer classes.
 */
public class SocketUtils {

    public static Message readSocket(Socket socket) {
        try {
            BufferedReader socketInput = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            String typeStr = socketInput.readLine();
            if (typeStr == null) {
                return null;
            }
            MessageType type = MessageType.valueOf(typeStr);
            String srcPort = socketInput.readLine();
            String destPort = socketInput.readLine();
            VectorClock vectorClock = VectorClock.valueOf(socketInput.readLine());
            String data = socketInput.readLine();

            switch(type) {
                case REQ_QUERY:
                    return new QueryRequestMessage(srcPort,destPort,vectorClock,data);

                case RES_QUERY:
                    return new QueryResponseMessage(srcPort,destPort,vectorClock,data);

                case DELETE:
                    return new DeleteMessage(srcPort,destPort,vectorClock,data);

                case INSERT:
                    return new InsertMessage(srcPort,destPort,vectorClock,data);

                case INSERT_ACK:
                    return new InsertAckMessage(srcPort,destPort,vectorClock,data);

                default:
            }

        } catch (IOException e) {
            Log.e("READ SOCKET", "IOException in readSocket");
        }

        return null;
    }

    public static void writeSocket(Socket socket, Message msg) {
        try {
            BufferedWriter socketOutput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            socketOutput.write(msg.getMessageContent());
            socketOutput.flush();
        } catch (IOException e) {
            Log.e("WRITE SOCKET", "IOException in writeSocket", e);
        }
    }
}
