package com.polydus.alarmclockfree;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class StopwatchFragment extends Fragment {

	FragmentActivity context;

	public int stopwatchState;

	public final int STATE_CLEAN = 0;
	public final int STATE_RUNNING = 1;
	public final int STATE_STOPPED = 2;

	private TextView chronometer;
	private Timer timer;
	private TimerTask timerTask;
	private StringBuilder chronometerText;

	private int hours;
	private int minutes;
	private int seconds;
	private int tensOfSeconds;
	private int hundredsOfSeconds;

	private int lapInterval; //hundreds of seconds since lap start

	private SimpleAdapter lapListAdapter;
	private ArrayList<HashMap<String, String>> lapListItems;

	public StopwatchFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootview = inflater.inflate(R.layout.fragment_stopwatch, container, false);

		context = getActivity();

		lapListItems = new ArrayList<HashMap<String, String>>();
		lapListAdapter = new SimpleAdapter(
				context,
				lapListItems,
				R.layout.stopwatch_list_item,
				new String[]{"lap", "lapTime", "totalTime"},
				new int[]{R.id.stopwatch_lap_list_item1, R.id.stopwatch_lap_list_item2, R.id.stopwatch_lap_list_item3});


		ListView lapList = (ListView) rootview.findViewById(R.id.stopwatch_lap_list);
		lapList.setAdapter(lapListAdapter);


		chronometer = (TextView) rootview.findViewById(R.id.stopwatch_chronometer);

		chronometerText = new StringBuilder("0:00:00:00");
		chronometer.setText(chronometerText.toString());

		final Button button1 = (Button) rootview.findViewById(R.id.chronometer_button_1);
		final Button button2 = (Button) rootview.findViewById(R.id.chronometer_button_2);

		stopwatchState = STATE_CLEAN; //default state

		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (stopwatchState){
					case STATE_CLEAN:
						stopwatchState = STATE_RUNNING;
						button1.setText(R.string.stopwatch_fragment_button_1_running);
						button2.setText(R.string.stopwatch_fragment_button_2_running);
						startChronometer();
						break;
					case STATE_RUNNING:
						stopwatchState = STATE_STOPPED;
						button1.setText(R.string.stopwatch_fragment_button_1_stopped);
						button2.setText(R.string.stopwatch_fragment_button_2_stopped);
						stopChronometer();
						break;
					case STATE_STOPPED:
						stopwatchState = STATE_RUNNING;
						button1.setText(R.string.stopwatch_fragment_button_1_running);
						button2.setText(R.string.stopwatch_fragment_button_2_running);
						continueChronometer();
						break;
				}
			}
		});

		button2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (stopwatchState){
					case STATE_CLEAN:
						//does nothing
						break;
					case STATE_RUNNING:
						HashMap<String, String> newLap = new HashMap<String, String>();
						String lap = null;
						String lapTime = null;
						String totalTime = null;

						if (lapListAdapter.getCount() == 0){
							lapTime = chronometerText.toString();
							lapInterval = 0;
							LinearLayout listHeader = (LinearLayout) rootview.findViewById(R.id.stopwatch_list_header);
							listHeader.setVisibility(LinearLayout.VISIBLE);
						}
						if (lapListAdapter.getCount() < 9){
							lap = "0" + (lapListAdapter.getCount() + 1);
						} else {
							lap = "" + (lapListAdapter.getCount() + 1);
						}
						if (lapTime == null){
							lapTime = buildStringLap();
						}
						totalTime = chronometerText.toString();

						newLap.put("lap", lap);
						newLap.put("lapTime", lapTime);
						newLap.put("totalTime", totalTime);

						lapListItems.add(0, newLap);
						lapListAdapter.notifyDataSetChanged();
						break;
					case STATE_STOPPED:
						stopwatchState = STATE_CLEAN;
						button1.setText(R.string.stopwatch_fragment_button_1_clean);
						button2.setText(R.string.stopwatch_fragment_button_2_clean);
						cleanChronometer();
						lapListItems.clear();
						lapListAdapter.notifyDataSetChanged();
						LinearLayout listHeader = (LinearLayout) rootview.findViewById(R.id.stopwatch_list_header);
						listHeader.setVisibility(LinearLayout.INVISIBLE);
						break;
				}
			}
		});

		return rootview;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private void startChronometer(){
		timer = new Timer();
		chronometerText = new StringBuilder("0:00:00:00");
		hours = 0;
		minutes = 0;
		seconds = 0;
		tensOfSeconds = 0;
		hundredsOfSeconds = 0;

		chronometer.setText(chronometerText.toString());
		runChronometer();
	}

	private void continueChronometer(){
		timer = new Timer();
		runChronometer();
	}

	private void cleanChronometer(){
		chronometerText = new StringBuilder("0:00:00:00");
		stopChronometer();
	}

	private void stopChronometer(){
		timerTask.cancel();
		timer.cancel();
		timer.purge();
		chronometer.setText(chronometerText.toString());
	}

	private void runChronometer(){
		timer.scheduleAtFixedRate(timerTask = new TimerTask() {
			@Override
			public void run() {
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (stopwatchState == STATE_RUNNING){

							hundredsOfSeconds++;
							lapInterval++;

							if (hundredsOfSeconds == 10) {
								tensOfSeconds++;
								hundredsOfSeconds = 0;
								if (tensOfSeconds == 10) {
									seconds++;
									tensOfSeconds = 0;
									if (seconds == 60) {
										minutes++;
										seconds = 0;
										if (minutes == 60) {
											hours++;
											minutes = 0;
										}
									}
								}
							}

							chronometer.setText(buildStringTime(
									chronometerText, hours, minutes,
									seconds, tensOfSeconds, hundredsOfSeconds));
						} else {
							stopChronometer();
						}
					}
				});
			}
		}, 1, 10);
	}

	private String buildStringTime(StringBuilder string, int hours, int minutes, int seconds, int tensOfSeconds, int hundredsOfSeconds){
		if (hours > 0){
			string.setCharAt(0, Character.forDigit(hours, 10));
		}

		if (minutes < 10){
			string.setCharAt(3, Character.forDigit(minutes, 10));
		} else {
			String minute = Integer.toString(minutes);
			string.setCharAt(2, minute.charAt(0));
			string.setCharAt(3, minute.charAt(1));
		}

		if (seconds < 10){
			string.setCharAt(6, Character.forDigit(seconds, 10));
		} else {
			String second = Integer.toString(seconds);
			string.setCharAt(5, second.charAt(0));
			string.setCharAt(6, second.charAt(1));
		}

		string.setCharAt(8, Character.forDigit(tensOfSeconds, 10));
		string.setCharAt(9, Character.forDigit(hundredsOfSeconds, 10));
		
		return string.toString();
	}

	private String buildStringLap(){
		int hoursLap = 0;
		int minutesLap = 0;
		int secondsLap = 0;
		int tensOfSecondsLap = 0;
		int hundredsOfSecondsLap;

		while(lapInterval >= 10){
			tensOfSecondsLap++;
			lapInterval -= 10;
			if (tensOfSecondsLap == 10){
				secondsLap++;
				tensOfSecondsLap = 0;
				if (secondsLap == 60){
					minutesLap++;
					secondsLap = 0;
					if (minutesLap == 60){
						hoursLap++;
						minutesLap = 0;
					}
				}
			}
		}
		hundredsOfSecondsLap = lapInterval;
		lapInterval = 0;

		return buildStringTime(new StringBuilder("0:00:00:00"), hoursLap, minutesLap, secondsLap, tensOfSecondsLap, hundredsOfSecondsLap);
	}
}
