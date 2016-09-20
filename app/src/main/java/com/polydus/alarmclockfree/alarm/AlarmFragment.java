package com.polydus.alarmclockfree.alarm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.polydus.alarmclockfree.R;
import com.polydus.alarmclockfree.alarm.implementation.AlarmBroadcastReceiver;
import com.polydus.alarmclockfree.alarm.preferences.AlarmPreferencesActivity;
import com.polydus.alarmclockfree.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;

public class AlarmFragment extends android.support.v4.app.Fragment {

	private View rootView;
	private SharedPreferences preferences;

	private SimpleAdapter alarmListAdapter;
	private ArrayList<HashMap<String, String>> alarmListItems;
	private ListView alarmListView;

	private ArrayList<Switch> switches;
	private boolean handledSwitchListeners;

	private Bundle savedInstanceState;

	public AlarmFragment() {
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		firstVisiblePosition = -9;
		lastVisiblePosition = -9;
		if (switches == null){
			switches = new ArrayList<>();
		}
		if (input == null) input = new ArrayList<>();
		cleanupPreferences();
		//see if there are active alarms with 0 days
		checkInvalidAlarms();

		//init view layout
		initAlarmListLayout();

		//add alarm list items from preferences
		int alarmAmount = preferences.getInt(Constants.ALARM_AMOUNT, 0);

		for (int i = 0; i < alarmAmount; i++) {
			HashMap<String, String> newAlarm = new HashMap<String, String>();
			newAlarm.put("time", preferences.getString(i + Constants.X_ALARM_TIME, Constants.ALARM_TIME_DEFAULT));
			newAlarm.put("name", preferences.getString(i + Constants.X_ALARM_NAME, Constants.ALARM_NAME_DEFAULT));
			newAlarm.put("details", preferences.getString(i + Constants.X_ALARM_ACTIVE_DAYS_SUMMARY, "asdf"));
			newAlarm.put("toggle", "");

			alarmListItems.add(newAlarm);
			alarmListAdapter.notifyDataSetChanged();
		}
		//add listeners
		addListenersToViews();

		super.onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_alarm, container, false);

		this.savedInstanceState = savedInstanceState;
		preferences = getActivity().getSharedPreferences(Constants.ALARM_PREFERENCES, Context.MODE_PRIVATE);

		//removeScheduledAlarms();
		//deleteAllPrefs();

