package com.polydus.alarmclockfree;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.polydus.alarmclockfree.alarm.implementation.AlarmBroadcastReceiver;
import com.polydus.alarmclockfree.util.FragmentPagerAdapter;

import java.util.Arrays;

public class MainActivity extends ActionBarActivity{

	public final String FRAGMENT_POSITION_PREFERENCES = "FRAGMENT_POSITION_PREFERENCES";
	public final String ACTIVE_FRAGMENT = "ACTIVE_FRAGMENT";

	private Toolbar toolbar;

	private ViewPager viewPager;
	private FragmentPagerAdapter fragmentPagerAdapter;

	private MenuItem[] menuItems;
	private SpannableString[] menuItemsTitles;
	private boolean[] greyedOutTitle;

	private ForegroundColorSpan greyColor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
			//apparently this is needed
			if (ContextCompat.checkSelfPermission(
					this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{"android.permission.READ_PHONE_STATE"}, 0);
		}
		setContentView(R.layout.activity_main);

		setToolbar();
		setViewPager();
	}

	@Override
	protected void onPause() {
		SharedPreferences preferences = getSharedPreferences(FRAGMENT_POSITION_PREFERENCES, MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(ACTIVE_FRAGMENT, viewPager.getCurrentItem());
		editor.apply();

		AlarmBroadcastReceiver.setAllAlarms(this);

		super.onPause();
	}

	@Override
	protected void onResume() {
		SharedPreferences preferences = getSharedPreferences(FRAGMENT_POSITION_PREFERENCES, MODE_PRIVATE);
		viewPager.setCurrentItem(preferences.getInt(ACTIVE_FRAGMENT, 0));
		super.onResume();
	}

	private void setToolbar(){
		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);

		toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
		toolbar.inflateMenu(R.menu.menu_main);
		toolbar.canShowOverflowMenu();

		View childView = toolbar.getChildAt(0);
		int screenWidth = metrics.widthPixels;
		Toolbar.LayoutParams toolbarParams = new Toolbar.LayoutParams(screenWidth, ViewGroup.LayoutParams.MATCH_PARENT);
		childView.setLayoutParams(toolbarParams);

		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				switch (menuItem.getItemId()) {
					case R.id.action_alarm:
						viewPager.setCurrentItem(FragmentPagerAdapter.FRAGMENT_ALARM_POSITION);
						break;
					case R.id.action_stopwatch:
						viewPager.setCurrentItem(FragmentPagerAdapter.FRAGMENT_STOPWATCH_POSITION);
						break;
					case R.id.action_timer:
						viewPager.setCurrentItem(FragmentPagerAdapter.FRAGMENT_TIMER_POSITION);
						break;
				}
				return true;
			}
		});
	}

	private void setViewPager(){
		fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager());

		viewPager = (ViewPager) findViewById(R.id.viewpager);
		viewPager.setAdapter(fragmentPagerAdapter);

		menuItems = new MenuItem[FragmentPagerAdapter.FRAGMENT_COUNT];

		menuItems[FragmentPagerAdapter.FRAGMENT_ALARM_POSITION] = toolbar.getMenu().findItem(R.id.action_alarm);
		menuItems[FragmentPagerAdapter.FRAGMENT_STOPWATCH_POSITION] = toolbar.getMenu().findItem(R.id.action_stopwatch);
		menuItems[FragmentPagerAdapter.FRAGMENT_TIMER_POSITION] = toolbar.getMenu().findItem(R.id.action_timer);


		greyedOutTitle = new boolean[FragmentPagerAdapter.FRAGMENT_COUNT];
		Arrays.fill(greyedOutTitle, true);
		menuItemsTitles = new SpannableString[FragmentPagerAdapter.FRAGMENT_COUNT];

		greyColor = new ForegroundColorSpan(R.color.bright_foreground_disabled_material_dark);

		for (int i = 0; i < FragmentPagerAdapter.FRAGMENT_COUNT; i++){
			menuItemsTitles[i] = new SpannableString(menuItems[i].getTitle());
			menuItemsTitles[i].setSpan(greyColor, 0, menuItemsTitles[i].length(), 0);
			menuItems[i].setTitle(menuItemsTitles[i]);
		}

		menuItemsTitles[viewPager.getCurrentItem()].removeSpan(greyColor);
		greyedOutTitle[viewPager.getCurrentItem()] = false;
		menuItems[viewPager.getCurrentItem()].setTitle(menuItemsTitles[viewPager.getCurrentItem()]);

		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int i, float v, int i2) {
			}

			@Override
			public void onPageSelected(int i) {
				setTextColours(i);
			}

			@Override
			public void onPageScrollStateChanged(int i) {
			}
		});
	}

	private void setTextColours(int id){
		for (int i = 0; i < FragmentPagerAdapter.FRAGMENT_COUNT; i++){
			if (!greyedOutTitle[i]){
				//old one
				menuItemsTitles[i].setSpan(greyColor, 0, menuItemsTitles[i].length(), 0);
				greyedOutTitle[i] = true;
				menuItems[i].setTitle(menuItemsTitles[i]);
				break;
			}
		}
		//new one
		menuItemsTitles[id].removeSpan(greyColor);
		greyedOutTitle[id] = false;
		menuItems[id].setTitle(menuItemsTitles[id]);
	}

}
