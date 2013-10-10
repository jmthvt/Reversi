package uk.ac.brookes.jeyg.reversi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ReversiDatabaseHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "reversi.db";
  private static final int DATABASE_VERSION = 5;

  public ReversiDatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  // Method is called during creation of the database
  @Override
  public void onCreate(SQLiteDatabase database) {
    PlayerTable.onCreate(database);
    ScoreTable.onCreate(database);
  }

  // Method is called during an upgrade of the database,
  // e.g. if you increase the database version
  @Override
  public void onUpgrade(SQLiteDatabase database, int oldVersion,
      int newVersion) {
    PlayerTable.onUpgrade(database, oldVersion, newVersion);
    ScoreTable.onUpgrade(database, oldVersion, newVersion);
  }
} 