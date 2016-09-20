package com.polydus.alarmclockfree.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.polydus.alarmclockfree.StopwatchFragment;
import com.polydus.alarmclockfree.alarm.AlarmFragment;
import com.polydus.alarmclockfree.timer.TimerFragment;

/**
 * Created by leonard on 21-4-15.
 */
public class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter{

	public static final int FRAGMENT_COUNT = 3;

	public static final int FRAGMENT_ALARM_POSITION = 0;
	public static final int FRAGMENT_STOPWATCH_POSITION = 1;
	public static final int FRAGMENT_TIMER_POSITION = 2;

	public FragmentPagerAdapter(FragmentManager fm){
		super(fm);
	}

	@Override
	public Fragment getItem(int i) {
		Fragment fragment;

		switch (i){
			case FRAGMENT_ALARM_POSITION:
				fragment = new AlarmFragment();
				break;
			case FRAGMENT_STOPWATCH_POSITION:
				fragment = new StopwatchFragment();
				break;
			case FRAGMENT_TIMER_POSITION:
				fragment = new TimerFragment();
				break;
			default:
				fragment = new AlarmFragment();
		}
		return fragment;
	}

	@Override
	public int getCount() {
		return FRAGMENT_COUNT;
	}
}