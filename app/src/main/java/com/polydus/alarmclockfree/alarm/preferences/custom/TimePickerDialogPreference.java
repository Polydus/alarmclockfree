package com.polydus.alarmclockfree.alarm.preferences.custom;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import com.polydus.alarmclockfree.util.Constants;

/**
 * Created by leonard on 25-4-15.
 */
public class TimePickerDialogPreference extends DialogPreference{

	private TimePicker timePicker;

	private int hour;
	private int minutes;

	private int alarmId;
	private boolean restorePersistedValue;

	public TimePickerDialogPreference(Context context) {
		super(context, null);
	}

	public TimePickerDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setPositiveButtonText("Set");
		setNegativeButtonText("Cancel");
	}

	@Override
	protected View onCreateDialogView() {
		timePicker = new TimePicker(getContext());
		timePicker.setIs24HourView(true);

		return timePicker;
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);

		SharedPreferences preferences = getContext().getSharedPreferences(Constants.ALARM_PREFERENCES, Context.MODE_PRIVATE);
		String time;

		if(preferences.getString(alarmId + Constants.X_ALARM_TIME, "NOTSET").equals("NOTSET")){
			time = Constants.ALARM_TIME_DEFAULT;
		} else {
			time = getPersistedString(Constants.ALARM_TIME_DEFAULT);
		}

		hour = getHour(time);
		minutes = getMinutes(time);
		timePicker.setCurrentHour(hour);
		timePicker.setCurrentMinute(minutes);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		//super.onSetInitialValue(restorePersistedValue, defaultValue);

		//setKey();

		String time;

		if(restorePersistedValue){
			/*persistString(defaultValue.toString());
			notifyDependencyChange(shouldDisableDependents()); // o k
			notifyChanged();*/
			time = getPersistedString(Constants.ALARM_TIME_DEFAULT);
		} else {
			time = defaultValue.toString();
		}

		hour = getHour(time);
		minutes = getMinutes(time);
		updateTime(getTimeByInts(hour, minutes));

	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		String result = a.getString(index);

		return result;
	}

	private void setKey(){
		SharedPreferences preferences = getContext().getSharedPreferences(Constants.ALARM_PREFERENCES, Context.MODE_PRIVATE);
		Activity activity = (Activity) getContext();
		boolean newAlarm = activity.getIntent().getBooleanExtra("new_alarm", true);

		if(newAlarm){
			alarmId = preferences.getInt(Constants.ALARM_AMOUNT, -1);
			alarmId--; //bc the actual id is amount -1, also amount has been incremented before
			if (alarmId == -1) this.onDialogClosed(false); //another failsafe
			//restorePersistedValue = false;
		} else {
			//restorePersistedValue = true;
			alarmId = activity.getIntent().getIntExtra("alarm_id", -1);
		}
		if(preferences.getString(alarmId + Constants.X_ALARM_TIME, "NOTSET").equals("NOTSET")){
			restorePersistedValue = false;
		} else {
			restorePersistedValue = true;
		}

		String key = Integer.toString(alarmId) + Constants.X_ALARM_TIME;
		setKey(key);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if(positiveResult){

			String selection = getTimeByInts(timePicker.getCurrentHour(), timePicker.getCurrentMinute());

			if (callChangeListener(selection)){ //make change persistent
				updateTime(selection);
			}

		}
	}

	private void updateTime(String data){
		persistString(data);
		notifyDependencyChange(shouldDisableDependents());
		notifyChanged();
		setSummary(data);
	}

	private int getHour(String time){
		String times[] = time.split(":");
		return Integer.parseInt(times[0]);
	}

	private int getMinutes(String time){
		String times[] = time.split(":");
		return Integer.parseInt(times[1]);
	}

	private String getTimeByInts(int hour, int minutes){
		String result;
		if(hour < 10){
			result =  "0" + Integer.toString(hour);
		} else {
			result = Integer.toString(hour);
		}
		if (minutes < 10){
			result +=  ":0" + Integer.toString(minutes);
		} else {
			result +=  ":" + Integer.toString(minutes);
		}
		return result;
	}
}
