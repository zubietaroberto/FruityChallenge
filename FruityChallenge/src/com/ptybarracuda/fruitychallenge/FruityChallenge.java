package com.ptybarracuda.fruitychallenge;

import java.util.ArrayList;
import java.util.Iterator;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.scene.CameraScene;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.SpriteBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.HorizontalAlign;
import org.anddev.andengine.util.MathUtils;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

public class FruityChallenge extends BaseGameActivity implements IOnSceneTouchListener, IUpdateHandler {
	public static final int CAMERA_WIDTH = 480;
	public static final int CAMERA_HEIGHT = 320;
	float mSecondsSinceUpdate = 0;
	
	private Camera mCamera;
	private Texture mSpritesTexture, mScenesTexture, mTextTexture;
	private TextureRegion[] mFruitTextureRegion = new TextureRegion [6];
	private TextureRegion mBombTextureRegion, mGiftTextureRegion, mPauseTextureRegion, mBackgroundTextureRegion, mBasketTextureRegion;
	private Scene mMainScene;
	private CameraScene mPauseScene;
	private Sprite mBasket;
	private ArrayList<Sprite> mFrutas = new ArrayList<Sprite>();
	private int mDificultyLevel = 0, mTimeLimit = 0, mScore = 0;
	private Font mFont;
	private String mScoreboardText, mTimerText;
	
	public void gameOver() {
		Intent i = new Intent();
		i.putExtra("strScore", this.mScore);
		this.setResult(RESULT_OK, i);
		this.finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_DOWN){
			if (this.mEngine.isRunning()){
				this.mMainScene.setChildScene(mPauseScene, false, true, true);
				this.mEngine.stop();
			} else{
				this.mMainScene.clearChildScene();
				this.mEngine.start();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onLoadComplete() {
		//Nothing
	}

	@Override
	public Engine onLoadEngine() {
		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		Bundle launchIntent = getIntent().getExtras();
		if (launchIntent != null){
			this.mDificultyLevel = launchIntent.getInt("strLevel");
			
		} else {
			this.mDificultyLevel = 1;
		}
		this.mTimeLimit = 30;
		Toast.makeText(this, "Level: " + this.mDificultyLevel, Toast.LENGTH_SHORT).show();
		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera));
	}
	
	@Override
	public void onLoadResources() {		
		this.mSpritesTexture = new Texture(128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mFruitTextureRegion[0] = TextureRegionFactory.createFromAsset(mSpritesTexture, this, "gfx/f140.png", 0, 0);
		this.mFruitTextureRegion[1] = TextureRegionFactory.createFromAsset(mSpritesTexture, this, "gfx/f240.png", 41, 0);
		this.mFruitTextureRegion[2] = TextureRegionFactory.createFromAsset(mSpritesTexture, this, "gfx/f340.png", 82, 0);
		this.mFruitTextureRegion[3] = TextureRegionFactory.createFromAsset(mSpritesTexture, this, "gfx/f440.png", 0, 41);
		this.mFruitTextureRegion[4] = TextureRegionFactory.createFromAsset(mSpritesTexture, this, "gfx/f540.png", 41, 41);
		this.mFruitTextureRegion[5] = TextureRegionFactory.createFromAsset(mSpritesTexture, this, "gfx/f640.png", 82, 41);
		
		this.mBombTextureRegion = TextureRegionFactory.createFromAsset(mSpritesTexture, this, "gfx/bomb40.png", 0, 82);
		this.mGiftTextureRegion = TextureRegionFactory.createFromAsset(mSpritesTexture, this, "gfx/gift.png", 41, 82);
		
		this.mScenesTexture = new Texture(512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mBackgroundTextureRegion = TextureRegionFactory.createFromAsset(mScenesTexture, this, "gfx/fruitsfalling2mini.jpg", 0, 0);
		this.mPauseTextureRegion = TextureRegionFactory.createFromAsset(mScenesTexture, this, "gfx/paused.png", 0, 321);
		this.mBasketTextureRegion = TextureRegionFactory.createFromAsset(mScenesTexture, this, "gfx/basket.png", 0, 372);
		
		this.mTextTexture = new Texture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mFont = new Font(mTextTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 10, true, Color.WHITE);
		
		this.mEngine.getTextureManager().loadTextures(mSpritesTexture, mScenesTexture, mTextTexture);
		this.mEngine.getFontManager().loadFont(mFont);
		
	}

	@Override
	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		this.mPauseScene = new CameraScene(1, this.mCamera);
		
		final int x = CAMERA_WIDTH/2 - this.mPauseTextureRegion.getWidth()/2;
		final int y = CAMERA_HEIGHT/2 - this.mPauseTextureRegion.getHeight()/2;
		final Sprite pausedSprite = new Sprite(x, y, this.mPauseTextureRegion);
		this.mPauseScene.getLastChild().attachChild(pausedSprite);
		this.mPauseScene.setBackgroundEnabled(false);
		
		this.mMainScene = new Scene(1);
		this.mMainScene.setOnSceneTouchListener(this);
		this.mMainScene.setBackground(new SpriteBackground(new Sprite(0, 0, mBackgroundTextureRegion)));
		this.mBasket = new Sprite(CAMERA_WIDTH/2, CAMERA_HEIGHT/2, this.mBasketTextureRegion);	
		this.mMainScene.getLastChild().attachChild(mBasket);
		
		this.mMainScene.registerUpdateHandler(this);
		mScoreboardText = getString(com.ptybarracuda.fruitchallenge.R.string.ScoreboardText) + ": ";
		mTimerText = getString(com.ptybarracuda.fruitchallenge.R.string.TimerText)+ ": ";
		final ChangeableText scoreboard = new ChangeableText(0, 0, mFont, this.mScoreboardText + "0", HorizontalAlign.LEFT, this.mScoreboardText.length() + 5);
		final ChangeableText timer = new ChangeableText(0, scoreboard.getBaseHeight(), mFont, this.mTimerText + "0", HorizontalAlign.RIGHT, this.mScoreboardText.length() +5);
		scoreboard.registerUpdateHandler(new TimerHandler(1/20.0f, true, new ITimerCallback(){

			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				scoreboard.setText(mScoreboardText + mScore);				
			}
			
		}));
		timer.registerUpdateHandler(new TimerHandler(1/20.0f, true, new ITimerCallback(){

			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				timer.setText(mTimerText + FruityChallenge.this.mEngine.getSecondsElapsedTotal());
				
			}
			
		}));
		this.mMainScene.getLastChild().attachChild(timer);
		this.mMainScene.getLastChild().attachChild(scoreboard);
		
