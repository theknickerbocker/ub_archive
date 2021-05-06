package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import edu.buffalo.cse.cse486586.simpledynamo.model.DeleteMessage;
import edu.buffalo.cse.cse486586.simpledynamo.model.Entry;
import edu.buffalo.cse.cse486586.simpledynamo.model.GlobalFileFilter;
import edu.buffalo.cse.cse486586.simpledynamo.model.InsertAckMessage;
import edu.buffalo.cse.cse486586.simpledynamo.model.InsertMessage;
import edu.buffalo.cse.cse486586.simpledynamo.model.Message;
import edu.buffalo.cse.cse486586.simpledynamo.model.Node;
import edu.buffalo.cse.cse486586.simpledynamo.model.NodeGroup;
import edu.buffalo.cse.cse486586.simpledynamo.model.QueryRequestMessage;
import edu.buffalo.cse.cse486586.simpledynamo.model.QueryResponseMessage;
import edu.buffalo.cse.cse486586.simpledynamo.model.SpecificFileFilter;
import edu.buffalo.cse.cse486586.simpledynamo.model.VectorClock;
import edu.buffalo.cse.cse486586.simpledynamo.net.ClientTask;
import edu.buffalo.cse.cse486586.simpledynamo.net.InsertTask;
import edu.buffalo.cse.cse486586.simpledynamo.net.QueryTask;

import static edu.buffalo.cse.cse486586.simpledynamo.net.ClientTask.BCAST;
import static edu.buffalo.cse.cse486586.simpledynamo.net.SocketUtils.readSocket;
import static edu.buffalo.cse.cse486586.simpledynamo.SimpleDynamoActivity.SYS_PORTS;
import static edu.buffalo.cse.cse486586.simpledynamo.net.SocketUtils.writeSocket;

public class SimpleDynamoProvider extends ContentProvider {

	private static final String TAG = SimpleDynamoActivity.class.getSimpleName();
	private static final int SERVER_PORT = 10000;

	public final static String[] COLUMNS = new String[]{"key", "value","clock"};
	public static final String DELIMITER = ":::";
	public static final String GLOBAL_ALL = "*";
	public static final String LOCAL_ALL = "@";

	public static final int DUPLICATES = 3;
	public static final int READERS = 2;
	public static final int WRITERS = 2;

	private NodeGroup nodeGroup;
	private String myPort;
	private String myId;
	private VectorClock myClock;
	private Uri uri;


	@Override
	public boolean onCreate() {
		TelephonyManager tel = (TelephonyManager) this.getContext().getSystemService(Context.TELEPHONY_SERVICE);
		String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		int serialNumber = Integer.parseInt(portStr);
		myPort = Integer.toString(serialNumber * 2);
		myId = genHash(Integer.toString(serialNumber));

		uri = buildUri("content", "edu.buffalo.cse.cse486586.simpledynamo.provider");
		nodeGroup = new NodeGroup(SYS_PORTS);
        myClock = new VectorClock(SYS_PORTS);

		try {
			ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
			new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
		} catch (IOException e) {
			Log.e(TAG, "Failed to initialize server", e);
		}

		recover();

		return true;
	}

