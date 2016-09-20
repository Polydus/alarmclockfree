package com.polydus.alarmclockfree.alarm.preferences.custom;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.polydus.alarmclockfree.R;
import com.polydus.alarmclockfree.util.Constants;

/**
 * Created by leonard on 2-5-15.
 */
public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener{

	private SeekBar seekBar;
	private TextView textView;

	//attrs
	private int minimumValue;
	private int maximumValue;
	private int stepSize;
	private String units;

	private int result;

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		minimumValue = 0;
		maximumValue = 100;
		stepSize = 1;
		units = "%";
		result = Constants.ALARM_VOLUME_DEFAULT;
	}

	@Override
	protected View onCreateDialogView() {
		LayoutInflater layoutInflater = LayoutInflater.from(getContext());
		View view = layoutInflater.inflate(R.layout.seekbar_preference, null);

		seekBar = (SeekBar)view.findViewById(R.id.seekbar_preference_seekbar);
		textView = (TextView)view.findViewById(R.id.seekbar_preference_textview);

		result = getPersistedInt(Constants.ALARM_VOLUME_DEFAULT);
		if (result < 0) result = 0;

		seekBar.setOnSeekBarChangeListener(this);
		seekBar.setKeyProgressIncrement(stepSize);
		seekBar.setMax(maximumValue - minimumValue);
		seekBar.setProgress(result);

		textView.setText(result + units);

		return view;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			if (shouldPersist()) {
				persistInt(result + minimumValue);
			}
		}
		super.onClick(dialog, which);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

		if (stepSize >=1){
			result = Math.round(progress / stepSize) * stepSize;
		} else {
			result = progress;
		}

		textView.setText(String.valueOf(result + minimumValue) + (units == null ? "" : units));

		callChangeListener(result);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}
}
