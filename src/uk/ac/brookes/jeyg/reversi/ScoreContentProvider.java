package uk.ac.brookes.jeyg.reversi;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class ScoreContentProvider extends ContentProvider {

	// database
	private ReversiDatabaseHelper database;

	// Used for the UriMatcher
	private static final int SCORES = 1;
	private static final int SCORE_ID = 2;

	private static final String AUTHORITY = "uk.ac.brookes.jeyg.reversi.ScoreContentProvider";

	private static final String BASE_PATH = "scores";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/scores";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/score";

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, SCORES);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", SCORE_ID);
	}

	@Override
	public boolean onCreate() {
		database = new ReversiDatabaseHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		// Uisng SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// Check if the caller has requested a column which does not exists
		checkColumns(projection);

		// Set the table
		queryBuilder.setTables(ScoreTable.TABLE_SCORE);

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case SCORES:
			sortOrder = ScoreTable.COLUMN_SCORE + " DESC LIMIT 10";
			break;
		case SCORE_ID:
			// Adding the ID to the original query
			queryBuilder.appendWhere(ScoreTable.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		switch (sURIMatcher.match(uri)) {
		case SCORES:
			return "vnd.android.cursor.dir/vnd.brookes.score";
		case SCORE_ID:
			return "vnd.android.cursor.item/vnd.brookes.score";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}	  }

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		long id = 0;
		switch (uriType) {
		case SCORES:
			id = sqlDB.insert(ScoreTable.TABLE_SCORE, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH + "/" + id);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case SCORES:
			rowsDeleted = sqlDB.delete(ScoreTable.TABLE_SCORE, selection,
					selectionArgs);
			break;
		case SCORE_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(ScoreTable.TABLE_SCORE,
						ScoreTable.COLUMN_ID + "=" + id, 
						null);
			} else {
				rowsDeleted = sqlDB.delete(ScoreTable.TABLE_SCORE,
						ScoreTable.COLUMN_ID + "=" + id 
						+ " and " + selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsUpdated = 0;
		switch (uriType) {
		case SCORES:
			rowsUpdated = sqlDB.update(ScoreTable.TABLE_SCORE, 
					values, 
					selection,
					selectionArgs);
			break;
		case SCORE_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(ScoreTable.TABLE_SCORE, 
						values,
						ScoreTable.COLUMN_ID + "=" + id, 
						null);
			} else {
				rowsUpdated = sqlDB.update(ScoreTable.TABLE_SCORE, 
						values,
						ScoreTable.COLUMN_ID + "=" + id 
						+ " and " 
						+ selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	private void checkColumns(String[] projection) {
		String[] available = { ScoreTable.COLUMN_NAME, ScoreTable.COLUMN_PHOTO,
				ScoreTable.COLUMN_ID, ScoreTable.COLUMN_SCORE };
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException("Unknown columns in projection");
			}
		}
	}


}
