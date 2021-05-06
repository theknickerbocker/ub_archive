package edu.buffalo.cse.cse486586.simpledynamo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import static edu.buffalo.cse.cse486586.simpledynamo.SimpleDynamoProvider.COLUMNS;
import static edu.buffalo.cse.cse486586.simpledynamo.SimpleDynamoProvider.genHash;

public class SimpleDynamoActivity extends Activity {

	public static final List<String> SYS_PORTS = Arrays.asList(
			"11108",
			"11112",
			"11116",
			"11120",
			"11124"
	);
	public static final Uri uri = buildUri("content", "edu.buffalo.cse.cse486586.simpledynamo.provider");

	private TextView logTextView;
	private EditText inputKeyEditText;
	private EditText inputValueEditText;
	private ContentResolver contentResolver;

	private static Uri buildUri(String scheme, String authority) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_simple_dynamo);

		logTextView = (TextView) findViewById(R.id.log_text_view);
		inputKeyEditText = (EditText) findViewById(R.id.input_key_edit_text);
		inputValueEditText = (EditText) findViewById(R.id.input_value_edit_text);
		contentResolver = getContentResolver();
		findViewById(R.id.button_test).setOnClickListener(
				new OnTestClickListener(logTextView, getContentResolver()));

		findViewById(R.id.button_query).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String keyText = inputKeyEditText.getText().toString();
				if (keyText.equals("")) {
					return;
				}

				Cursor queryResults = contentResolver.query(uri,null,keyText,null, null);
				queryResults.moveToFirst();
				while(!queryResults.isAfterLast()) {
					String key = queryResults.getString(queryResults.getColumnIndex(COLUMNS[0]));
					String value = queryResults.getString(queryResults.getColumnIndex(COLUMNS[1]));

					String pair = key + " : " + value;
					logTextView.append("Query Result: " + pair + "\n");
					queryResults.moveToNext();
				}
				inputKeyEditText.setText("");
				inputValueEditText.setText("");
			}
		});

		findViewById(R.id.button_insert).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String keyText = inputKeyEditText.getText().toString();
				String valueText = inputValueEditText.getText().toString();
				if (valueText.equals("") || keyText.equals("")) {
					return;
				}

				ContentValues values = new ContentValues();
				values.put(COLUMNS[0], keyText);
				values.put(COLUMNS[1], valueText);
				contentResolver.insert(uri, values);
				logTextView.append("Insert: " + keyText + " : " + valueText + "   hash = " + genHash(keyText) + "\n");
				inputKeyEditText.setText("");
				inputValueEditText.setText("");
			}
		});

		findViewById(R.id.button_delete).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String keyText = inputKeyEditText.getText().toString();
				if (keyText.equals("")) {
					return;
				}
				contentResolver.delete(uri, keyText, null);
				logTextView.append("Deleted: " + keyText + "\n");
				inputKeyEditText.setText("");
				inputValueEditText.setText("");
			}
		});

		findViewById(R.id.button_ldump).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Cursor queryResults = contentResolver.query(uri,null,"@",null, null);
				queryResults.moveToFirst();
				if (queryResults.isAfterLast()) {
					logTextView.append("LDump: LOCAL EMPTY\n");
				}
				while(!queryResults.isAfterLast()) {
					String key = queryResults.getString(queryResults.getColumnIndex(COLUMNS[0]));
					String value = queryResults.getString(queryResults.getColumnIndex(COLUMNS[1]));

					String pair = key + " : " + value;
					logTextView.append("LDump: " + pair + "\n");
					queryResults.moveToNext();
				}
			}
		});

		findViewById(R.id.button_gdump).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Cursor queryResults = contentResolver.query(uri,null,"*",null, null);
				queryResults.moveToFirst();
				if (queryResults.isAfterLast()) {
					logTextView.append("GDump: GLOBAL EMPTY\n");
				}
				while(!queryResults.isAfterLast()) {
					String key = queryResults.getString(queryResults.getColumnIndex(COLUMNS[0]));
					String value = queryResults.getString(queryResults.getColumnIndex(COLUMNS[1]));

					String pair = key + " : " + value;
					logTextView.append("GDump: " + pair + "\n");
					queryResults.moveToNext();
				}
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.simple_dynamo, menu);
		return true;
	}

	@Override
	public void onStop() {
        super.onStop();
	    Log.v("Test", "onStop()");
	}
}
