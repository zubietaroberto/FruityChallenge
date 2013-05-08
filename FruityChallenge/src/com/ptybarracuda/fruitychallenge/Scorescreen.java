package com.ptybarracuda.fruitychallenge;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ptybarracuda.fruitchallenge.R;

public class Scorescreen extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.scoreboard);
		TextView ScoreText = (TextView) this.findViewById(R.id.score);
		ScoreText.setText(String.valueOf(this.getIntent().getIntExtra("finalScore", 0)));
		
	}
	
	public void endGame(View pView){
		finish();
	}

}
