package edu.buffalo.cse.cse486586.simpledynamo.net;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import edu.buffalo.cse.cse486586.simpledynamo.model.InsertAckMessage;
import edu.buffalo.cse.cse486586.simpledynamo.model.InsertMessage;
import edu.buffalo.cse.cse486586.simpledynamo.model.Message;
import edu.buffalo.cse.cse486586.simpledynamo.model.Node;

import static edu.buffalo.cse.cse486586.simpledynamo.SimpleDynamoActivity.SYS_PORTS;
import static edu.buffalo.cse.cse486586.simpledynamo.net.SocketUtils.readSocket;
import static edu.buffalo.cse.cse486586.simpledynamo.net.SocketUtils.writeSocket;

/**
 * Created by kevinrathbun on 5/11/18.
 */

public class InsertTask extends AsyncTask<Message, Void, Message> {

    private static final String TAG = InsertTask.class.getSimpleName();

    @Override
    protected Message doInBackground(Message... msgs) {
        Message sending = msgs[0];
        Message receiving = sendMessage(sending);

        return receiving;
    }


    private Message sendMessage(Message msg) {
        try {
            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                    Integer.valueOf(msg.getDestinationPort()));
            socket.setSoTimeout(200);

            writeSocket(socket, msg);
//            Log.i(TAG, "SENT: " + msg.toString());

            Message receiving = readSocket(socket);
            socket.close();
            return receiving;
        } catch (UnknownHostException e) {
            Log.e(TAG, "ClientPriorityTask UnknownHostException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "ClientPriorityTask socket IOException during write");
            e.printStackTrace();
        }
        return null;
    }
}
