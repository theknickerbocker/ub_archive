package edu.buffalo.cse.cse486586.simpledynamo.model;

import android.database.Cursor;
import android.database.MatrixCursor;

import static edu.buffalo.cse.cse486586.simpledynamo.SimpleDynamoProvider.COLUMNS;
import static edu.buffalo.cse.cse486586.simpledynamo.SimpleDynamoProvider.DELIMITER;
import static edu.buffalo.cse.cse486586.simpledynamo.SimpleDynamoProvider.GLOBAL_ALL;
import static edu.buffalo.cse.cse486586.simpledynamo.SimpleDynamoProvider.genHash;

/**
 * Created by kevinrathbun on 4/12/18.
 */

public abstract class QueryMessage extends Message {

    private String queryId;
    private String hash;
    MatrixCursor cursor;

    public QueryMessage(String srcPort, String destPort, VectorClock vectorClock, String data) {
        super(srcPort, destPort, vectorClock, data);
    }

    @Override
    void handleData(String data) {
        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        String[] info = split(data);
        queryId = info[0];
        for (int i = 1; i < info.length; i = i + 3) {
            cursor.addRow(new String[]{info[i], info[i+1], info[i+2]});
        }
        this.cursor = cursor;
        hash = genHash(queryId);
    }

    public String getQueryId() {
        return queryId;
    }

    public MatrixCursor getCursor() {
        return cursor;
    }

    public boolean isQueryAll() {
        return queryId.equals(GLOBAL_ALL);
    }

    public String getHash() {
        return hash;
    }

    @Override
    public String getData() {
        cursor.moveToFirst();
        StringBuffer buffer = new StringBuffer(queryId);
        while (!cursor.isAfterLast()) {
            String key = cursor.getString(cursor.getColumnIndex(COLUMNS[0]));
            String value = cursor.getString(cursor.getColumnIndex(COLUMNS[1]));
            String clock = cursor.getString(cursor.getColumnIndex(COLUMNS[2]));

            buffer.append(DELIMITER + key + DELIMITER + value + DELIMITER + clock);
            cursor.moveToNext();
        }
        data = buffer.toString();
        return data;
    }

    public void insertValues(Cursor valueCursor) {
        valueCursor.moveToFirst();
        while (!valueCursor.isAfterLast()) {
            String key = valueCursor.getString(valueCursor.getColumnIndex(COLUMNS[0]));
            String value = valueCursor.getString(valueCursor.getColumnIndex(COLUMNS[1]));
            String clock = valueCursor.getString(valueCursor.getColumnIndex(COLUMNS[2]));

            cursor.addRow(new String[]{key,value,clock});
            valueCursor.moveToNext();
        }
    }
}
