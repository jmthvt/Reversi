package uk.ac.brookes.jeyg.reversi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class PlayerDetails extends ListActivity implements
LoaderManager.LoaderCallbacks<Cursor> {

	private final int PICK_CONTACT=10;

	private SimpleCursorAdapter adapter;

	private long playerId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player_details);
		
		ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

		fillData();
	}


	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		playerId = id;
		Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(intent, PICK_CONTACT);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_player_details, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            // Back icon in action bar clicked; go home
	            Intent intent = new Intent(this, SettingsActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	private void fillData() {
		// Fields from the database (projection)
		// Must include the _id column for the adapter to work
		String[] from = new String[] { PlayerTable.COLUMN_ID, PlayerTable.COLUMN_NAME, PlayerTable.COLUMN_PHOTO };
		// Fields on the UI to which we map
		int[] to = new int[] { R.id.textview_player_number, R.id.textview_contact, R.id.imageview_contact };

		getLoaderManager().initLoader(0, null, this);
		adapter = new SimpleCursorAdapter(this, R.layout.contact_details_layout, null, from,
				to, 0);

		setListAdapter(adapter);
	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case (PICK_CONTACT) :
			if (resultCode == Activity.RESULT_OK) {
				String name=null;
				String oppName=null;
				Uri photoUri=null;
				InputStream photoStream=null;
				String photoPath=null;
				String contactId=null;
				Uri contactData = data.getData();
				Cursor c =  getContentResolver().query(contactData, null, null, null, null);

				if (c.moveToFirst()) {
					contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
					name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
					photoUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contactId));
					photoStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(), photoUri);
					}
				c.close();

				if (playerId == 1) {
					c = getContentResolver().query(Uri.parse(ContactContentProvider.CONTENT_URI + "/" + "2"), null, null, null, null);
					if (c.moveToFirst()) {
						oppName = c.getString(c.getColumnIndex(PlayerTable.COLUMN_NAME));
					}
				}
				else {				
					c = getContentResolver().query(Uri.parse(ContactContentProvider.CONTENT_URI + "/" + "1"), null, null, null, null);
					if (c.moveToFirst()) {
						oppName = c.getString(c.getColumnIndex(PlayerTable.COLUMN_NAME));
					}
				}
				c.close();
				Log.i("NAME", name);
				Log.i("OPP NAME", oppName);
				if (name.equals(oppName)) {

					Toast.makeText(this, "You must select two different contacts.", Toast.LENGTH_LONG).show();
				}
				else {
					// Get contact picture				
					 if (photoStream != null) {
				            Bitmap photo = BitmapFactory.decodeStream(photoStream);
						// Getting Caching directory
						File cacheDirectory = getBaseContext().getCacheDir();
						// Temporary file to store the contact image
						File tmpFile = new File(cacheDirectory.getPath() + "/rvrsi_"+contactId+".png");
						// The FileOutputStream to the temporary file
						try {
							FileOutputStream fOutStream = new FileOutputStream(tmpFile);
							// Writing the bitmap to the temporary file as png file
							photo.compress(Bitmap.CompressFormat.PNG,100, fOutStream);
							// Flush the FileOutputStream
							fOutStream.flush();
							//Close the FileOutputStream
							fOutStream.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
						photoPath = tmpFile.getPath();
					}
					// Save into the database
					Uri playerUri = Uri.parse(ContactContentProvider.CONTENT_URI + "/" + playerId);
					ContentValues values = new ContentValues();
					values.put(PlayerTable.COLUMN_NAME, name);
					values.put(PlayerTable.COLUMN_PHOTO, photoPath);
					getContentResolver().update(playerUri, values, null, null);
				}
			}
		break;
		}
		Intent intent = getIntent();
		finish();
		startActivity(intent);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle args) {
		String[] projection = { PlayerTable.COLUMN_ID, PlayerTable.COLUMN_NAME, PlayerTable.COLUMN_PHOTO };
		CursorLoader cursorLoader = new CursorLoader(this,
				ContactContentProvider.CONTENT_URI, projection, null, null, null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor data) {
		adapter.swapCursor(data);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// data is not available anymore, delete reference
		adapter.swapCursor(null);

	}
}