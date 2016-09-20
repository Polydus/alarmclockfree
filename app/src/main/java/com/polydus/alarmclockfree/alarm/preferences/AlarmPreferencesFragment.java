package com.polydus.alarmclockfree.alarm.preferences;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.MediaStore;

import com.polydus.alarmclockfree.R;
import com.polydus.alarmclockfree.alarm.implementation.AlarmBroadcastReceiver;
import com.polydus.alarmclockfree.alarm.preferences.custom.SoundPreference;
import com.polydus.alarmclockfree.util.Constants;

import java.util.Set;

public class AlarmPreferencesFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener{

	private SharedPreferences sharedPreferences;
	private SharedPreferences.OnSharedPreferenceChangeListener listener;

	private Preference customSoundPreference;
	private boolean returningFromCustomSoundPreference = false;

	private int alarmId;
	private boolean newAlarm;
	private boolean defaultsSetInPreferences = false;

	private final int CUSTOM_SOUND_REQUESTCODE = 12440;
	public static final String CUSTOM_SOUND_URI_FLAGS = "CUSTOM_SOUND_URI_FLAGS";

	public AlarmPreferencesFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(Constants.ALARM_PREFERENCES);

		init();
		findAlarmStatus();
		setDefaultsIntoPermanentPreferences();
		setPreferenceChangeListener();

