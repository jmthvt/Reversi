package uk.ac.brookes.jeyg.reversi;

import java.io.File;
import java.util.Random;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class BoardActivity extends Activity {

	// Variables
	private int[] board = new int[64];
	private int[][] board2d = new int[8][8];
	private ImageAdapter boardAdapter;
	private GridView boardGrid;
	private TextView playerNameTextView;
	private TextView countdownTextView;
	private ImageView tokenImage;
	private ImageView playerImage;
	private CountDownTimer countDown;
	private CountDownTimer countDownRing;
	private Boolean isTimerOn;
	private Boolean isSoundOn;
	private String playerOneName;
	private String playerTwoName;
	private String playerOnePicturePath;
	private String playerTwoPicturePath;
	private int playerNo=1;
	private int playerOneScore=2;
	private int playerTwoScore=2;
	private int blackTile;
	private int whiteTile;
	private int gameMode;

	private static Random rnd = new Random();

	// Constants
	private static final int EMPTY = 0;
	private static final int BLACK = 1;
	private static final int WHITE = 2;
	private static final int VALID = 3;
	private static final int[] OFFSET_X = {-1, -1, -1,  0,  0,  1,  1,  1};
	private static final int[] OFFSET_Y = {-1,  0,  1, -1,  1, -1,  0,  1};
	private static final int EXIT_GAME = 41;
	private static final int END_GAME_TIMEOUT_DIALOG = 42;
	private static final int END_GAME_DIALOG = 43;
	private static final int MODE_CLASSIC = 100;
	private static final int MODE_TIME = 101;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_board);

		// Get game mode (single or two players)
		gameMode = getIntent().getIntExtra("gameMode", 1);

		// Preferences reading
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		isTimerOn = sharedPref.getBoolean("pref_key_timer", false);
		isSoundOn = sharedPref.getBoolean("pref_sound", false);
		final String timerDuration = sharedPref.getString("pref_key_timer_duration", "5 minutes");
		final String tilesSet = sharedPref.getString("pref_key_tiles_sets", "Black and White");

		// Action Bar
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		// Widget biding
		boardGrid = (GridView) findViewById(R.id.board_grid);
		tokenImage = (ImageView) findViewById(R.id.imageview_token);
		playerNameTextView = (TextView) findViewById(R.id.textview_player_name);
		playerImage = (ImageView) findViewById(R.id.imageview_player);

		if (isTimerOn) {
			countdownTextView = (TextView) findViewById(R.id.textview_countdown);
			countdownTextView.setVisibility(View.VISIBLE);
		}
		
		// Tiles Set initialization
		if (tilesSet.equals("Black and White")) {
			blackTile = R.drawable.black_circle;
			whiteTile = R.drawable.white_circle;
		}
		else if (tilesSet.equals("Red and Blue")) {
			blackTile = R.drawable.red_circle;
			whiteTile = R.drawable.blue_circle;
		}
		else if (tilesSet.equals("Red and White")) {
			blackTile = R.drawable.red_circle;
			whiteTile = R.drawable.white_circle;
		}

		// Image Adapter
		boardAdapter = new ImageAdapter(this,board,blackTile,whiteTile);
		boardGrid.setAdapter(boardAdapter);

		// Players name and image initialization from the content provider
		initPlayers();

		// Timer initialization on Timed mode
		initTimer(timerDuration);

		// Board initialization
		board2d[3][3] = WHITE;
		board2d[3][4] = BLACK;
		board2d[4][3] = BLACK;
		board2d[4][4] = WHITE;

		board[27] = WHITE;
		board[28] = BLACK;
		board[35] = BLACK;
		board[36] = WHITE;

		// Player One turn initialization
		playerNameTextView.setText(playerOneName);
		tokenImage.setImageResource(blackTile);
		changePlayerPicture(1,playerOnePicturePath);
		getValidMoves(BLACK);

		// Start timer on Timed mode
		startTimer();

		// Listeners
		boardGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// if item clicked during IA turn, do nothing
				if (playerNo==2 && gameMode==1) {
					return;
				}
				// if position is valid, cancel the timer and put the token
				if (board[position]==VALID) {
					cancelTimer();
					board[position]=playerNo;
					// if it's player one turn, spin black tokens and get valid moves for the next turn
					if (playerNo==1) {
						playerNo=2;
						spinTokens1D(BLACK,position);
						getValidMoves(WHITE);
						// we verify that the game is'nt finished before going further
						if (endCheck(WHITE) == false) {
							playerNameTextView.setText(playerTwoName);
							changePlayerPicture(2,playerTwoPicturePath);
							tokenImage.setImageResource(whiteTile);
							// if we are in Single Player Mode, do play the AI, then prepare the player1 turn.
							if (gameMode == 1) {
								boardAdapter.notifyDataSetChanged();
								Handler handler = new Handler(); 
								handler.postDelayed(new Runnable() { 
									public void run() { 
										getAIMove();
										playerNo=1;
										getValidMoves(BLACK);
										endCheck(BLACK);
										playerNameTextView.setText(playerOneName);
										changePlayerPicture(1,playerOnePicturePath);
										tokenImage.setImageResource(blackTile);
										startTimer();
										boardAdapter.notifyDataSetChanged();
									} 
								}, 2000);
							}
							// if we are in Two Player mode, prepare the player2 turn
							else {
								getValidMoves(WHITE);
								endCheck(WHITE);
								startTimer();
							}
						}
						// if the game is finished, update High Score and show ending dialog
						else {
							setHighScore(MODE_CLASSIC);
							showDialog(END_GAME_DIALOG);
						}
					}
					// if it's player2 turn, spin white tokens and get valid moves for the next turn
					else {
						playerNo=1;
						spinTokens1D(WHITE,position);
						getValidMoves(BLACK);
						endCheck(BLACK);
						playerNameTextView.setText(playerOneName);
						changePlayerPicture(1,playerOnePicturePath);
						tokenImage.setImageResource(blackTile);
						startTimer();
					}
					// update the board display
					boardAdapter.notifyDataSetChanged();
				}
				// invalid move tried
				else {
					Toast.makeText(getApplicationContext(), "Can't do that", Toast.LENGTH_SHORT).show();
				}
			}

		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isTimerOn) {
			countDown.cancel();
			if (isSoundOn) {
				countDownRing.cancel();	
			}
		}
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		if (isTimerOn) {
			countDown.start();
			if (isSoundOn) {
				countDownRing.start();	
			}
		}
	}

	@Override
	// Physical Back Button
	public void onBackPressed() {
		showDialog(EXIT_GAME);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_board, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// Back icon in action bar clicked; show dialog to go home
			showDialog(EXIT_GAME);
			break;
		case R.id.action_replay:
			// Start a New Game
			recreate();
			break;
		case R.id.action_pass:
			passTurn();
			break;
		case R.id.action_score:
			// Display score
			getScore();
			Toast.makeText(this, "Score\n\n" +  playerOneName + ": " + String.valueOf(playerOneScore) + 
					"\n" + playerTwoName + ": " + String.valueOf(playerTwoScore), Toast.LENGTH_SHORT).show();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public Dialog onCreateDialog(int id) {
		Builder builder = new AlertDialog.Builder(this);
		AlertDialog dialog;
		switch (id) {
		case END_GAME_DIALOG:
			builder.setMessage("Game Over! \nThe winner is: " + getWinner() + ".\n\nScore\n" + playerOneName +
					": " + String.valueOf(playerOneScore) + "\n" + playerTwoName + ": " + String.valueOf(playerTwoScore));
			builder.setCancelable(false);

			builder.setPositiveButton("Menu", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					Intent intent = new Intent(getApplicationContext(),StartScreen.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
			});

			builder.setNegativeButton("New Game", 
					new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					recreate();
				}
			});
			dialog = builder.create();
			dialog.show();
			break;
		case END_GAME_TIMEOUT_DIALOG:
			builder.setMessage("Time Out! " + getTimeoutWinner() + " Won!");
			builder.setCancelable(false);

			builder.setPositiveButton("Menu", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					Intent intent = new Intent(getApplicationContext(),StartScreen.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
			});

			builder.setNegativeButton("New Game", 
					new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					recreate();
				}
			});
			dialog = builder.create();
			dialog.show();
			break;
		case EXIT_GAME:
			builder.setMessage("This will end the game.\nAre you sure ?");
			builder.setCancelable(false);

			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					Intent intent = new Intent(getApplicationContext(),StartScreen.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
			});

			builder.setNegativeButton("No", 
					new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			dialog = builder.create();
			dialog.show();
			break;
		}
		return super.onCreateDialog(id);
	}

	public void startTimer() {
		if (isTimerOn) {
			countDown.start();
			if (isSoundOn) {
				countDownRing.start();
			} 
		}
	}

	public void cancelTimer() {
		if (isTimerOn) {
			countDown.cancel();
			if (isSoundOn) {
				countDownRing.cancel();	
			} 
		}
	}

	public void setHighScore(int mode) {
		int score;
		String winner;
		String name;
		String photoPath;

		if (mode == MODE_CLASSIC) {
			winner = getWinner();
		}
		else {
			winner = getTimeoutWinner();
		}


		if (winner.equals("Android")) {
			return;
		}
		else if (winner.equals(playerOneName)) {
			score = playerOneScore;
			name = playerOneName;
			photoPath = playerOnePicturePath;
		}
		else if (winner.equals(playerTwoName)) {
			score = playerTwoScore;
			name = playerTwoName;
			photoPath = playerTwoPicturePath;
		}
		else { return; }

		Uri ScoreUri = Uri.parse(ScoreContentProvider.CONTENT_URI + "/");
		ContentValues values = new ContentValues();
		values.put(ScoreTable.COLUMN_NAME, name);
		values.put(ScoreTable.COLUMN_PHOTO, photoPath);
		values.put(ScoreTable.COLUMN_SCORE, score);
		getContentResolver().insert(ScoreUri, values);
	}

	// Get score by counting each token
	public void getScore() {
		playerOneScore = 0;
		playerTwoScore = 0;
		for (int x = 0; x < 64; x++) {
			if (board[x] == BLACK) {
				playerOneScore++;
			}
			else if (board[x] == WHITE) {
				playerTwoScore++;
			}
		}
	}

	public String getWinner() {
		getScore();
		if (playerOneScore > playerTwoScore) {
			return playerOneName;
		}
		else if (playerOneScore < playerTwoScore) {
			return playerTwoName;
		}
		else {
			return "... Nobody, Draw !";
		}
	}

	public String getTimeoutWinner() {
		if (playerNo == 1) {
			return playerOneName;
		}
		else {
			return playerTwoName;
		}
	}

	public Boolean endCheck(int tokenColor) {
		Boolean isGameEnded = isGameEnded(tokenColor);
		if (isGameEnded) {
			cancelTimer();
			setHighScore(MODE_CLASSIC);
			showDialog(END_GAME_DIALOG);
		}
		return isGameEnded;
	}

	public boolean isGameEnded(int tokenColor) {
		boolean isGameEnded = false;
		boolean valid1 = false;
		for (int x = 0; x < 64; x++) {
			if (board[x]==VALID) {
				valid1 = true;
				break;
			}
		}
		if (valid1 == false) {
			if (tokenColor == BLACK) {
				getValidMoves(WHITE);
			}
			else {
				getValidMoves(BLACK);
			}
			boolean valid2 = false;
			for (int x = 0; x < 64; x++) {
				if (board[x]==VALID) {
					valid2 = true;
					break;
				}
			}
			if (valid2 == false) {
				isGameEnded = true;
			}
		}
		else {
			getValidMoves(tokenColor);
			isGameEnded = false;
		}
		return isGameEnded;
	}

	public void initTimer(String timerDuration) {
		if (isTimerOn) {
			int msTimerDuration = 60000;
			if (timerDuration.equals("2 minutes")) {
				msTimerDuration = 120000;
			}
			else if (timerDuration.equals("3 minutes")) {
				msTimerDuration = 180000;
			}
			//Countdown
			countDown =  new CountDownTimer(msTimerDuration, 1000) {
				public void onTick(long millisUntilFinished) {
					countdownTextView.setText("Time remaining: " + millisUntilFinished / 1000);
				}
				public void onFinish() {
					if (playerNo==1) {
						playerNo=2;
					}
					else {
						playerNo=1;
					}
					setHighScore(MODE_TIME);
					showDialog(END_GAME_TIMEOUT_DIALOG);
				}};
				//Ringtone Countdown
				if (isSoundOn) {
					countDownRing =  new CountDownTimer(msTimerDuration, 29000) {
						public void onTick(long millisUntilFinished) {
							RingtoneManager.getRingtone(getApplicationContext(),
									RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).play();
						}

						public void onFinish() {

						}
					};
				}
		}
	}

	public void initPlayers() {
		Cursor c = getContentResolver().query(Uri.parse(ContactContentProvider.CONTENT_URI + "/" + "1"), null, null, null, null);
		if (c.moveToFirst()) {
			playerOneName = c.getString(c.getColumnIndex(PlayerTable.COLUMN_NAME));
			playerOnePicturePath = c.getString(c.getColumnIndex(PlayerTable.COLUMN_PHOTO));
		}
		c.close();
		if (gameMode == 1) {
			playerTwoName = "Android";
		}
		else {
			c = getContentResolver().query(Uri.parse(ContactContentProvider.CONTENT_URI + "/" + "2"), null, null, null, null);
			if (c.moveToFirst()) {
				playerTwoName = c.getString(c.getColumnIndex(PlayerTable.COLUMN_NAME));
				playerTwoPicturePath = c.getString(c.getColumnIndex(PlayerTable.COLUMN_PHOTO));
			}
			c.close();	
		}
	}

	public void changePlayerPicture(int player, String picturePath) {
		if (picturePath == null && player == 2 && gameMode == 1) {
			playerImage.setImageResource(R.drawable.android_contact);
		}
		else if (picturePath == null) {
			playerImage.setImageResource(R.drawable.default_contact);
		}
		else {
			File imgFile = new  File(picturePath);
			if(imgFile.exists()){
				Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
				playerImage.setImageBitmap(myBitmap);
			}
		}
	}

	// Pass a turn
	public void passTurn() {
		// If we try to pass during Android turn, do nothing
		if (playerNo == 2 && playerTwoName.equals("Android")) {
			return;
		}
		else {
			cancelTimer();
			if (playerNo==1) {
				playerNo=2;
				getValidMoves(WHITE);
				if (isGameEnded(WHITE) == false) {
					playerNameTextView.setText(playerTwoName);
					changePlayerPicture(2,playerTwoPicturePath);
					tokenImage.setImageResource(whiteTile);
					// if we're playing in Single Player mode, do the AI play
					if (gameMode == 1) {
						Handler handler = new Handler(); 
						handler.postDelayed(new Runnable() { 
							public void run() { 
								getAIMove();
								playerNo=1;
								getValidMoves(BLACK);
								endCheck(BLACK);
								playerNameTextView.setText(playerOneName);
								changePlayerPicture(1,playerOnePicturePath);
								tokenImage.setImageResource(blackTile);
								startTimer();
								boardAdapter.notifyDataSetChanged();
							} 
						}, 2000);
					}
					// else, if we're in Two Players mode, prepare the 2nd player turn 
					else {
						getValidMoves(WHITE);
						endCheck(WHITE);
						startTimer();
					}
				}
			}
			else {
				playerNo=1; 
				playerNameTextView.setText(playerOneName);			
				changePlayerPicture(1,playerOnePicturePath);
				tokenImage.setImageResource(blackTile);
				getValidMoves(BLACK);
				startTimer();
			}
		}
	}

	// Computes all possible moves
	public void getValidMoves(int tokenColor) { 
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				// clean previous valid moves
				if (board2d[x][y] == VALID) {
					board2d[x][y] = EMPTY;
				}

				// If the move is valid, we update the board accordingly
				if (isValidMove(tokenColor, x, y)) {
					board2d[x][y] = VALID;
				}
			}
		}
		convert2Dto1DArray();
	}

	// AI turn
	public void getAIMove() {
		Boolean hasPlayed = false;
		tabloop:
			for (int x = 0; x < 8; x++) {
				for (int y = 0; y < 8; y++) {
					if (board2d[x][y] == VALID) {
						if (rnd.nextBoolean()) {
							spinTokens2D(WHITE,x,y);
							hasPlayed = true;
							break tabloop;
						}
					}
				}
			}
		if (hasPlayed == false) {
			getAIMove();
		} else {
			convert2Dto1DArray();
		}
	}

	// Checks the validity of the move
	// Inspired by https://github.com/luugiathuy/Reversi/blob/master/src/com/luugiathuy/games/reversi/Reversi.java
	public boolean isValidMove(int tokenColor, int x, int y) {
		// check if the square is empty
		if (board2d[x][y] != EMPTY)
			return false;

		int opponentTokenColor = BLACK;
		boolean isValid = false;

		if (tokenColor == BLACK) {
			opponentTokenColor = WHITE;
		}

		int curRow;
		int curCol;
		boolean hasOpponentTokenBetween;
		for (int i = 0; i < 8; ++i) {
			curRow = x + OFFSET_X[i];
			curCol = y + OFFSET_Y[i];
			hasOpponentTokenBetween = false;
			while (curRow >=0 && curRow < 8 && curCol >= 0 && curCol < 8) {
				if (board2d[curRow][curCol] == opponentTokenColor)
					hasOpponentTokenBetween = true;
				else if ((board2d[curRow][curCol] == tokenColor) && hasOpponentTokenBetween)
				{
					isValid = true;
					break;
				}
				else
					break;

				curRow += OFFSET_X[i];
				curCol += OFFSET_Y[i];
			}
			if (isValid)
				break;
		}
		return isValid;
	}

	// Convert a 2D to 1D array
	public void convert2Dto1DArray() {
		int i = 0;
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				board[i]=board2d[x][y];
				i++;
			}
		}
	}

	// Convert 1D table to 2D and call spinTokens2D
	public void spinTokens1D(int tokenColor, int position) {
		int p = 0;
		// Must initiate these coordinates to 0 because Eclipse
		// doesn't like variable which may not be initialized.
		int xCoor = 0;
		int yCoor = 0;
		// Get the 2D coordinates from the 1D
		tabloop:
			for (int x = 0; x < 8; x++) {
				for (int y = 0; y < 8; y++) {
					if (p==position) {
						xCoor = x;
						yCoor = y;
						break tabloop;
					}
					p++;
				}
			}
		spinTokens2D(tokenColor, xCoor, yCoor);
	}


	// Spin tokens from the 2D table
	// Inspired by https://github.com/luugiathuy/Reversi/blob/master/src/com/luugiathuy/games/reversi/Reversi.java
	public void spinTokens2D(int tokenColor, int xCoor, int yCoor) {
		board2d[xCoor][yCoor] = tokenColor;
		int curRow;
		int curCol;
		for (int i = 0; i < 8; i++) {
			curRow = xCoor + OFFSET_X[i];
			curCol = yCoor + OFFSET_Y[i];
			boolean hasOppPieceBetween = false;
			while (curRow >=0 && curRow < 8 && curCol >= 0 && curCol < 8) {
				// if empty or valid square, break
				if (board2d[curRow][curCol]==EMPTY ||board2d[curRow][curCol]==VALID) {
					break;
				}

				if (board2d[curRow][curCol] != tokenColor) {
					hasOppPieceBetween = true;
				}
				if ((board2d[curRow][curCol] == tokenColor) && hasOppPieceBetween)
				{
					int spinTokenX = xCoor + OFFSET_X[i];
					int spinTokenY = yCoor + OFFSET_Y[i];
					while (spinTokenX != curRow || spinTokenY != curCol)
					{
						board2d[spinTokenX][spinTokenY] = tokenColor;
						spinTokenX += OFFSET_X[i];
						spinTokenY += OFFSET_Y[i];
					}

					break;
				}
				curRow += OFFSET_X[i];
				curCol += OFFSET_Y[i];
			}
		}
	}
}