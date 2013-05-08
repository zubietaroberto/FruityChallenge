package com.ptybarracuda.fruitychallenge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ptybarracuda.fruitchallenge.R;


public class Launcher extends Activity {
	static int mRepetitions = 0, mCurrentScore = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main);
	}
	
	public void startGame(View pButton){
		Intent i = new Intent(this, com.ptybarracuda.fruitychallenge.FruityChallenge.class);
		try{
			EditText et = (EditText) findViewById(R.id.lvlEntry);
			i.putExtra("strLevel", Integer.parseInt(et.getText().toString()) * (Launcher.mRepetitions + 1));			
			startActivityForResult(i, 1);
		}catch (NumberFormatException pE){
			Toast.makeText(this, R.string.act1_errorstring1, Toast.LENGTH_SHORT).show();
			return;
		}
	}

	@Override
	protected void onActivityResult(int pRequestCode, int pResultCode, Intent pData) {
		switch (pRequestCode){
		case 1:
			if (pResultCode == RESULT_OK){
				if (Launcher.mRepetitions++ < 10){
					Launcher.mCurrentScore += pData.getIntExtra("strScore", 0);
					this.startGame(null);
				} else{
					Intent i = new Intent(this, com.ptybarracuda.fruitychallenge.Scorescreen.class);
					i.putExtra("finalScore", Launcher.mCurrentScore);
					startActivity(i);
					finish();
				}
			} else if (pResultCode == RESULT_CANCELED){
				Toast.makeText(this, R.string.act1_errorstring2, Toast.LENGTH_SHORT).show();
				finish();
			}
			
		}
	}

}
