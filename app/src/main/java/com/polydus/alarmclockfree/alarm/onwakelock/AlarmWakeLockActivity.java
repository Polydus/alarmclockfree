package com.polydus.alarmclockfree.alarm.onwakelock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.polydus.alarmclockfree.MainActivity;
import com.polydus.alarmclockfree.R;
import com.polydus.alarmclockfree.alarm.preferences.AlarmPreferencesFragment;
import com.polydus.alarmclockfree.util.Constants;

import java.util.Timer;
import java.util.TimerTask;

public class AlarmWakeLockActivity extends Activity {

	private PowerManager.WakeLock wakeLock;

	private MediaPlayer mediaPlayer;

	private boolean vibrationEnabled;
	private boolean soundEnabled;
	private String soundUri;
	private Vibrator vibrator;

	private float volume;
	private int systemVolume;

	private boolean snoozeEnabled;
	private int snoozeTime;
	private boolean snoozeTickingDown;
	private int snoozeTimeTicker;
	private String snoozeTimeText;

	private Timer timer;
	private TimerTask timerTask;
	private Button snooze;

	private String message;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_wake_lock);

		/*if (!parent.hasExtra(AlarmService.LAUNCHED_FROM_SERVICE)){
			//closeActivity();
			soundEnabled = false;
		}*/
		SharedPreferences preferences = getSharedPreferences(Constants.ALARM_LAUNCH_PREFERENCES, MODE_PRIVATE);
		boolean alarmAllowed = preferences.getBoolean(Constants.ALARM_LAUNCH_ALLOWED, Constants.ALARM_LAUNCH_ALLOWED_DEFAULT);
		if (!alarmAllowed){
			closeActivity();
		} else {
			Intent parent = getIntent();

			setAlarmTitleText(parent);
			setSnooze(parent);
			addVibration(parent);
			addMusic(parent);

			addListeners();
			//ensure wakelock & release
			initWakeLock();
		}
	}

	private void setAlarmTitleText(Intent parent){
		message = parent.getStringExtra(Constants.X_ALARM_NAME);
	}

	private void addMusic(Intent parent){
		String musicSource = parent.getStringExtra(Constants.X_ALARM_SOURCE);
		String[] values = new String[]{
				getBaseContext().getString(R.string.alarm_ringtone_source_values_silent),
				getBaseContext().getString(R.string.alarm_ringtone_source_values_ringtone),
				getBaseContext().getString(R.string.alarm_ringtone_source_values_music)
		};
		if (musicSource.equals(values[0])){
			soundUri = "";
		} else if (musicSource.equals(values[1])){
			soundUri = parent.getStringExtra(Constants.X_ALARM_SOUND);
		} else if (musicSource.equals(values[2])){
			soundUri = parent.getStringExtra(Constants.X_ALARM_CUSTOM_SOUND);
		} else {
			//shouldn't happen
			soundUri = "";
		}
		System.out.println(soundUri);

		try {
			if (!soundUri.equals("")) {
				mediaPlayer = new MediaPlayer();
				Uri uri = Uri.parse(soundUri);
				int volumeSrc = parent.getIntExtra(Constants.X_ALARM_VOLUME, Constants.ALARM_VOLUME_DEFAULT);
				volume = (volumeSrc / 100f);

				//fix if phone alarm volume is down
				AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				systemVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
				audioManager.setStreamVolume(AudioManager.STREAM_ALARM, (volumeSrc / (100 / 7)), 0);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
					//apparently this is needed
					if (ContextCompat.checkSelfPermission(
							this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{"android.permission.READ_PHONE_STATE"}, 0);
				}

				if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT){
					SharedPreferences sharedPreferences = getSharedPreferences(Constants.ALARM_PREFERENCES, MODE_PRIVATE);

					grantUriPermission(getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
					final int takeFlags = sharedPreferences.getInt(AlarmPreferencesFragment.CUSTOM_SOUND_URI_FLAGS, -1);
					getContentResolver().takePersistableUriPermission(uri, takeFlags); //still works though
				}


				mediaPlayer.setDataSource(this, uri);
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				mediaPlayer.setLooping(true);
				mediaPlayer.prepare();
				mediaPlayer.setVolume(volume, volume);
				mediaPlayer.start();
				soundEnabled = true;
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	private void addVibration(Intent parent){
		vibrationEnabled = parent.getBooleanExtra(Constants.X_ALARM_VIBRATION, Constants.ALARM_VIBRATION_DEFAULT);
		if (vibrationEnabled){
			vibrator = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);
			long[] pattern = {0, 100, 1000};
			vibrator.vibrate(pattern, 0);
		}
	}

	private void setSnooze(Intent parent){
		snoozeEnabled = parent.getBooleanExtra(Constants.X_ALARM_SNOOZE_ENABLED, Constants.ALARM_SNOOZE_ENABLED_DEFAULT);
		if (snoozeEnabled){
			snoozeTime = parent.getIntExtra(Constants.X_ALARM_SNOOZE_TIME, Constants.ALARM_SNOOZE_TIME_DEFAULT);
			snoozeTickingDown = false;
			snoozeTimeText = snoozeTime + ":00";
			snoozeTime *= 60; //bc it ticks in seconds
			snoozeTimeTicker = snoozeTime;
		}
	}

	private void addListeners(){
		Button cancel = (Button) findViewById(R.id.alarm_wake_lock_cancel);
		snooze = (Button) findViewById(R.id.alarm_wake_lock_snooze);
		snooze.setText(getString(R.string.wakelock_activity_snooze_button) + "\n" + message);

		if (snoozeEnabled){
			snooze.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!snoozeTickingDown){
						snoozeTickingDown = true;
						if (vibrationEnabled) {
							vibrator.cancel();
						}
						if(soundEnabled){
							mediaPlayer.pause();
						}
						v.setEnabled(false);
						Button snooze = (Button) v;
						snooze.setText(snoozeTimeText + "\n" + message);
						startSnoozeTimer();
					}
				}
			});
		} else {
			snooze.setVisibility(View.GONE);
		}

		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (vibrationEnabled) {
					vibrator.cancel();
				}
				if (soundEnabled){
					mediaPlayer.stop();
					AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
					audioManager.setStreamVolume(AudioManager.STREAM_ALARM, systemVolume, 0);
				}
				if (timer != null){
					timerTask.cancel();
					timer.cancel();
					timer.purge();
				}
				if (wakeLock != null && wakeLock.isHeld()){
					wakeLock.release();
				}
				//Intent intent = new Intent(getApplicationContext(), MainActivity.class);
				//startActivityIfNeeded(intent, 0);
				closeActivity();
			}
		});
	}

	private void setWakeLockReleaseTimer(){
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

				if (wakeLock != null && wakeLock.isHeld()){
					wakeLock.release();
				}
			}
		};
		new Handler().postDelayed(runnable, (60 * 1000));
	}

	private void startSnoozeTimer(){
		timer = new Timer();
		timerTask = new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						snoozeTimeTicker--;
						if (snoozeTimeTicker == 0) {
							onSnoozeTimerTickedDown();
						} else {
							tickDownSnoozeText();
						}
					}
				});
			}
		};
		timer.scheduleAtFixedRate(timerTask, 1, 1000);
	}

	private void onSnoozeTimerTickedDown(){
		timerTask.cancel();
		timer.cancel();
		timer.purge();
		snoozeTickingDown = false;
		snoozeTimeTicker = snoozeTime;
		snoozeTimeText = (snoozeTime / 60) + ":00";
		snooze.setEnabled(true);
		if (vibrationEnabled) {
			long[] pattern = {0, 100, 1000};
			vibrator.vibrate(pattern, 0);
		}
		if(soundEnabled){
			mediaPlayer.start();
		}
		snooze.setText(getString(R.string.wakelock_activity_snooze_button) + "\n" + message);
		initWakeLock();
	}

	private void tickDownSnoozeText(){
		int minutes = snoozeTimeTicker / 60;
		int seconds = snoozeTimeTicker % 60;
		if (seconds < 10){
			snoozeTimeText = minutes + ":0" + seconds;
		} else {
			snoozeTimeText = minutes + ":" + seconds;
		}
		snooze.setText(snoozeTimeText + "\n" + message);
	}

	private void initWakeLock(){
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
		if(wakeLock == null){
			wakeLock = powerManager.newWakeLock(
					(PowerManager.FULL_WAKE_LOCK | PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP),
					this.getClass().getSimpleName());
		}
		if (!wakeLock.isHeld()){
			wakeLock.acquire();
		}
		setWakeLockReleaseTimer();
	}

	@Override
	protected void onResume() {
		super.onResume();
		initWakeLock();
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (wakeLock != null && wakeLock.isHeld()){
			wakeLock.release();
		}
	}

	@Override
	public void onBackPressed() {
		//super.onBackPressed();
	}

	private void closeActivity(){
		SharedPreferences preferences = getSharedPreferences(Constants.ALARM_LAUNCH_PREFERENCES, MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(Constants.ALARM_LAUNCH_ALLOWED, false);
		editor.apply();
		//this is confusing but this is actually the place where this activity transitions and closes
		/*SharedPreferences preferences = getSharedPreferences(Constants.ACTIVITY_STACK, MODE_PRIVATE);
		if (preferences.getBoolean(Constants.MAIN_ACTIVITY_ACTIVE, Constants.MAIN_ACTIVITY_ACTIVE_DEFAULT) ||
			preferences.getBoolean(Constants.ALARM_PREFERENCES_ACTIVITY_ACTIVE, Constants.ALARM_PREFERENCES_ACTIVITY_ACTIVE_DEFAULT)){
			super.onBackPressed();
		} else {
			Intent intent = new Intent(getApplicationContext(), MainActivity.class);
			startActivity(intent);
		}
		finish();*/
		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("exit", true);
		startActivity(intent);
		finish();
	}

	@Override
	protected void onDestroy() {
		if (soundEnabled){
			mediaPlayer.stop();
			AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			audioManager.setStreamVolume(AudioManager.STREAM_ALARM, systemVolume, 0);
			mediaPlayer = null;
		}
		if (vibrationEnabled){
			vibrator.cancel();
			vibrator = null;
		}
		super.onDestroy();
	}
}

/*
		if (wakeLock != null && wakeLock.isHeld()){
			wakeLock.release();
		}
 */