		this.mMainScene.registerUpdateHandler(new TimerHandler(this.mTimeLimit, false, new ITimerCallback(){

			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				FruityChallenge.this.gameOver();
				
			}
			
		}));
		
		return this.mMainScene;
	}
	
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if (pSceneTouchEvent.isActionDown()){
			this.mBasket.setPosition(pSceneTouchEvent.getX() - this.mBasket.getWidth()/2,
					pSceneTouchEvent.getY()-this.mBasket.getHeight()/2);
			return true;
		}
		return false;
	}

	@Override
	public void onUpdate(float pSecondsElapsed) {
		int difficulty = this.mDificultyLevel;
		this.mSecondsSinceUpdate += pSecondsElapsed;
		if (this.mSecondsSinceUpdate > difficulty/1000){
			int locacion = MathUtils.random(0, CAMERA_WIDTH -(int) this.mFruitTextureRegion[0].getWidth());
			Sprite frutaNueva = new Sprite(locacion, -mFruitTextureRegion[0].getHeight(), this.mFruitTextureRegion[this.mFrutas.size() % this.mFruitTextureRegion.length]);
			frutaNueva.registerEntityModifier(new MoveModifier(10/MathUtils.random(difficulty, difficulty*2), locacion, locacion, 0, CAMERA_HEIGHT + 1));
			this.mMainScene.getLastChild().attachChild(frutaNueva);
			this.mFrutas.add(frutaNueva);
			this.mSecondsSinceUpdate = 0;
		}
		Iterator<Sprite> itr = this.mFrutas.iterator();
		while (itr.hasNext()){
			Sprite pruebas = itr.next();
			if (pruebas.collidesWith(this.mBasket) ){
				this.removeSprite(pruebas, itr);
				this.increaseScore();
			} else if (pruebas.getX() > CAMERA_HEIGHT){
				this.removeSprite(pruebas, itr);				
			}
		}
	}

	@Override
	public void reset() {
		
	}
	
	private void increaseScore() {
		this.mScore += this.mDificultyLevel;
	}

	private void removeSprite(final Sprite pSprite, Iterator<Sprite> pIterator){
		pSprite.setVisible(false);
		this.runOnUpdateThread(new Runnable(){

			@Override
			public void run() {
				mMainScene.getLastChild().detachChild(pSprite);				
			}
			
		});
		pIterator.remove();
	}

}
