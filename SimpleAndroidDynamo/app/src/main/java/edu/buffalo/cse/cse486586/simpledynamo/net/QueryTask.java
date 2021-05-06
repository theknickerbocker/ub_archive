package edu.buffalo.cse.cse486586.simpledynamo.net;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import edu.buffalo.cse.cse486586.simpledynamo.model.Message;
import edu.buffalo.cse.cse486586.simpledynamo.model.QueryResponseMessage;

import static edu.buffalo.cse.cse486586.simpledynamo.net.SocketUtils.readSocket;
import static edu.buffalo.cse.cse486586.simpledynamo.net.SocketUtils.writeSocket;

/**
 * Created by kevinrathbun on 5/11/18.
 */

public class QueryTask extends AsyncTask<Message, Void, Cursor> {

    private static final String TAG = QueryTask.class.getSimpleName();

    @Override
    protected Cursor doInBackground(Message... msgs) {
        Message sending = msgs[0];
        Cursor cursor = sendMessage(sending);

        return cursor;
    }


    private Cursor sendMessage(Message msg) {
        try {
            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                    Integer.valueOf(msg.getDestinationPort()));
            socket.setSoTimeout(400);

            writeSocket(socket, msg);
//            Log.i(TAG, "SENT: " + msg.toString());


            QueryResponseMessage queryResMsg = (QueryResponseMessage) readSocket(socket);
            socket.close();
            if(queryResMsg == null) {
                return null;
            }
//            Log.i(TAG, "REC: " + queryResMsg.toString());
            socket.close();
            return queryResMsg.getCursor();
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
