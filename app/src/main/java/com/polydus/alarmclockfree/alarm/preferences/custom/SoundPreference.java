package com.polydus.alarmclockfree.alarm.preferences.custom;

import android.content.Context;
import android.content.Intent;
import android.preference.RingtonePreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by leonard on 2-5-15.
 */
public class SoundPreference extends RingtonePreference{

	public static boolean returningFromSoundPreference = false;

	public SoundPreference(Context context) {
		super(context, null);
	}

	public SoundPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		return super.onCreateView(parent);
	}

	@Override
	protected void onBindView(View view) {

		super.onBindView(view);
	}

	@Override
	protected void onClick() {
		//before onPause in fragment
		returningFromSoundPreference = true;
		super.onClick();
	}



	@Override
	public OnPreferenceChangeListener getOnPreferenceChangeListener() {
		return super.getOnPreferenceChangeListener();
	}

	@Override
	public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
		//returningFromSoundPreference = true;
		super.setOnPreferenceChangeListener(onPreferenceChangeListener);
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		//returningFromSoundPreference = true;
		return super.onActivityResult(requestCode, resultCode, data);
	}
}
