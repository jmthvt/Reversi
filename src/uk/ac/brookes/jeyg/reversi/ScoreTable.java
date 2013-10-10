package uk.ac.brookes.jeyg.reversi;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ScoreTable  {
	
	// Database table
	public static final String TABLE_SCORE = "score";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_PHOTO = "photo";
	public static final String COLUMN_SCORE = "score";
	
	// Database creation SQL
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_SCORE
			+ "("
			+ COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_NAME + " text not null, "
			+ COLUMN_PHOTO + " text, "
			+ COLUMN_SCORE + " int not null"
			+ ");";

	public static void onCreate(SQLiteDatabase database) {
	    database.execSQL(DATABASE_CREATE);
	  }

	  public static void onUpgrade(SQLiteDatabase database, int oldVersion,
	      int newVersion) {
	    Log.w(ScoreTable.class.getName(), "Upgrading database from version "
	        + oldVersion + " to " + newVersion
	        + ", which will destroy all old data");
	    database.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORE);
	    onCreate(database);
	  }
}
