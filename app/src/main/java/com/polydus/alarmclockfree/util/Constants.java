package com.polydus.alarmclockfree.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by leonard on 29-4-15.
 */
public final class Constants {

	//prefs name
	public static final String ALARM_PREFERENCES = "ALARM_PREFERENCES";
	public static final String TIMER_PREFERENCES = "TIMER_PREFERENCES";

	public static final String ALARM_LAUNCH_PREFERENCES = "ALARM_LAUNCH_PREFERENCES";

	//ALARM_PREFERENCES keys
	public static final String ALARM_AMOUNT = "ALARM_AMOUNT";
	public static final String TIMER_AMOUNT = "TIMER_AMOUNT";

	/*alarm specific keys
	write ALARM_ID + X_ALARM_TIME
	or any other vars like this below.
	*/

	public static final String X_ALARM_NAME = "_ALARM_NAME";
	public static final String X_ALARM_TIME = "_ALARM_TIME";
	public static final String X_ALARM_ACTIVE_DAYS = "_ALARM_ACTIVE_DAYS";
	public static final String X_ALARM_STATUS = "_ALARM_STATUS";
	public static final String X_ALARM_VIBRATION = "_ALARM_VIBRATION";
	public static final String X_ALARM_SOURCE= "_ALARM_SOURCE";
	public static final String X_ALARM_SOUND = "_ALARM_SOUND";
	public static final String X_ALARM_CUSTOM_SOUND = "_ALARM_CUSTOM_SOUND";
	public static final String X_ALARM_VOLUME = "_ALARM_VOLUME";
	public static final String X_ALARM_SNOOZE_ENABLED = "_ALARM_SNOOZE_ENABLED";
	public static final String X_ALARM_SNOOZE_TIME = "_ALARM_SNOOZE_TIME";

	public static final String X_TIMER_NAME = "_TIMER_NAME";
	public static final String X_TIMER_TIME = "_TIMER_TIME";
	public static final String X_TIMER_HOURS = "_TIMER_HOURS";
	public static final String X_TIMER_MINUTES = "_TIMER_MINUTES";
	public static final String X_TIMER_SECONDS = "_TIMER_SECONDS";
	public static final String X_TIMER_STATUS = "_TIMER_STATUS";

	//activity keys

	public static final String ALARM_LAUNCH_ALLOWED = "ALARM_LAUNCH_ALLOWED";

	//summary keys

	//time summary handled in class
	public static final String X_ALARM_NAME_SUMMARY = "_ALARM_NAME_SUMMARY";
	public static final String X_ALARM_ACTIVE_DAYS_SUMMARY = "_ALARM_ACTIVE_DAYS_SUMMARY";
	public static final String X_ALARM_VIBRATION_SUMMARY = "_ALARM_VIBRATION_SUMMARY";
	public static final String X_ALARM_SOURCE_SUMMARY = "_ALARM_SOURCE_SUMMARY";
	public static final String X_ALARM_SOUND_SUMMARY = "_ALARM_SOUND_SUMMARY";
	public static final String X_ALARM_CUSTOM_SOUND_SUMMARY = "_ALARM_CUSTOM_SOUND_SUMMARY";
	public static final String X_ALARM_VOLUME_SUMMARY = "_ALARM_VOLUME_SUMMARY";
	public static final String X_ALARM_SNOOZE_ENABLED_SUMMARY = "_ALARM_SNOOZE_ENABLED_SUMMARY";
	public static final String X_ALARM_SNOOZE_TIME_SUMMARY = "_ALARM_SNOOZE_TIME_SUMMARY";

	//used only in Receiver
	public static final String X_ALARM_ID = "_ALARM_ID";

	//used in preferences
	public static final boolean ALARM_STATUS_DEFAULT = true;
	public static final String ALARM_NAME_DEFAULT = "Alarm 1";
	public static final String ALARM_TIME_DEFAULT = "08:00";
	public static final Set<String> ALARM_ACTIVE_DAYS_DEFAULT = new HashSet<String>() {{
		add("sunday");
		add("monday");
		add("tuesday");
		add("wednesday");
		add("thursday");
		add("friday");
		add("saturday");
	}};
	public static final String ALARM_SOURCE_DEFAULT = "ringtone";
	public static final String ALARM_SOUND_DEFAULT = "content://settings/system/ringtone";
	public static final String ALARM_CUSTOM_SOUND_DEFAULT = "";
	public static final boolean ALARM_VIBRATION_DEFAULT = true;
	public static final int ALARM_VOLUME_DEFAULT = 50;
	public static final boolean ALARM_SNOOZE_ENABLED_DEFAULT = true;
	public static final int ALARM_SNOOZE_TIME_DEFAULT = 5;

	public static final String TIMER_NAME_DEFAULT = "That timer's name?";
	public static final String TIMER_TIME_DEFAULT = "0:05:00";
	public static final boolean TIMER_STATUS_DEFAULT = false;
	public static final int TIMER_HOURS_DEFAULT = 0;
	public static final int TIMER_MINUTES_DEFAULT = 5;
	public static final int TIMER_SECONDS_DEFAULT = 0;

	public static final boolean ALARM_LAUNCH_ALLOWED_DEFAULT = true;
}
