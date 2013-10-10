package uk.ac.brookes.jeyg.reversi;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class PlayerTable  {
	
	// Database table
	public static final String TABLE_PLAYERS = "players";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_PHOTO = "photo";
	
	// Database creation SQL
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_PLAYERS
			+ "("
			+ COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_NAME + " text not null, "
			+ COLUMN_PHOTO + " text"
			+ ");";
	
	private static final String DATABASE_SETUP_P1 = "insert into "
			+ TABLE_PLAYERS + "("
			+ COLUMN_NAME + ")"
			+ " values"
			+ "(" + "'Player1'" + ");";
	
	private static final String DATABASE_SETUP_P2 = "insert into "
			+ TABLE_PLAYERS + "("
			+ COLUMN_NAME + ")"
			+ " values"
			+ "(" + "'Player2'" + ");";

	public static void onCreate(SQLiteDatabase database) {
	    database.execSQL(DATABASE_CREATE);
	    database.execSQL(DATABASE_SETUP_P1);
	    database.execSQL(DATABASE_SETUP_P2);
	  }

	  public static void onUpgrade(SQLiteDatabase database, int oldVersion,
	      int newVersion) {
	    Log.w(PlayerTable.class.getName(), "Upgrading database from version "
	        + oldVersion + " to " + newVersion
	        + ", which will destroy all old data");
	    database.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYERS);
	    onCreate(database);
	  }
}
