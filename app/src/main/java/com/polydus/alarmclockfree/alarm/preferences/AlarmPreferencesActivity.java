package com.polydus.alarmclockfree.alarm.preferences;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.polydus.alarmclockfree.util.Constants;


public class AlarmPreferencesActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int alarmId = getIntent().getIntExtra("alarm_id", -1);
		boolean newAlarm = getIntent().getBooleanExtra("new_alarm", true);

		//if -1 is the result, something is wrong & therefore dont edit anything & return to the list
		//else add a default alarm to the list.
		if (alarmId == -1){
			this.finish();
		} else if (newAlarm){
			SharedPreferences preferences = getSharedPreferences(Constants.ALARM_PREFERENCES, MODE_PRIVATE);
			SharedPreferences.Editor editor = preferences.edit();
			editor.putInt(Constants.ALARM_AMOUNT, alarmId + 1);//+1 bc a new alarm is made.
			//editor.putBoolean(Integer.toString(alarmId) + Constants.X_ALARM_STATUS, true);
			editor.apply();
		}

		getFragmentManager().beginTransaction().replace(
				android.R.id.content,
				new AlarmPreferencesFragment()).commit();
	}

}
