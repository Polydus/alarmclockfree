package com.polydus.alarmclockfree.timer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.polydus.alarmclockfree.R;
import com.polydus.alarmclockfree.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by leonard on 22-4-15.
 */
public class TimerFragment extends Fragment{

	private View rootView;
	private Bundle savedInstanceState;

	protected ListView timerListView;
	private SimpleAdapter timerListAdapter;
	private ArrayList<HashMap<String, String>> timerListItems;

	private SharedPreferences preferences;

	private NumberPicker hours;
	private NumberPicker minutes;
	private NumberPicker seconds;

	private boolean addedListeners;

	private ArrayList<com.polydus.alarmclockfree.timer.Timer> timers;
	private ArrayList<Integer> timersOnZero;

	private MediaPlayer mediaPlayer;
	private int systemVolume;

	private int initOrientation;
	private int layoutChanges;


	public TimerFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_timer, container, false);
		this.savedInstanceState = savedInstanceState;

		preferences = getActivity().getSharedPreferences(Constants.TIMER_PREFERENCES, Context.MODE_PRIVATE);

		//deleteAllPrefs();
		initOrientation = getResources().getConfiguration().orientation;
		resetAllTimers();

		return rootView;
	}

	private void resetAllTimers(){
		int amount = preferences.getInt(Constants.TIMER_AMOUNT, 0);
		SharedPreferences.Editor editor = preferences.edit();
		for(int i = 0; i < amount; i++){
			if (preferences.getBoolean(i + Constants.X_TIMER_STATUS, false)){
				editor.putBoolean(i + Constants.X_TIMER_STATUS, false);
			}
		}
		editor.apply();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		//stopMusic();
		super.onStop();
	}


	@Override
	public void onDestroy() {
		stopMusic();
		super.onDestroy();
	}

	@Override
	public void onResume() {
		layoutChanges = 0;
		if (getResources().getConfiguration().orientation != initOrientation){
			if (timers != null){
				for (int i = 0; i < timers.size(); i++){
					timers.get(i).stop();
					timers.remove(i);
				}
			}
			resetAllTimers();
		}
		//handle orientation on kitkat

		initList();
		addListItems();
		initListeners();

		super.onResume();
		System.out.println("afters super.onresume at: " + System.currentTimeMillis());
	}

	private void initList(){
		timerListView = (ListView) rootView.findViewById(R.id.timer_listview);
		timerListItems = new ArrayList<HashMap<String, String>>();
		timerListAdapter = new SimpleAdapter(
				getActivity(),
				timerListItems,
				R.layout.timer_list_item,
				new String[]{getString(R.string.timer_item_countdown_tag), getString(R.string.timer_item_pause_tag), getString(R.string.timer_item_stop_tag)},
				new int[]{R.id.timer_item_countdown_text, R.id.timer_item_pause_button, R.id.timer_item_stop_button}
		);

		/*if (timerListView.getFooterViewsCount() == 0){
			LayoutInflater inflater = getLayoutInflater(savedInstanceState);
			ViewGroup footer = (ViewGroup) inflater.inflate(R.layout.timer_list_footer, timerListView, false);
			timerListView.addFooterView(footer, null, true);
			timerListView.setFooterDividersEnabled(true);
		}*/
		timerListView.setAdapter(timerListAdapter);
		timerListView.setLongClickable(true);
		timerListView.setClickable(true);
	}

	private void addListItems(){
		int amount = preferences.getInt(Constants.TIMER_AMOUNT, 0);

		for (int i = 0; i < amount; i++){
			HashMap<String, String> newTimer = new HashMap<String, String>();
			newTimer.put(getString(R.string.timer_item_countdown_tag), preferences.getString(i + Constants.X_TIMER_TIME, Constants.TIMER_TIME_DEFAULT));
			newTimer.put(getString(R.string.timer_item_pause_tag), "");
			newTimer.put(getString(R.string.timer_item_stop_tag), "");
			timerListItems.add(newTimer);
			timerListAdapter.notifyDataSetChanged();
		}
	}

	private TextView footer;

	private void initListeners(){
		timerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//if (view.getId() == R.id.timer_list_footer) {
				//	showNumberPickerDialog(true, -1, Constants.TIMER_HOURS_DEFAULT, Constants.TIMER_MINUTES_DEFAULT, Constants.TIMER_SECONDS_DEFAULT);
				//} else {
					System.out.println(view.getId());
					System.out.println(view.getTag());
					System.out.println(position);
					System.out.println(id);
					showNumberPickerDialog(false, (int) id,
							preferences.getInt((int) id + Constants.X_TIMER_HOURS, Constants.TIMER_HOURS_DEFAULT),
							preferences.getInt((int) id + Constants.X_TIMER_MINUTES, Constants.TIMER_MINUTES_DEFAULT),
							preferences.getInt((int) id
									+ Constants.X_TIMER_SECONDS, Constants.TIMER_SECONDS_DEFAULT));
				//}
			}
		});

		footer = (TextView) getActivity().findViewById(R.id.timer_list_footer_text);
		footer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showNumberPickerDialog(true, -1, Constants.TIMER_HOURS_DEFAULT, Constants.TIMER_MINUTES_DEFAULT, Constants.TIMER_SECONDS_DEFAULT);
			}
		});
		addedListeners = false;
		System.out.println("in initlisteners at: " + System.currentTimeMillis());
		timerListView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

				if (!addedListeners){
					setListViewChildren();
				}
				//setListViewChildren();

				//System.out.println(v.);
				//if (!addedListeners) {
				//timerListAdapter.notifyDataSetChanged();
					/*if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT){
						if (layoutChanges <= preferences.getInt(Constants.TIMER_AMOUNT, 0)){
							layoutChanges++;
						} else {
							addedListeners = true;
						}
					} else {
						addedListeners = true;
					}*/
				//}
			}
		});

		/*timerListView.setRecyclerListener(new AbsListView.RecyclerListener() {
			@Override
			public void onMovedToScrapHeap(View view) {
				setListViewChildren();
			}
		});*/
	}

	private void setListViewChildren(){
		for (int i = 0; i < timerListView.getChildCount(); i++) {
			if (timerListView.getChildAt(i).getId() == R.id.timer_list_item) {
				System.out.println("in onlayoutchange at: " + System.currentTimeMillis());
				initPauseButton(i);
				initStopButton(i);
			}
		}
	}

	private void initPauseButton(final int id){
		ImageButton imageButton = (ImageButton) timerListView.getChildAt(id).findViewById(R.id.timer_item_pause_button);
		boolean status = preferences.getBoolean(id + Constants.X_TIMER_STATUS, Constants.TIMER_STATUS_DEFAULT);
		if (status){
			//imageButton.setImageResource(R.drawable.ic_pause_black_48dp);
			imageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_black_48dp));
		} else {
			//imageButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);
			imageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black_48dp));
		}
		imageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences.Editor editor = preferences.edit();
				ImageButton imageButton = (ImageButton) v;
				if (preferences.getBoolean(id + Constants.X_TIMER_STATUS, Constants.TIMER_STATUS_DEFAULT)) {
					try {
						stopCountDown(id);
					} catch (Exception e) {
						e.printStackTrace();
					}
					editor.putBoolean(id + Constants.X_TIMER_STATUS, false);
					//imageButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);
					imageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black_48dp));
					TextView countDownTextView = (TextView) timerListView.getChildAt(id).findViewById(R.id.timer_item_countdown_text);
					countDownTextView.setText(preferences.getString(id + Constants.X_TIMER_TIME, Constants.TIMER_TIME_DEFAULT));
				} else {
					if (startCountDown(id)) {
						//imageButton.setImageResource(R.drawable.ic_pause_black_48dp);
						imageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_black_48dp));
						editor.putBoolean(id + Constants.X_TIMER_STATUS, true);
					}
				}
				editor.apply();
			}
		});
	}

	private void initStopButton(final int id){
		ImageButton imageButton = (ImageButton) timerListView.getChildAt(id).findViewById(R.id.timer_item_stop_button);
		//imageButton.setImageResource(R.drawable.ic_delete_black_48dp);
		imageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_delete_black_48dp));

		imageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//this sucks
				int amount = preferences.getInt(Constants.TIMER_AMOUNT, 0);
				//if (amount != 0) amount++; //yay off by one errors
				for (int i = 0; i < amount; i++){
					if (preferences.getBoolean(i + Constants.X_TIMER_STATUS, Constants.TIMER_STATUS_DEFAULT)){
						try {
							stopCountDown(i);
							SharedPreferences.Editor editor = preferences.edit();
							editor.putBoolean(i + Constants.X_TIMER_STATUS, false);
							ImageButton imageButton = (ImageButton) timerListView.getChildAt(i).findViewById(R.id.timer_item_pause_button);
							//imageButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);
							imageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black_48dp));
							TextView countDownTextView = (TextView) timerListView.getChildAt(i).findViewById(R.id.timer_item_countdown_text);
							countDownTextView.setText(preferences.getString(i + Constants.X_TIMER_TIME, Constants.TIMER_TIME_DEFAULT));
							editor.apply();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				removeTimer(id);
			}
		});
	}

	private boolean startCountDown(int id){
		if (timers == null) timers = new ArrayList<>();
		com.polydus.alarmclockfree.timer.Timer timer = new com.polydus.alarmclockfree.timer.Timer(id, this);
		boolean result = timer.start();
		if (result){
			timers.add(timer);
		}
		return result;
	}

	protected void stopCountDown(int id) throws Exception {

		stopMusic();

		/*if (timersOnZero.size() == 1){
			if (timersOnZero.get(0) == id){
				stopMusic();
			}
		} else if (timersOnZero.size() > 1){
			for (int i = 0; i < timersOnZero.size();i++){
				if (timersOnZero.get(i) == id){
					timersOnZero.remove(i);
				}
			}
		}*/

		boolean timerRemoved = false;
		for (int i = 0; i < timers.size(); i++){
			if (timers.get(i).id == id){
				timers.get(i).stop();
				timers.remove(i);
				timerRemoved = true;
			}
		}
		if (!timerRemoved){
			throw new Exception("Timer not removed! watt");
		}
	}

	protected void onTimerReachesZero(int id) throws Exception {
		if (mediaPlayer == null){
			startMusic();
		} else if (!mediaPlayer.isPlaying()){
			startMusic();
		}
		if (timersOnZero == null) timersOnZero = new ArrayList<>();
		boolean canAddValue = true;
		for (int i = 0; i < timersOnZero.size(); i++){
			if (timersOnZero.get(i) == id){
				canAddValue = false;
			}
		}
		if (canAddValue) timersOnZero.add(id);

		ViewPropertyAnimator viewPropertyAnimator = timerListView.getChildAt(id).animate();
		viewPropertyAnimator.start();

		boolean timerRemoved = false;
		for (int i = 0; i < timers.size(); i++){
			if (timers.get(i).id == id){
				timers.remove(i);
				timerRemoved = true;
			}
		}
		if (!timerRemoved){
			throw new Exception("Timer not removed! watt");
		}
	}

	private void startMusic(){
		try{
			Uri uri = Uri.parse(Constants.ALARM_SOUND_DEFAULT);
			mediaPlayer = new MediaPlayer();

			AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
			systemVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
			audioManager.setStreamVolume(AudioManager.STREAM_ALARM, (50 / (100 / 7)), 0);

			mediaPlayer.setDataSource(getActivity(), uri);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
			mediaPlayer.setLooping(true);
			mediaPlayer.prepare();
			mediaPlayer.setVolume(0.5f, 0.5f);
			mediaPlayer.start();
			System.out.println();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	private void stopMusic(){
		if (mediaPlayer != null){
			if (mediaPlayer.isPlaying()){
				mediaPlayer.stop();
				AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
				audioManager.setStreamVolume(AudioManager.STREAM_ALARM, systemVolume, 0);
			}
		}
	}

	private void showNumberPickerDialog(final boolean newTimer, final int id, int oldHours, int oldMinutes, int oldSeconds){
		LayoutInflater layoutInflater = getLayoutInflater(savedInstanceState);
		View numberPicker = layoutInflater.inflate(R.layout.timer_numberpicker, null);

		hours = (NumberPicker) numberPicker.findViewById(R.id.timer_numberpicker_hours);
		minutes = (NumberPicker) numberPicker.findViewById(R.id.timer_numberpicker_minutes);
		seconds = (NumberPicker) numberPicker.findViewById(R.id.timer_numberpicker_seconds);

		hours.setMinValue(0);
		hours.setMaxValue(23);
		hours.setValue(oldHours);

		minutes.setMinValue(0);
		minutes.setMaxValue(59);
		minutes.setValue(oldMinutes);

		seconds.setMinValue(0);
		seconds.setMaxValue(59);
		seconds.setValue(oldSeconds);

		AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
		AlertDialog alertDialog;

		if (newTimer){
			builder.setTitle("Add timer with time:");
		} else {
			builder.setTitle("Change time:");
		}
		builder.setView(numberPicker);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (newTimer){
					addTimer(hours.getValue(), minutes.getValue(), seconds.getValue());
				} else {
					SharedPreferences.Editor editor = preferences.edit();
					editor.putInt(id + Constants.X_TIMER_HOURS, hours.getValue());
					editor.putInt(id + Constants.X_TIMER_MINUTES, minutes.getValue());
					editor.putInt(id + Constants.X_TIMER_SECONDS, seconds.getValue());
					editor.putString(id + Constants.X_TIMER_TIME, buildTimerString(hours.getValue(), minutes.getValue(), seconds.getValue()));
					editor.apply();
					onResume(); // l o l
				}
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		alertDialog = builder.create();
		alertDialog.show();
	}

	private void addTimer(int hours, int minutes, int seconds){
		SharedPreferences.Editor editor = preferences.edit();
		int amount = preferences.getInt(Constants.TIMER_AMOUNT, 0);
		amount++;
		editor.putInt(Constants.TIMER_AMOUNT, amount);
		int timerId = amount - 1;
		editor.putBoolean(timerId + Constants.X_TIMER_STATUS, false);
		editor.putInt(timerId + Constants.X_TIMER_HOURS, hours);
		editor.putInt(timerId + Constants.X_TIMER_MINUTES, minutes);
		editor.putInt(timerId + Constants.X_TIMER_SECONDS, seconds);
		editor.putString(timerId + Constants.X_TIMER_TIME, buildTimerString(hours, minutes, seconds));
		editor.apply();

		HashMap<String, String> newTimer = new HashMap<String, String>();
		newTimer.put(getString(R.string.timer_item_countdown_tag), preferences.getString(timerId + Constants.X_TIMER_TIME, Constants.TIMER_TIME_DEFAULT));
		newTimer.put(getString(R.string.timer_item_pause_tag), "");
		newTimer.put(getString(R.string.timer_item_stop_tag), "");
		addedListeners = false;
		timerListItems.add(newTimer);
		timerListAdapter.notifyDataSetChanged();
		//initListeners();
		//initPauseButton(timerId);
		//initStopButton(timerId);
	}

	private void removeTimer(int id){
		//prefs delete
		int newAmount = preferences.getInt(Constants.TIMER_AMOUNT, -1);
		if (newAmount == -1) return;
		newAmount--;
		SharedPreferences.Editor editor = preferences.edit();

		if(id != newAmount){
			for (int i = id; i < newAmount;i++) {
				editor.putBoolean(i + Constants.X_TIMER_STATUS, preferences.getBoolean((i + 1) + Constants.X_TIMER_STATUS, Constants.TIMER_STATUS_DEFAULT));
				editor.putInt(i + Constants.X_TIMER_HOURS, preferences.getInt((i + 1) + Constants.X_TIMER_HOURS, Constants.TIMER_HOURS_DEFAULT));
				editor.putInt(i + Constants.X_TIMER_MINUTES, preferences.getInt((i + 1) + Constants.X_TIMER_MINUTES, Constants.TIMER_MINUTES_DEFAULT));
				editor.putInt(i + Constants.X_TIMER_SECONDS, preferences.getInt((i + 1) + Constants.X_TIMER_SECONDS, Constants.TIMER_SECONDS_DEFAULT));
				editor.putString(i + Constants.X_TIMER_TIME, preferences.getString((i + 1) + Constants.X_TIMER_TIME, Constants.TIMER_TIME_DEFAULT));
				timerListItems.set(i, timerListItems.get(i + 1));
			}
			id = newAmount;
		}

		editor.remove(id + Constants.X_TIMER_STATUS);
		editor.remove(id + Constants.X_TIMER_TIME);
		editor.remove(id + Constants.X_TIMER_HOURS);
		editor.remove(id + Constants.X_TIMER_MINUTES);
		editor.remove(id + Constants.X_TIMER_SECONDS);
		editor.putInt(Constants.TIMER_AMOUNT, newAmount);
		editor.apply();
		addedListeners = false;
		timerListItems.remove(id);
		timerListAdapter.notifyDataSetChanged();
	}

	protected String buildTimerString(int hours, int minutes, int seconds){
		String time = hours + ":";
		if (minutes < 10){
			time += "0" + minutes + ":";
		} else {
			time += minutes + ":";
		}
		if (seconds < 10){
			time += "0" + seconds + "";
		} else {
			time += seconds + "";
		}
		return time;
	}

	private void deleteAllPrefs() {
		SharedPreferences.Editor editor = getActivity().getSharedPreferences(Constants.TIMER_PREFERENCES, Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.apply();
	}


}


/*


	/*private boolean startCountDown(final int id){
		int hours = preferences.getInt(id + Constants.X_TIMER_HOURS, Constants.TIMER_HOURS_DEFAULT);
		int minutes = preferences.getInt(id + Constants.X_TIMER_MINUTES, Constants.TIMER_MINUTES_DEFAULT);
		int seconds = preferences.getInt(id + Constants.X_TIMER_SECONDS, Constants.TIMER_SECONDS_DEFAULT);

		if (hours == 0 && minutes == 0 && seconds == 0){
			return false;
		}

		int amount = preferences.getInt(Constants.TIMER_AMOUNT, 0);
		countDownTickers = new int[amount];
		timers = new Timer[amount];
		timerTasks = new TimerTask[amount];

		countDownTickers[id] = seconds;
		countDownTickers[id] += (minutes * 60);
		countDownTickers[id] += (hours * 3600);

		final TextView countDownTextView = (TextView) timerListView.getChildAt(id).findViewById(R.id.timer_item_countdown_text);

		timers[id] = new Timer();
		timerTasks[id] = new TimerTask() {
			@Override
			public void run() {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						countDownTickers[id]--;
						int hours = countDownTickers[id] / 3600;
						int minutes = (countDownTickers[id] % 3600) / 60;
						int seconds = countDownTickers[id] % 60;
						countDownTextView.setText(buildTimerString(hours, minutes, seconds));
						if (countDownTickers[id] == 0){
							onTimerReachesZero(id);
						}
					}
				});
			}
		};

		timers[id].scheduleAtFixedRate(timerTasks[id], 1, 1000);

		return true;
	}

	private void stopCountDown(int id){
		if (mediaPlayer != null){
			if (mediaPlayer.isPlaying()){
				mediaPlayer.stop();
				AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
				audioManager.setStreamVolume(AudioManager.STREAM_ALARM, systemVolume, 0);
			}
		}
		timerTasks[id].cancel();
		timers[id].cancel();
		timers[id].purge();
	}

	private void onTimerReachesZero(int id){
		try{
			Uri uri = Uri.parse(Constants.ALARM_SOUND_DEFAULT);
			mediaPlayer = new MediaPlayer();

			AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
			systemVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
			audioManager.setStreamVolume(AudioManager.STREAM_ALARM, (50 / (100 / 7)), 0);

			mediaPlayer.setDataSource(getActivity(), uri);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
			mediaPlayer.setLooping(true);
			mediaPlayer.prepare();
			mediaPlayer.setVolume(0.5f, 0.5f);
			mediaPlayer.start();
			System.out.println();
		} catch (Exception e){
			e.printStackTrace();
		}
		timerTasks[id].cancel();
		timers[id].cancel();
		timers[id].purge();
		//timerListView.getChildAt(id).animate();
		//ImageButton imageButton = (ImageButton) timerListView.getChildAt(id).findViewById(R.id.timer_item_pause_button);
		//imageButton.callOnClick();
	}*/
