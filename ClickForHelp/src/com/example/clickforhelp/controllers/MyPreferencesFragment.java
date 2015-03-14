package com.example.clickforhelp.controllers;

import com.example.clickforhelp.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

public class MyPreferencesFragment extends PreferenceFragment implements
		android.content.SharedPreferences.OnSharedPreferenceChangeListener {
	private static final String TAG = "MyPreferencesFragment";
	private static final String[] values={"send updates all the time",
    "send updates if the power is 30 percent or more(recommended)",
    "when plugged in to a power source",
    "never send location updates(not recommended)"};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		Log.d(TAG, "onCreate");
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.d(TAG, "here");
		Preference pref = findPreference(key);
		pref.setSummary(values[Integer.valueOf(sharedPreferences.getString(
				getString(R.string.string_key_location_settings), ""))-1]);
		Log.d(TAG,key);

	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
		ListPreference pref = (ListPreference) findPreference(getString(R.string.string_key_location_settings));
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		if (pref != null) {
			pref.setSummary(values[Integer.valueOf(sharedPref.getString(
					getString(R.string.string_key_location_settings), ""))-1]);
		}
		Log.d(TAG, sharedPref.getString(
				getString(R.string.string_key_location_settings), "nothing"));
		Log.d(TAG,"in OnResume->"+getString(R.string.string_key_location_settings));
	}

}
