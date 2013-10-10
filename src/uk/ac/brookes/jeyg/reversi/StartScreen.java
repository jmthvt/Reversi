package uk.ac.brookes.jeyg.reversi;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class StartScreen extends Activity {
	
	private Button buttonSettings;
	private Button buttonStartSinglePlayerGame;
	private Button buttonStartTwoPlayerGame;
	private Button buttonHighScore;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_screen);
		
		// Widget biding
		buttonSettings = (Button) findViewById(R.id.buttonSettings);
		buttonStartTwoPlayerGame = (Button) findViewById(R.id.buttonTwoPlayerGame);
		buttonStartSinglePlayerGame = (Button) findViewById(R.id.buttonSinglePlayerGame);
		buttonHighScore = (Button) findViewById(R.id.buttonHighScore);
		
		// Listeners
		buttonSettings.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
				startActivity(intent);
			}
		});
		
		buttonHighScore.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), HighScoreActivity.class);
				startActivity(intent);
			}
		});
		
		buttonStartSinglePlayerGame.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), BoardActivity.class);
				intent.putExtra("gameMode", 1);
				startActivity(intent);				
			}
		});
		
		buttonStartTwoPlayerGame.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), BoardActivity.class);
				intent.putExtra("gameMode", 2);
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_start_screen, menu);
		return true;
	}

}
