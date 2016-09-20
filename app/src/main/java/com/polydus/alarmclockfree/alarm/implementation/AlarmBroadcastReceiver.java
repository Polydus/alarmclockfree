package com.polydus.alarmclockfree.alarm.implementation;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.polydus.alarmclockfree.MainActivity;
import com.polydus.alarmclockfree.R;
import com.polydus.alarmclockfree.util.Constants;

import java.util.Calendar;
import java.util.Set;

/**
 * Created by leonard on 29-4-15.
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver{

	private static final String MONDAY = "monday";
	private static final String TUESDAY = "tuesday";
	private static final String WEDNESDAY = "wednesday";
	private static final String THURSDAY = "thursday";
	private static final String FRIDAY = "friday";
	private static final String SATURDAY = "saturday";
	private static final String SUNDAY = "sunday";

	@Override
	public void onReceive(Context context, Intent intent) {
		setAllAlarms(context);
	}

	public static void setAllAlarms(Context context){
		cancelAllAlarms(context);

		SharedPreferences prefs = context.getSharedPreferences(Constants.ALARM_PREFERENCES, Context.MODE_PRIVATE);
		int amount = prefs.getInt(Constants.ALARM_AMOUNT, 0);

		for (int i = 0; i < amount; i++){

			Log.v("polydus", "i in set: " + i + " of " + amount);


			if (prefs.getBoolean((Integer.toString(i) + Constants.X_ALARM_STATUS), false)){

				PendingIntent intent = createPendingIntent(context, prefs, i);

				Set<String> daysSet = prefs.getStringSet((Integer.toString(i) + Constants.X_ALARM_ACTIVE_DAYS), null);
				Object[] fromSet = daysSet.toArray();
				String[] daysArray = new String[fromSet.length];
				for (int j = 0; j < fromSet.length;j++){
					daysArray[j] = fromSet[j].toString();
				}

				Calendar calendar = Calendar.getInstance();
				calendar.setFirstDayOfWeek(Calendar.SUNDAY);
				String time = prefs.getString((Integer.toString(i) + Constants.X_ALARM_TIME), Constants.ALARM_TIME_DEFAULT);
				String[] times = time.split(":");
				int hour = Integer.parseInt(times[0]);
				int minutes = Integer.parseInt(times[1]);
				calendar.set(Calendar.HOUR_OF_DAY, hour);
				calendar.set(Calendar.MINUTE, minutes);
				calendar.set(Calendar.SECOND, 0);

				Calendar thisTime = Calendar.getInstance();
				thisTime.setFirstDayOfWeek(Calendar.SUNDAY);
				int currentDay = thisTime.get(Calendar.DAY_OF_WEEK);
				int currentHour = thisTime.get(Calendar.HOUR_OF_DAY);
				int currentMinute = thisTime.get(Calendar.MINUTE);
				boolean isAlarmSet = false;

				int alarmDay = calendar.get(Calendar.DAY_OF_WEEK);
				int alarmHour = calendar.get(Calendar.HOUR_OF_DAY);
				int alarmMinute = calendar.get(Calendar.MINUTE);

				//are there any alarms to schedule this week, or is the next one in the next week?

				//check if an alarm is coming up today
				for (int j = 0; j < daysArray.length; j++){
					if (currentDay == getDayId(daysArray[j])){
						if (hour > currentHour){
							calendar.set(Calendar.DAY_OF_WEEK, currentDay);
							setAlarm(context, calendar, intent, i);
							isAlarmSet = true;
							break;
						} else if (hour == currentHour && minutes > currentMinute){
							calendar.set(Calendar.DAY_OF_WEEK, currentDay);
							setAlarm(context, calendar, intent, i);
							isAlarmSet = true;
							break;
						}
					}
				}
				//check if an alarm is coming up this week, but not today
				//find the earliest alarm thats coming up
				if (!isAlarmSet) {
					int earliestDay = 9;
					for (int j = 0; j < daysArray.length; j++) {
						if (currentDay < getDayId(daysArray[j])) {
							if(getDayId(daysArray[j]) < earliestDay){
								earliestDay = getDayId(daysArray[j]);
							}
						}
					}
					if (earliestDay != 9){
						calendar.set(Calendar.DAY_OF_WEEK, earliestDay);
						setAlarm(context, calendar, intent, i);
						isAlarmSet = true;
					}
				}
				//else set it to the earliest day that is set, but next week
				if (!isAlarmSet) {
					int earliestDay = Calendar.SATURDAY;
					for (int j = 0; j < daysArray.length; j++) {
						if (earliestDay > getDayId(daysArray[j])) {
							earliestDay = getDayId(daysArray[j]);
							//if (earliestDay == Calendar.SUNDAY) break;
						}
					}
					calendar.set(Calendar.DAY_OF_WEEK, earliestDay);
					calendar.add(Calendar.WEEK_OF_YEAR, 1);
					setAlarm(context, calendar, intent, i);
					isAlarmSet = true;
				}

				if(!isAlarmSet){
					//System.out.println("wat");
				}


			}
		}

	}

	private static long lastTime;

	public static void cancelAllAlarms(Context context){
		lastTime = 0;
		SharedPreferences prefs = context.getSharedPreferences(Constants.ALARM_PREFERENCES, Context.MODE_PRIVATE);

		//int amount = 6;
		int amount = prefs.getInt(Constants.ALARM_AMOUNT, 0);

		for (int i = 0; i < amount; i++){

			//Log.v("polydus", "i in cancel: " + i + " of " + amount);

			PendingIntent intent = createPendingIntent(context, prefs, i);

			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(intent);
		}
		removeNotification(context);
	}

	private static void setAlarm(Context context, Calendar calendar, PendingIntent intent, int i){
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		//System.out.println(calendar.getTimeInMillis());
		//Log.v("version", Build.VERSION.SDK_INT + "");
		if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT){
			alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intent);
		} else {
			alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intent);
		}
		addNotification(context, calendar, i);
	}

	private static void addNotification(Context context, Calendar calendar, int i){
		long now = Calendar.getInstance().getTimeInMillis();
		long difference = calendar.getTimeInMillis() - now;
		if (lastTime == 0){
			lastTime = difference;
		} else {
			if (lastTime < difference){
				return;
			}
		}

		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);
		String time;

		if (hours < 10){
			time = "0" + hours;
		} else {
			time = "" + hours;
		}
		if (minutes < 10){
			time += ":0" + minutes;
		} else {
			time += ":" + minutes;
		}
		String day = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, context.getResources().getConfiguration().locale);
		int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		String month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, context.getResources().getConfiguration().locale);
		int year = calendar.get(Calendar.YEAR);

		String message = "Next alarm at " + time + ", " + day + " " + dayOfMonth + " " + month + " " + year;

			NotificationCompat.Builder builder =
				new NotificationCompat.Builder(context)
						.setSmallIcon(R.drawable.ic_access_alarm_white_48dp)
						.setContentTitle(context.getString(R.string.app_name))
						.setContentText(message);

		Intent intent = new Intent(context, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(intent);
		PendingIntent pendingIntent =
				stackBuilder.getPendingIntent(
						0,
						PendingIntent.FLAG_UPDATE_CURRENT
				);
		builder.setContentIntent(pendingIntent);

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(1, builder.build());
	}

	private static void removeNotification(Context context){
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
	}

	private static int getDayId(String day){
		switch (day){
			case MONDAY:
				return Calendar.MONDAY;
			case TUESDAY:
				return Calendar.TUESDAY;
			case WEDNESDAY:
				return Calendar.WEDNESDAY;
			case THURSDAY:
				return Calendar.THURSDAY;
			case FRIDAY:
				return Calendar.FRIDAY;
			case SATURDAY:
				return Calendar.SATURDAY;
			case SUNDAY:
				return Calendar.SUNDAY;
		}
		return Integer.parseInt(null); // l o l
	}

	private static PendingIntent createPendingIntent(Context context, SharedPreferences prefs, int i){
		Intent tempIntent = new Intent(context, AlarmService.class);

		tempIntent.putExtra(Constants.X_ALARM_NAME, prefs.getString((Integer.toString(i) + Constants.X_ALARM_NAME), Constants.ALARM_NAME_DEFAULT));
		tempIntent.putExtra(Constants.X_ALARM_TIME, prefs.getString((Integer.toString(i) + Constants.X_ALARM_TIME), Constants.ALARM_TIME_DEFAULT));
		tempIntent.putExtra(Constants.X_ALARM_ID, i);
		Set<String> daysSet = prefs.getStringSet((Integer.toString(i) + Constants.X_ALARM_ACTIVE_DAYS), null);
		Object[] fromSet = daysSet.toArray();
		String[] daysArray = new String[fromSet.length];
		for (int j = 0; j < fromSet.length;j++){
			daysArray[j] = fromSet[j].toString();
		}
		tempIntent.putExtra(Constants.X_ALARM_ACTIVE_DAYS, daysArray);

		tempIntent.putExtra(Constants.X_ALARM_VIBRATION, prefs.getBoolean((Integer.toString(i) + Constants.X_ALARM_VIBRATION), Constants.ALARM_VIBRATION_DEFAULT));
		tempIntent.putExtra(Constants.X_ALARM_SOURCE, prefs.getString((Integer.toString(i) + Constants.X_ALARM_SOURCE), Constants.ALARM_SOURCE_DEFAULT));
		tempIntent.putExtra(Constants.X_ALARM_SOUND, prefs.getString((Integer.toString(i) + Constants.X_ALARM_SOUND), Constants.ALARM_SOUND_DEFAULT));
		tempIntent.putExtra(Constants.X_ALARM_CUSTOM_SOUND, prefs.getString((Integer.toString(i) + Constants.X_ALARM_CUSTOM_SOUND), Constants.ALARM_CUSTOM_SOUND_DEFAULT));
		tempIntent.putExtra(Constants.X_ALARM_VOLUME, prefs.getInt((Integer.toString(i) + Constants.X_ALARM_VOLUME), Constants.ALARM_VOLUME_DEFAULT));
		tempIntent.putExtra(Constants.X_ALARM_SNOOZE_ENABLED, prefs.getBoolean((Integer.toString(i) + Constants.X_ALARM_SNOOZE_ENABLED), Constants.ALARM_SNOOZE_ENABLED_DEFAULT));
		tempIntent.putExtra(Constants.X_ALARM_SNOOZE_TIME, prefs.getInt((Integer.toString(i) + Constants.X_ALARM_SNOOZE_TIME), Constants.ALARM_SNOOZE_TIME_DEFAULT));

		//tempIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

		return PendingIntent.getService(context, i, tempIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

}

