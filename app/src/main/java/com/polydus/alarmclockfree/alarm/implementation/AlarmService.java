package com.polydus.alarmclockfree.alarm.implementation;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.polydus.alarmclockfree.alarm.onwakelock.AlarmWakeLockActivity;
import com.polydus.alarmclockfree.util.Constants;

/**
 * Created by leonard on 29-4-15.
 */
public class AlarmService extends Service{

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (intent != null){
			SharedPreferences preferences = getSharedPreferences(Constants.ALARM_LAUNCH_PREFERENCES, MODE_PRIVATE);
			SharedPreferences.Editor editor = preferences.edit();
			editor.putBoolean(Constants.ALARM_LAUNCH_ALLOWED, true);
			editor.apply();

			Intent startActivityIntent = new Intent(getBaseContext(), AlarmWakeLockActivity.class);
			startActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			startActivityIntent.putExtras(intent);

			getApplication().startActivity(startActivityIntent);

			AlarmBroadcastReceiver.setAllAlarms(this);
		}

		return super.onStartCommand(intent, flags, startId);

	}
}
