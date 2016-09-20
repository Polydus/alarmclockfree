package com.polydus.alarmclockfree.timer;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;

import com.polydus.alarmclockfree.R;
import com.polydus.alarmclockfree.util.Constants;

import java.util.TimerTask;

/**
 * Created by leonard on 18-5-15.
 */
public class Timer {

	private TimerFragment parent;
	protected int id;

	private java.util.Timer timer;
	private TimerTask timerTask;
	private int countDownTicker;

	private SharedPreferences preferences;

	public Timer(int id, TimerFragment parent) {
		this.id = id;
		this.parent = parent;
		preferences = this.parent.getActivity().getSharedPreferences(Constants.TIMER_PREFERENCES, Context.MODE_PRIVATE);
	}

	public boolean start(){
		int hours = preferences.getInt(id + Constants.X_TIMER_HOURS, Constants.TIMER_HOURS_DEFAULT);
		int minutes = preferences.getInt(id + Constants.X_TIMER_MINUTES, Constants.TIMER_MINUTES_DEFAULT);
		int seconds = preferences.getInt(id + Constants.X_TIMER_SECONDS, Constants.TIMER_SECONDS_DEFAULT);

		if (hours == 0 && minutes == 0 && seconds == 0){
			return false;
		}

		countDownTicker = seconds;
		countDownTicker += (minutes * 60);
		countDownTicker += (hours * 3600);

		final TextView countDownTextView = (TextView) parent.timerListView.getChildAt(id).findViewById(R.id.timer_item_countdown_text);

		timer = new java.util.Timer();
		timerTask = new TimerTask() {
			@Override
			public void run() {
				try{
					parent.getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							countDownTicker--;
							countDownTextView.setText(parent.buildTimerString(
									countDownTicker / 3600, //hours
									(countDownTicker % 3600) / 60, //minutes
									countDownTicker % 60 //seconds
							));
							if (countDownTicker == 0){
								try {
									parent.onTimerReachesZero(id);
								} catch (Exception e) {
									e.printStackTrace();
								}
								stop();
							}
						}
					});
				} catch (Exception e){
					e.printStackTrace();
					stop();
				}
			}
		};
		timer.scheduleAtFixedRate(timerTask, 1, 1000);
		return true;
	}

	public void stop(){
		stopTicking();
	}

	private void stopTicking(){
		timerTask.cancel();
		timer.cancel();
		timer.purge();
	}
}