		return rootView;
	}

	private TextView footer;

	private void initAlarmListLayout() {
		alarmListItems = new ArrayList<HashMap<String, String>>();
		alarmListView = (ListView) rootView.findViewById(R.id.alarm_list);
		//alarmListView.setVelocityScale(0.1f);
		//alarmListView.setFriction(ViewConfiguration.getScrollFriction() * 10);
		alarmListAdapter = new SimpleAdapter(
				getActivity(),
				alarmListItems,
				R.layout.alarm_list_item,
				new String[]{"time", "name", "details", "toggle"},
				new int[]{R.id.alarm_list_item_time, R.id.alarm_list_item_name, R.id.alarm_list_item_details, R.id.alarm_list_item_toggle});

		/*if (alarmListView.getFooterViewsCount() == 0){
			LayoutInflater inflater = getLayoutInflater(savedInstanceState);
			ViewGroup footer = (ViewGroup) inflater.inflate(R.layout.alarm_list_footer, alarmListView, false);
			alarmListView.addFooterView(footer, null, true);
			alarmListView.setFooterDividersEnabled(true);
		}*/
		footer = (TextView) getActivity().findViewById(R.id.alarm_list_footer_text);
		footer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startAlarmCustomizeActivity(alarmListAdapter.getCount(), true);
			}
		});

		alarmListView.setAdapter(alarmListAdapter);
	}

	private void addListenersToViews() {
		alarmListView.setLongClickable(true);
		alarmListView.setClickable(true);
		//short presses
		alarmListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//handle preference screen on list item click here
				//using id & startAlarmCustomizeActivity
				//if (view.getId() == R.id.alarm_list_footer) {
					//System.out.println("footer. adding alarm #" + alarmListAdapter.getCount());
				//	startAlarmCustomizeActivity(alarmListAdapter.getCount(), true);
				//} else {
					//System.out.println("editing item: " + position);
					startAlarmCustomizeActivity(position, false);
				//}
			}
		});
		handledSwitchListeners = false;

		//add listeners to switches like this, really no other way to do this
		//always called when listview is inflated, using boolean to avoid calling this multiple times
		alarmListView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				if (!handledSwitchListeners){
					System.out.println(alarmListView.getFirstVisiblePosition());
					System.out.println(alarmListView.getLastVisiblePosition());
					System.out.println(alarmListView.getChildCount());
					//System.out.println("layout change");
					scheduleAlarms();
					setListenersOnSwitches();
					handledSwitchListeners = true;
				}
			}
		});

		alarmListView.setRecyclerListener(new AbsListView.RecyclerListener() {
			@Override
			public void onMovedToScrapHeap(View view) {
				//System.out.println(view.getId());
				//setListenersOnSwitches();
				//System.out.println("onMovedToScrapHeap");
				//System.out.println("getCount: " + alarmListView.getCount());


				if (alarmListView.getChildCount() > 0){
					//System.out.println("getChildCount: " + alarmListView.getChildCount());

					if(firstVisiblePosition != alarmListView.getFirstVisiblePosition()
					   || lastVisiblePosition != alarmListView.getLastVisiblePosition()){
						setListenersOnSwitches();
						//scheduleAlarms();
						/*ArrayList<View> input = new ArrayList<>();
						CharSequence charSequence = "alarm_switch";
						rootView.findViewsWithText(input, charSequence, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
						System.out.println("visible switches: " + input.size());*/
					}

					if (firstVisiblePosition != alarmListView.getFirstVisiblePosition()){
						firstVisiblePosition = alarmListView.getFirstVisiblePosition();
						//System.out.println("getFirstVisiblePosition: " + alarmListView.getFirstVisiblePosition());
					}
					if (lastVisiblePosition != alarmListView.getLastVisiblePosition()){
						lastVisiblePosition = alarmListView.getLastVisiblePosition();
						//System.out.println("getLastVisiblePosition: " + alarmListView.getLastVisiblePosition());
					}
				}
			}
		});

		//long presses

		alarmListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

				if (view.getId() == R.id.alarm_list_item) {
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

					//dialog items
					CharSequence[] dialogItems = {"Delete", "Delete all"};
					//builder.setTitle("dialog title");

					builder.setItems(dialogItems, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0) {
								removeScheduledAlarms();
								removeAlarmById(position);
								scheduleAlarms();
								alarmListAdapter.notifyDataSetChanged();
							}
							if (which == 1) {
								removeScheduledAlarms();
								deleteAllPrefs();
								alarmListItems.clear();
								alarmListAdapter.notifyDataSetChanged();
							}
						}
					});

					AlertDialog menu = builder.create();
					menu.show();

					return true;
				} else {
					//longpress on footer
					return true;
				}
			}
		});
	}

	private int firstVisiblePosition;
	private int lastVisiblePosition;

	private ArrayList<View> input;

	private void setListenersOnSwitches(){
		/*System.out.println("getChildCount: " + alarmListView.getChildCount());
		System.out.println("getCount: " + alarmListView.getCount());
		System.out.println("getFirstVisiblePosition: " + alarmListView.getFirstVisiblePosition());
		System.out.println("getLastVisiblePosition: " + alarmListView.getLastVisiblePosition());*/


		input.clear();
		switches.clear();
		rootView.findViewsWithText(input, "alarm_switch", View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);

		int first = alarmListView.getFirstVisiblePosition();
		//int last = alarmListView.getLastVisiblePosition();
		//if (last == alarmListView.getCount()) last--;

		for (int i = 0; i < input.size();i++) {
			switches.add(i, (Switch) input.get(i));

			switches.get(i).setChecked(getAlarmStatus(i + first));
			System.out.println("Switch #" + (i + first) + " is: " + switches.get(i).isChecked());

			/*switches.get(i).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Switch thisSwitch = null;
					int j = -1;
					for (int i = 0; i < switches.size(); i++){
						if (v.equals(switches.get(i))){
							thisSwitch = switches.get(i);
							j = i + alarmListView.getFirstVisiblePosition();
							break;
						}
					}
					if (j == -1) return;
					if (thisSwitch == null) return;
					if (!setAlarmStatus(j, thisSwitch.isChecked())){
						thisSwitch.setChecked(false);
					} else {
						thisSwitch.setChecked(!thisSwitch.isChecked());
					}
					scheduleAlarms();
				}
			});*/

			switches.get(i).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					//int first = alarmListView.getFirstVisiblePosition();
					//int last = first + switches.size();
					for (int i = 0; i < switches.size(); i++){
						if (buttonView.getParent().getParent().equals(alarmListView.getChildAt(i))){
							int j = i + alarmListView.getFirstVisiblePosition();
							if(!setAlarmStatus(j, isChecked)){
								switches.get(j).setChecked(false);
							}
							System.out.println("switch is #: " + j);
							scheduleAlarms();
						}
					}
					/*int first = alarmListView.getFirstVisiblePosition();
					int last = first + switches.size();
					for (int i = first; i < last; i++){
						if (buttonView.getParent().getParent().equals(alarmListView.getChildAt(i))){
							if(!setAlarmStatus(i, isChecked)){
								switches.get(i).setChecked(false);
							}
							System.out.println("switch is #: " + i);
							scheduleAlarms();
						}
					}*/
				}
			});
		}



		/*for (int i = 0; i < input.size(); i++){
			switches.add(i, (Switch) input.get(i));

			switches.get(i).setChecked(getAlarmStatus(i));

			switches.get(i).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					/*int amount = (alarmListView.getCount() - 1);
					for (int i = 0; i < amount; i++){

					}*/

					// i <= because getlastpos in listview is crap, it wont call the last entry otherwise
					/*for (int i = alarmListView.getFirstVisiblePosition(); i <= alarmListView.getLastVisiblePosition(); i++){
						if (buttonView.getParent().getParent().equals(alarmListView.getChildAt(i - alarmListView.getFirstVisiblePosition()))){
							System.out.println("switch is #: " + i);
							if(!setAlarmStatus(i, isChecked)){
								switches.get(i).setChecked(false);
							}
							scheduleAlarms();
						}
					}
				}
			});*/
		}

						/*System.out.println(alarmListView.getFirstVisiblePosition());
					System.out.println(alarmListView.getLastVisiblePosition());
					System.out.println("onclick in switch");
					System.out.println(buttonView.getClass());
					System.out.println(buttonView.getId());
					System.out.println(isChecked);
					System.out.println(buttonView.getParent());*/

	private void startAlarmCustomizeActivity(int id, boolean newAlarm) {
		Intent intent = new Intent(getActivity(), AlarmPreferencesActivity.class);
		intent.putExtra("alarm_id", id);
		intent.putExtra("new_alarm", newAlarm);
		startActivity(intent);
	}

	private boolean setAlarmStatus(int id, boolean status){
		SharedPreferences.Editor editor = preferences.edit();

		int size;
		try{
			size = preferences.getStringSet(id + Constants.X_ALARM_ACTIVE_DAYS, null).size();
		}catch (Exception e){
			e.printStackTrace();
			size = -1;
		}

		if (size == 0){
			Toast toast = Toast.makeText(getActivity().getApplicationContext(), "This alarm has no days selected", Toast.LENGTH_SHORT);
			toast.show();
			editor.putBoolean(Integer.toString(id) + Constants.X_ALARM_STATUS, false);
			editor.apply();
			return false;
		}

		editor.putBoolean(Integer.toString(id) + Constants.X_ALARM_STATUS, status);
		editor.apply();
		return true;
	}

	private boolean getAlarmStatus(int id){
		return preferences.getBoolean(Integer.toString(id) + Constants.X_ALARM_STATUS, false);
	}

	private void deleteAllPrefs() {
		SharedPreferences.Editor editor = getActivity().getSharedPreferences(Constants.ALARM_PREFERENCES, Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.apply();
	}

	private void removeAlarmById(int id) {
		//prefs delete
		int newAmount = preferences.getInt(Constants.ALARM_AMOUNT, -1);
		if (newAmount == -1) return;
		newAmount--;
		SharedPreferences.Editor editor = preferences.edit();

		if(id != newAmount){
			for (int i = id; i < newAmount;i++) {
				//need to shift all above the removed item
				editor.putString(i + Constants.X_ALARM_NAME, preferences.getString((i + 1) + Constants.X_ALARM_NAME, Constants.ALARM_NAME_DEFAULT));
				editor.putString(i + Constants.X_ALARM_TIME, preferences.getString((i + 1) + Constants.X_ALARM_TIME, Constants.ALARM_TIME_DEFAULT));
				editor.putStringSet(i + Constants.X_ALARM_ACTIVE_DAYS, preferences.getStringSet((i + 1) + Constants.X_ALARM_ACTIVE_DAYS, Constants.ALARM_ACTIVE_DAYS_DEFAULT));
				editor.putBoolean(i + Constants.X_ALARM_STATUS, preferences.getBoolean((i + 1) + Constants.X_ALARM_STATUS, Constants.ALARM_STATUS_DEFAULT));
				editor.putString(i + Constants.X_ALARM_SOURCE, preferences.getString((i + 1) + Constants.X_ALARM_SOURCE, Constants.ALARM_SOURCE_DEFAULT));
				editor.putBoolean(i + Constants.X_ALARM_VIBRATION, preferences.getBoolean((i + 1) + Constants.X_ALARM_VIBRATION, Constants.ALARM_VIBRATION_DEFAULT));
				editor.putString(i + Constants.X_ALARM_SOUND, preferences.getString((i + 1) + Constants.X_ALARM_SOUND, Constants.ALARM_SOUND_DEFAULT));
				editor.putString(i + Constants.X_ALARM_CUSTOM_SOUND, preferences.getString((i + 1) + Constants.X_ALARM_CUSTOM_SOUND, Constants.ALARM_CUSTOM_SOUND_DEFAULT));
				editor.putInt(i + Constants.X_ALARM_VOLUME, preferences.getInt((i + 1) + Constants.X_ALARM_VOLUME, Constants.ALARM_VOLUME_DEFAULT));
				editor.putBoolean(i + Constants.X_ALARM_SNOOZE_ENABLED, preferences.getBoolean((i + 1) + Constants.X_ALARM_SNOOZE_ENABLED, Constants.ALARM_SNOOZE_ENABLED_DEFAULT));
				editor.putInt(i + Constants.X_ALARM_SNOOZE_TIME, preferences.getInt((i + 1) + Constants.X_ALARM_SNOOZE_TIME, Constants.ALARM_SNOOZE_TIME_DEFAULT));
				alarmListItems.set(i, alarmListItems.get(i + 1));
			}
			id = newAmount;
		}

		editor.remove(id + Constants.X_ALARM_NAME);
		editor.remove(id + Constants.X_ALARM_TIME);
		editor.remove(id + Constants.X_ALARM_ACTIVE_DAYS);
		editor.remove(id + Constants.X_ALARM_STATUS);
		editor.remove(id + Constants.X_ALARM_VIBRATION);
		editor.remove(id + Constants.X_ALARM_SOURCE);
		editor.remove(id + Constants.X_ALARM_SOUND);
		editor.remove(id + Constants.X_ALARM_CUSTOM_SOUND);
		editor.remove(id + Constants.X_ALARM_VOLUME);
		editor.remove(id + Constants.X_ALARM_SNOOZE_ENABLED);
		editor.remove(id + Constants.X_ALARM_SNOOZE_TIME);

		editor.putInt(Constants.ALARM_AMOUNT, newAmount);
		editor.apply();
		//view delete
		alarmListItems.remove(id);
		alarmListAdapter.notifyDataSetChanged();
	}

	private void scheduleAlarms(){
		AlarmBroadcastReceiver.setAllAlarms(getActivity().getApplicationContext());
	}

	private void removeScheduledAlarms(){
		AlarmBroadcastReceiver.cancelAllAlarms(getActivity().getApplicationContext());
	}

	private void cleanupPreferences(){
		SharedPreferences.Editor editor = preferences.edit();

		editor.remove(Constants.X_ALARM_NAME);
		editor.remove(Constants.X_ALARM_TIME);
		editor.remove(Constants.X_ALARM_ACTIVE_DAYS);
		editor.remove(Constants.X_ALARM_STATUS);
		editor.remove(Constants.X_ALARM_VIBRATION);
		editor.remove(Constants.X_ALARM_SOURCE);
		editor.remove(Constants.X_ALARM_SOUND);
		editor.remove(Constants.X_ALARM_CUSTOM_SOUND);
		editor.remove(Constants.X_ALARM_VOLUME);
		editor.remove(Constants.X_ALARM_SNOOZE_ENABLED);
		editor.remove(Constants.X_ALARM_SNOOZE_TIME);

		editor.apply();
	}

	private void checkInvalidAlarms(){
		//see if there are active alarms with 0 days
		int alarmAmount = preferences.getInt(Constants.ALARM_AMOUNT, 0);
		SharedPreferences.Editor edit = preferences.edit();

		for (int i = 0; i < alarmAmount; i++){
			if (preferences.getBoolean(Integer.toString(i) + Constants.X_ALARM_STATUS, false)){
				if (preferences.getStringSet(Integer.toString(i) + Constants.X_ALARM_ACTIVE_DAYS, null).size() == 0){
					edit.putBoolean(Integer.toString(i) + Constants.X_ALARM_STATUS, false);
					edit.apply();
				}
			}
		}
	}
}
