package com.polydus.alarmclockfree.alarm.preferences.custom;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.polydus.alarmclockfree.R;
import com.polydus.alarmclockfree.util.Constants;

/**
 * Created by leonard on 5-5-15.
 */
public class NumberPickerPreference extends DialogPreference implements NumberPicker.OnValueChangeListener{

	private NumberPicker numberPicker;
	private TextView textView;

	public NumberPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected View onCreateDialogView() {
		LayoutInflater layoutInflater = LayoutInflater.from(getContext());
		View view = layoutInflater.inflate(R.layout.numberpicker_preference, null);

		numberPicker = (NumberPicker) view.findViewById(R.id.numberpicker_preference_numberpicker);
		textView = (TextView) view.findViewById(R.id.numberpicker_preference_textview);

		numberPicker.setMinValue(1);
		numberPicker.setMaxValue(60);
		numberPicker.setValue(getPersistedInt(Constants.ALARM_SNOOZE_TIME_DEFAULT));//default
		numberPicker.setOnValueChangedListener(this);

		return view;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == Dialog.BUTTON_POSITIVE){
			if (shouldPersist()){
				persistInt(numberPicker.getValue());
			}
		}
		callChangeListener(numberPicker.getValue());
		super.onClick(dialog, which);
	}

	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		//idk like, whatever
	}
}