		addPreferencesFromResource(R.xml.alarm_customize_preferences);
		setSummaries();
		customSoundPreference = findPreference(Constants.X_ALARM_CUSTOM_SOUND);
		customSoundPreference.setOnPreferenceClickListener(this);
	}

	//more sillyness
	@Override
	public void onPause() {

		if(!defaultsSetInPreferences){
			init();
			findAlarmStatus();
			setDefaultsIntoPermanentPreferences();
		}

		if (SoundPreference.returningFromSoundPreference || returningFromCustomSoundPreference){
			SoundPreference.returningFromSoundPreference = false;
			returningFromCustomSoundPreference = false;
		} else {
			sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
			if (customSoundPreference != null) customSoundPreference.setOnPreferenceClickListener(null);
			AlarmBroadcastReceiver.setAllAlarms(getActivity().getApplicationContext());
		}

		super.onPause();
	}

	private void init(){
		sharedPreferences = getActivity().getSharedPreferences(Constants.ALARM_PREFERENCES, Context.MODE_PRIVATE);
		//customSoundPreference = findPreference(Constants.X_ALARM_CUSTOM_SOUND);
	}

	private void findAlarmStatus(){
		newAlarm = getActivity().getIntent().getBooleanExtra("new_alarm", true);

		//set alarm id properly
		if (newAlarm){
			alarmId = sharedPreferences.getInt(Constants.ALARM_AMOUNT, -1);
			alarmId--; //bc the actual id is amount -1, also amount has been incremented before
		} else {
			alarmId = getActivity().getIntent().getIntExtra("alarm_id", -1);
		}
	}

	private void setDefaultsIntoPermanentPreferences(){
		SharedPreferences.Editor editor = sharedPreferences.edit();
		if (!newAlarm) {
			//add all old settings in the preferences used by the temp preferences!
			editor.putString(Constants.X_ALARM_NAME, sharedPreferences.getString(alarmId + Constants.X_ALARM_NAME, Constants.ALARM_NAME_DEFAULT));
			editor.putString(Constants.X_ALARM_TIME, sharedPreferences.getString(alarmId + Constants.X_ALARM_TIME, Constants.ALARM_TIME_DEFAULT));
			editor.putStringSet(Constants.X_ALARM_ACTIVE_DAYS, sharedPreferences.getStringSet(alarmId + Constants.X_ALARM_ACTIVE_DAYS, Constants.ALARM_ACTIVE_DAYS_DEFAULT));
			//editor.putBoolean(Constants.X_ALARM_STATUS, sharedPreferences.getBoolean(alarmId + Constants.X_ALARM_STATUS, Constants.ALARM_STATUS_DEFAULT));
			editor.putBoolean(Constants.X_ALARM_VIBRATION, sharedPreferences.getBoolean(alarmId + Constants.X_ALARM_VIBRATION, Constants.ALARM_VIBRATION_DEFAULT));
			editor.putString(Constants.X_ALARM_SOURCE, sharedPreferences.getString(alarmId + Constants.X_ALARM_SOURCE, Constants.ALARM_SOURCE_DEFAULT));
			editor.putString(Constants.X_ALARM_SOUND, sharedPreferences.getString(alarmId + Constants.X_ALARM_SOUND, Constants.ALARM_SOUND_DEFAULT));
			editor.putString(Constants.X_ALARM_CUSTOM_SOUND, sharedPreferences.getString(alarmId + Constants.X_ALARM_CUSTOM_SOUND, Constants.ALARM_CUSTOM_SOUND_DEFAULT));
			editor.putInt(Constants.X_ALARM_VOLUME, sharedPreferences.getInt(alarmId + Constants.X_ALARM_VOLUME, Constants.ALARM_VOLUME_DEFAULT));
			editor.putBoolean(Constants.X_ALARM_SNOOZE_ENABLED, sharedPreferences.getBoolean(alarmId + Constants.X_ALARM_SNOOZE_ENABLED, Constants.ALARM_SNOOZE_ENABLED_DEFAULT));
			editor.putInt(Constants.X_ALARM_SNOOZE_TIME, sharedPreferences.getInt(alarmId + Constants.X_ALARM_SNOOZE_TIME, Constants.ALARM_SNOOZE_TIME_DEFAULT));
		} else {
			editor.putBoolean(alarmId + Constants.X_ALARM_STATUS, Constants.ALARM_STATUS_DEFAULT);
			//because this pref is set w/ intent
			String name = "Alarm " + (alarmId + 1);
			editor.putString(Constants.X_ALARM_NAME, name);
			editor.putString(alarmId + Constants.X_ALARM_NAME, name);

			editor.putString(alarmId + Constants.X_ALARM_CUSTOM_SOUND, Constants.ALARM_CUSTOM_SOUND_DEFAULT);
			editor.putString(alarmId + Constants.X_ALARM_ACTIVE_DAYS_SUMMARY, getActivity().getString(R.string.alarm_preference_active_days_summary));
		}
		editor.apply();
		defaultsSetInPreferences = true;
	}

	private void setPreferenceChangeListener(){
		listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				SharedPreferences.Editor editor = sharedPreferences.edit();
				switch (key){
					case Constants.X_ALARM_NAME:
						editor.putString(alarmId + Constants.X_ALARM_NAME, sharedPreferences.getString(Constants.X_ALARM_NAME, Constants.ALARM_NAME_DEFAULT));
						break;
					case Constants.X_ALARM_TIME:
						editor.putString(alarmId + Constants.X_ALARM_TIME, sharedPreferences.getString(Constants.X_ALARM_TIME, Constants.ALARM_TIME_DEFAULT));
						break;
					case Constants.X_ALARM_ACTIVE_DAYS:
						editor.putStringSet(alarmId + Constants.X_ALARM_ACTIVE_DAYS, sharedPreferences.getStringSet(Constants.X_ALARM_ACTIVE_DAYS, Constants.ALARM_ACTIVE_DAYS_DEFAULT));
						//setSummary(key);
						break;
					case Constants.X_ALARM_STATUS:
						editor.putBoolean(alarmId + Constants.X_ALARM_STATUS, sharedPreferences.getBoolean(Constants.X_ALARM_STATUS, Constants.ALARM_STATUS_DEFAULT));
						break;
					case Constants.X_ALARM_VIBRATION:
						editor.putBoolean(alarmId + Constants.X_ALARM_VIBRATION, sharedPreferences.getBoolean(Constants.X_ALARM_VIBRATION, Constants.ALARM_VIBRATION_DEFAULT));
						//setSummary(key);
						break;
					case Constants.X_ALARM_SOURCE:
						editor.putString(alarmId + Constants.X_ALARM_SOURCE, sharedPreferences.getString(Constants.X_ALARM_SOURCE, Constants.ALARM_SOURCE_DEFAULT));
						break;
					case Constants.X_ALARM_SOUND:
						editor.putString(alarmId + Constants.X_ALARM_SOUND, sharedPreferences.getString(Constants.X_ALARM_SOUND, Constants.ALARM_SOUND_DEFAULT));
						break;
					/*case Constants.X_ALARM_CUSTOM_SOUND:
						editor.putString(alarmId + Constants.X_ALARM_CUSTOM_SOUND, sharedPreferences.getString(Constants.X_ALARM_CUSTOM_SOUND, Constants.ALARM_CUSTOM_SOUND_DEFAULT));
						break;*/
					case Constants.X_ALARM_VOLUME:
						editor.putInt(alarmId + Constants.X_ALARM_VOLUME, sharedPreferences.getInt(Constants.X_ALARM_VOLUME, Constants.ALARM_VOLUME_DEFAULT));
						break;
					case Constants.X_ALARM_SNOOZE_ENABLED:
						editor.putBoolean(alarmId + Constants.X_ALARM_SNOOZE_ENABLED, sharedPreferences.getBoolean(Constants.X_ALARM_SNOOZE_ENABLED, Constants.ALARM_SNOOZE_ENABLED_DEFAULT));
						break;
					case Constants.X_ALARM_SNOOZE_TIME:
						editor.putInt(alarmId + Constants.X_ALARM_SNOOZE_TIME, sharedPreferences.getInt(Constants.X_ALARM_SNOOZE_TIME, Constants.ALARM_SNOOZE_TIME_DEFAULT));
						break;
				}
				setSummary(key);
				editor.apply();
			}
		};

		sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		//only used for custom sound
		if (preference.getKey().equals(Constants.X_ALARM_CUSTOM_SOUND)){
			returningFromCustomSoundPreference = true;

			Intent intent = new Intent();
			intent.setType("audio/*");

			if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT){
				intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
			} else {
				intent.setAction(Intent.ACTION_GET_CONTENT);
			}

			startActivityForResult(Intent.createChooser(intent, "Choose sound"), CUSTOM_SOUND_REQUESTCODE);

			return true;
		}
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode != Activity.RESULT_OK || data == null) return;

		if(requestCode == CUSTOM_SOUND_REQUESTCODE){

			SharedPreferences.Editor editor = sharedPreferences.edit();
			Uri uri = data.getData();
			String result = uri.toString();

			if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT){
				getActivity().grantUriPermission(getActivity().getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
				final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION);
				getActivity().getContentResolver().takePersistableUriPermission(uri, takeFlags);
				editor.putInt(CUSTOM_SOUND_URI_FLAGS, takeFlags);
			}

			editor.putString(Constants.X_ALARM_CUSTOM_SOUND, result);
			editor.putString(alarmId + Constants.X_ALARM_CUSTOM_SOUND, result);
			editor.apply();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}


	private void setSummaries(){
		if (!newAlarm){
			setSummary(Constants.X_ALARM_NAME);
			setSummary(Constants.X_ALARM_ACTIVE_DAYS);
			setSummary(Constants.X_ALARM_VIBRATION);
			setSummary(Constants.X_ALARM_SOURCE);
			setSummary(Constants.X_ALARM_SOUND);
			setSummary(Constants.X_ALARM_CUSTOM_SOUND);
			setSummary(Constants.X_ALARM_VOLUME);
			setSummary(Constants.X_ALARM_SNOOZE_ENABLED);
			setSummary(Constants.X_ALARM_SNOOZE_TIME);
		} else {
			setSummary(Constants.X_ALARM_NAME);
			setSummary(Constants.X_ALARM_SOUND);
		}
	}

	private void setSummary(String key) {
		Preference preference = findPreference(key);
		if (preference == null) return;

		switch (key){
			case Constants.X_ALARM_NAME:
				preference.setSummary(sharedPreferences.getString(key, Constants.ALARM_NAME_DEFAULT));
				break;
			case Constants.X_ALARM_ACTIVE_DAYS:
				setActiveDaysSummary(preference, Constants.X_ALARM_ACTIVE_DAYS_SUMMARY);
				break;
			case Constants.X_ALARM_VIBRATION:
				if (sharedPreferences.getBoolean(key, Constants.ALARM_VIBRATION_DEFAULT)){
					preference.setSummary("Enabled");
				} else {
					preference.setSummary("Disabled");
				}
				break;
			case Constants.X_ALARM_SOURCE:
				String summary = sharedPreferences.getString(key, Constants.ALARM_SOURCE_DEFAULT);
				if (summary == null) return;
				if (summary.equals(getActivity().getString(R.string.alarm_ringtone_source_values_ringtone))){
					preference.setSummary(getActivity().getString(R.string.alarm_ringtone_source_entries_ringtone));
				} else if (summary.equals(getActivity().getString(R.string.alarm_ringtone_source_values_music))){
					preference.setSummary(getActivity().getString(R.string.alarm_ringtone_source_entries_music));
				} else {
					preference.setSummary(getActivity().getString(R.string.alarm_ringtone_source_entries_silent));
				}
				break;
			case Constants.X_ALARM_SOUND:
				String path = sharedPreferences.getString(key, Constants.ALARM_SOUND_DEFAULT);
				if (path == null) return;
				//if (path.equals(Constants.ALARM_SOUND_DEFAULT)) return;
				Uri uri = Uri.parse(path);
				Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), uri);
				String title = ringtone.getTitle(getActivity());
				preference.setSummary(title);
				break;
			case Constants.X_ALARM_CUSTOM_SOUND:
				path = sharedPreferences.getString(key, Constants.ALARM_CUSTOM_SOUND_DEFAULT);
				if (path == null) return;
				if (path.equals(Constants.ALARM_CUSTOM_SOUND_DEFAULT)) return;
				if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
					uri = Uri.parse(path);
					MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
					mediaMetadataRetriever.setDataSource(getActivity(), uri);
					String artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
					title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
					if (artist == null && title == null){
						summary = "Enabled";
					} else if (artist == null){
						summary = title;
					} else if (title == null){
						summary = artist;
					} else {
						summary = artist + " - " + title;
					}
					preference.setSummary(summary);
				} else {
					preference.setSummary(getSongName(path, true));
				}
				break;
			case Constants.X_ALARM_VOLUME:
				preference.setSummary(sharedPreferences.getInt(key, Constants.ALARM_VOLUME_DEFAULT) + "%");
				break;
			case Constants.X_ALARM_SNOOZE_ENABLED:
				if (sharedPreferences.getBoolean(key, Constants.ALARM_SNOOZE_ENABLED_DEFAULT)){
					preference.setSummary("Enabled");
				} else {
					preference.setSummary("Disabled");
				}
				break;
			case Constants.X_ALARM_SNOOZE_TIME:
				preference.setSummary(sharedPreferences.getInt(key, Constants.ALARM_SNOOZE_TIME_DEFAULT) + " minutes");
				break;
		}

	}

	private String getSongName(String path, boolean returnArtistName){
		Uri uri = Uri.parse(path);
		String result = "null";

		try{
			result = querySong(uri, returnArtistName);
		} catch (Exception e){
			e.printStackTrace();
			result = "";
				/*
				if (e instanceof SecurityException) {
					if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
						getActivity().grantUriPermission(getActivity().getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
						final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
						getActivity().getContentResolver().takePersistableUriPermission(uri, takeFlags);
						result = querySong(uri, returnArtistName);
					}
				}
				 */
			}
		return result;
	}

	private String querySong(Uri uri, boolean returnArtistName){
		String[] columns;
		String result;

		if (returnArtistName){
			columns = new String[2];
			columns[1] = MediaStore.Audio.Media.ARTIST;
		} else {
			columns = new String[1];
		}
		columns[0] = MediaStore.Audio.Media.TITLE;

		Cursor cursor = getActivity().getContentResolver().query(
				uri,
				columns,
				null,
				null,
				null);

		cursor.moveToFirst();

		if (returnArtistName){
			result = cursor.getString(cursor.getColumnIndexOrThrow(columns[1])) + " - " +
					cursor.getString(cursor.getColumnIndexOrThrow(columns[0]));
		} else {
			result = cursor.getString(cursor.getColumnIndexOrThrow(columns[0]));
		}

		cursor.close();
		return result;
	}

	private void setActiveDaysSummary(Preference preference, String key){
		Set<String> daysSet = sharedPreferences.getStringSet(Constants.X_ALARM_ACTIVE_DAYS, null);
		String[] days = new String[daysSet.size()];
		daysSet.toArray(days);
		String result;
		if (days.length == 0) {
			result = "No days set!";
		} else if (days.length == 7) {
			result = getActivity().getString(R.string.alarm_preference_active_days_summary);
		} else {
			result = getActivity().getString(R.string.alarm_preference_active_days_summary);
			String oppositeString = result;

			for (int i = 0; i < days.length; i++) {
				int index = oppositeString.indexOf((days[i].substring(0, 1).toUpperCase()) + days[i].substring(1, 2));
				try {
					oppositeString = oppositeString.substring(0, index) + oppositeString.substring(index + 3);
				} catch (StringIndexOutOfBoundsException e) {
					oppositeString = oppositeString.substring(0, index) + oppositeString.substring(index + 2);
				}
			}
			for (int i = 0; i < (7 - days.length); i++) {
				int index = result.indexOf(oppositeString.substring(i * 3, (i * 3) + 2));

				try {
					result = result.substring(0, index) + result.substring(index + 3);
				} catch (StringIndexOutOfBoundsException e) {
					result = result.substring(0, index) + result.substring(index + 2);
				}
			}
		}
		preference.setSummary(result);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(alarmId + key, result);
		editor.apply();
	}


}
