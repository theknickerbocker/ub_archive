package edu.buffalo.cse.cse486586.simpledynamo.net;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.buffalo.cse.cse486586.simpledynamo.model.Message;

import static edu.buffalo.cse.cse486586.simpledynamo.net.SocketUtils.writeSocket;
import static edu.buffalo.cse.cse486586.simpledynamo.SimpleDynamoActivity.SYS_PORTS;

/**
 * Basis taken from PA2B implementation, which is a modified version of the ClientTask from PA1
 * to use my Message class and to be more generic.
 */

public class ClientTask extends AsyncTask<Message, Void, Void> {

    private static final String TAG = ClientTask.class.getSimpleName();

    public static final String BCAST = "BCAST";

    @Override
    protected Void doInBackground(Message... msgs) {
        for (Message msg : msgs) {
            if (msg.getDestinationPort().equals(BCAST)){
                broadcastMessage(msg);
            } else {
                sendMessage(msg);
            }
        }
        return null;
    }

    private void broadcastMessage(Message msg) {
        try {
            for (String port : SYS_PORTS) {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(port));

                writeSocket(socket, msg);
                socket.close();
            }
        } catch (UnknownHostException e) {
            Log.e(TAG, "ClientBroadcastTask UnknownHostException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "ClientBroadcastTask socket IOException during write");
            e.printStackTrace();
        }
    }

    private void sendMessage(Message msg) {
        try {
            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                    Integer.valueOf(msg.getDestinationPort()));
            writeSocket(socket, msg);
            socket.close();
        } catch (UnknownHostException e) {
            Log.e(TAG, "ClientPriorityTask UnknownHostException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "ClientPriorityTask socket IOException during write");
            e.printStackTrace();
        }
    }
}