	private Uri buildUri(String scheme, String authority) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}

	private synchronized void recover() {
        ArrayList<Cursor> cursors = new ArrayList<Cursor>();
        for (String port : SYS_PORTS) {
            if (port.equals(myPort)) {
                continue;
            }
            Node node = nodeGroup.getNode(port);
            QueryRequestMessage queryReqMsg = new QueryRequestMessage(myPort, node.getPort(), myClock, GLOBAL_ALL);

            try {
                Cursor cursor = new QueryTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, queryReqMsg).get();
                if (cursor != null) {
                    cursors.add(cursor);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Insert interrupted", e);
            } catch (ExecutionException e) {
                Log.e(TAG, "Insert execution exception", e);
            }
        }
        insertRecoveryEntries(cursors);
    }
    @Override
    public int delete(Uri uri, final String selection, String[] selectionArgs) {
        FilenameFilter nameFilter;
        if (selection.equals(GLOBAL_ALL)) {
            DeleteMessage deleteMessage = new DeleteMessage(myPort, BCAST, myClock, LOCAL_ALL);
			new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, deleteMessage);
            return 0;
        } else if (selection.equals(LOCAL_ALL)) {
            nameFilter = new GlobalFileFilter();
        } else if (selectionArgs == null) {
            DeleteMessage deleteMessage = new DeleteMessage(myPort, BCAST, myClock, selection);
			new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, deleteMessage);
            return 0;
        } else {
            nameFilter = new SpecificFileFilter(selection);
        }

        try {
            File[] files = getContext().getFilesDir().listFiles(nameFilter);
            for (File file : files) {
                file.delete();
            }
            return files.length;
        } catch (NullPointerException e) {
            return 0;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String key = (String) values.get(COLUMNS[0]);
        String content = (String) values.get(COLUMNS[1]);
        String data = key + DELIMITER + content + DELIMITER + myClock.toString();
        Node successor = nodeGroup.getSuccessor(genHash(key));

        for (Node node : successor.getPreferenceList()) {
        	InsertMessage insertMsg = new InsertMessage(myPort, node.getPort(), myClock, data);

        	try {
                new InsertTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, insertMsg).get();
            } catch (InterruptedException e) {
        	    Log.e(TAG, "Insert interrupted", e);
            } catch (ExecutionException e) {
                Log.e(TAG, "Insert execution exception", e);
            }
		}

		return uri;
    }

    public synchronized Uri writeValues(Uri uri, ContentValues values) {
		String key = (String) values.get(COLUMNS[0]);
		String content = (String) values.get(COLUMNS[1]);
		content = content + DELIMITER + VectorClock.valueOf((String)values.get(COLUMNS[2]));
		FileOutputStream outputStream;

		try {
			outputStream = this.getContext().openFileOutput(key, Context.MODE_PRIVATE);
			outputStream.write(content.getBytes());
			outputStream.close();
		} catch (Exception e) {
			Log.e("insert", "File write failed", e);
		}

		return uri;
	}

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        FilenameFilter nameFilter;
        Node successor = nodeGroup.getSuccessor(genHash(selection));

        if (selection.equals(LOCAL_ALL)) {
            nameFilter = new GlobalFileFilter();
            return findValues(nameFilter, new String[]{COLUMNS[0], COLUMNS[1]});
        } else if (selection.equals(GLOBAL_ALL)) {
            ArrayList<Cursor> cursors = new ArrayList<Cursor>();
            for (String port : SYS_PORTS) {
                if (port.equals(myPort)) {
                    continue;
                }
                Node node = nodeGroup.getNode(port);
                QueryRequestMessage queryReqMsg = new QueryRequestMessage(myPort, node.getPort(), myClock, selection);

                try {
                    Cursor cursor = new QueryTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, queryReqMsg).get();
                    if (cursor != null) {
                        cursors.add(cursor);
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "Insert interrupted", e);
                } catch (ExecutionException e) {
                    Log.e(TAG, "Insert execution exception", e);
                }
            }
            Cursor cursor = resolveCursor(cursors);
            return cursor;
        } else {
            ArrayList<Cursor> cursors = new ArrayList<Cursor>();
            for (Node node : successor.getPreferenceList()) {
                QueryRequestMessage queryReqMsg = new QueryRequestMessage(myPort, node.getPort(), myClock, selection);

                try {
                    Cursor cursor = new QueryTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, queryReqMsg).get();
                    if (cursor != null) {
                        cursors.add(cursor);
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "Insert interrupted", e);
                } catch (ExecutionException e) {
                    Log.e(TAG, "Insert execution exception", e);
                }
            }
            Cursor cursor = resolveCursor(cursors);
            return cursor;
        }
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

    private synchronized Cursor findValues(FilenameFilter nameFilter, String[] columns) {
        try {
            MatrixCursor matrixCursor = new MatrixCursor(columns);
            File[] files = getContext().getFilesDir().listFiles(nameFilter);
            for (File file : files) {

                String key = file.getName();
                long fileLength = file.length();
                if (fileLength > Integer.MAX_VALUE) {
                    throw new IllegalArgumentException();
                }
                int bufferLength = (int) fileLength;
                char[] valueBuffer = new char[bufferLength];
                new BufferedReader(new FileReader(file)).read(valueBuffer);
                String[] content = new String(valueBuffer).split(DELIMITER);
                String value = content[0];
                String clock = content[1];

                ArrayList<String> rowValues = new ArrayList<String>();
                rowValues.add(key);
                rowValues.add(value);
                if (columns.length == 3) {
                    rowValues.add(clock);
                }

                String[] tuple = new String[rowValues.size()];
                for (int i = 0; i < rowValues.size(); i++) {
                    tuple[i] = rowValues.get(i);
                }
                matrixCursor.addRow(tuple);
            }
            return matrixCursor;
        } catch (NullPointerException e) {
            Log.e(TAG, "File null", e);
            return new MatrixCursor(COLUMNS);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found", e);
            return new MatrixCursor(COLUMNS);
        } catch (IOException e) {
            Log.e(TAG, "File IO error", e);
            return new MatrixCursor(COLUMNS);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "File too big", e);
            return new MatrixCursor(COLUMNS);
        }
    }

    private MatrixCursor resolveCursor(List<Cursor> cursors) {
        HashMap<String, Entry> entryMap = new HashMap<String, Entry>();
        for (Cursor cursor : cursors) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String key = cursor.getString(cursor.getColumnIndex(COLUMNS[0]));
                String value = cursor.getString(cursor.getColumnIndex(COLUMNS[1]));
                VectorClock clock = VectorClock.valueOf(cursor.getString(cursor.getColumnIndex(COLUMNS[2])));
                Entry entry = new Entry(key,value,clock);

                if (!entryMap.keySet().contains(key)) {
                    entryMap.put(key, entry);
                    continue;
                }
                Entry prevEntry = entryMap.get(key);
                if (entry.getClock().greaterThan(prevEntry.getClock())) {
                    entryMap.put(key,entry);
                }
                cursor.moveToNext();
            }
        }
        MatrixCursor cursor = new MatrixCursor(new String[]{COLUMNS[0], COLUMNS[1]});
        for (String key : entryMap.keySet()) {
            Entry entry = entryMap.get(key);
            String[] tuple = new String[]{entry.getKey(), entry.getValue()};
            cursor.addRow(tuple);
        }

        return cursor;
    }

    private synchronized void insertRecoveryEntries(List<Cursor> cursors) {
        HashMap<String, Entry> entryMap = new HashMap<String, Entry>();
        for (Cursor cursor : cursors) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String key = cursor.getString(cursor.getColumnIndex(COLUMNS[0]));
                String value = cursor.getString(cursor.getColumnIndex(COLUMNS[1]));
                VectorClock clock = VectorClock.valueOf(cursor.getString(cursor.getColumnIndex(COLUMNS[2])));
                Entry entry = new Entry(key,value,clock);

                if (!entryMap.keySet().contains(key)) {
                    entryMap.put(key, entry);
                    cursor.moveToNext();
                    continue;
                }
                Entry prevEntry = entryMap.get(key);
//                if (entry.getClock().greaterThan(prevEntry.getClock())) {
                    entryMap.put(key,entry);
//                }
                cursor.moveToNext();
            }
        };
        for (String key : entryMap.keySet()) {
            Entry entry = entryMap.get(key);
            String content = entry.getValue() + DELIMITER + entry.getClock().toString();
            FileOutputStream outputStream;

            if (nodeGroup.isHeldBy(genHash(key), myPort)) {
                try {
                    outputStream = this.getContext().openFileOutput(key, Context.MODE_PRIVATE);
                    outputStream.write(content.getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    Log.e("insert", "File write failed", e);
                }
            }
        }

    }

	public static String genHash(String input) {
		MessageDigest sha1;
		try {
			sha1 = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "Hashing algorithm not found", e);
			return null;
		}
		byte[] sha1Hash = sha1.digest(input.getBytes());
		Formatter formatter = new Formatter();
		for (byte b : sha1Hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}

	/**
	 * Basis also taken from PA2B implementation, which again is a modified version of the
	 * ClientTask from PA1 to use my Message class and to be more generic.
	 */

	private class ServerTask extends AsyncTask<ServerSocket, Message, Void> {

		@Override
		protected Void doInBackground(ServerSocket... sockets) {
			while (true) {
				ServerSocket serverSocket = sockets[0];

				Socket socket;
				try {
					socket = serverSocket.accept();

					Message msg = readSocket(socket);
//                    Log.i("REC", msg.toString());

					myClock.increment(myPort);
					myClock.update(msg.getClock());

					if (msg instanceof InsertMessage) {
						InsertMessage insertMsg = (InsertMessage) msg;

						handleInsert(insertMsg, socket);
					} else if (msg instanceof QueryRequestMessage) {
					    QueryRequestMessage queryReqMsg = (QueryRequestMessage) msg;

					    handleQueryRequest(queryReqMsg, socket);
					} else if (msg instanceof QueryResponseMessage) {

					} else {
						publishProgress(msg);
						socket.close();
					}
				} catch (IOException e) {
					Log.e(TAG, "IOException in doInBackground", e);
					return null;
				} catch (NullPointerException e) {
					Log.e(TAG, "Null pointer in doInBackground", e);
					return null;
				}
			}
		}

		@Override
		protected void onProgressUpdate(Message... messages) {
			Message msg = messages[0];
			if (msg instanceof DeleteMessage) {
				DeleteMessage deleteMessage = (DeleteMessage) msg;
                delete(uri,deleteMessage.getSelection(),new String[]{deleteMessage.getSourcePort()});
			} else if (msg instanceof InsertMessage) {
				InsertMessage insertMessage = (InsertMessage) msg;


			} else if (msg instanceof QueryRequestMessage) {
			    QueryRequestMessage queryReqMessage = (QueryRequestMessage) msg;

			} else {
				Log.e(TAG, "Received matched no MessageType");
			}
		}

		private synchronized void handleQueryRequest(QueryRequestMessage queryReqMessage, Socket socket) {
            String selection = queryReqMessage.getQueryId();
            FilenameFilter filter;

            if (selection.equals(GLOBAL_ALL)) {
                filter = new GlobalFileFilter();
            } else {
                filter = new SpecificFileFilter(selection);
            }

            Cursor cursor = findValues(filter, COLUMNS);

            QueryResponseMessage queryResMsg = new QueryResponseMessage(myPort, queryReqMessage.getSourcePort(), myClock, selection);
            queryResMsg.insertValues(cursor);

//            Log.i("SENT", queryResMsg.toString());
            writeSocket(socket, queryResMsg);
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing socket",e);
            }
		}

		private synchronized void handleInsert(InsertMessage insertMessage, Socket socket) {
            ContentValues values = insertMessage.getValues();

            writeValues(uri, values);

            InsertAckMessage ack =
                    new InsertAckMessage(myPort, insertMessage.getSourcePort(), myClock, "ACK");
            writeSocket(socket, ack);
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing socket",e);
            }
		}
	}
}
