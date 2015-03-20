package com.example.clickforhelp.controllers;

import com.example.clickforhelp.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

public class MyPreferencesFragment extends PreferenceFragment implements
		android.content.SharedPreferences.OnSharedPreferenceChangeListener {
	private static final String TAG = "MyPreferencesFragment";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.d(TAG, "onSharedPreferenceChanged");
		sharedPreferences.getString(
				getString(R.string.string_key_location_settings), "sfdf");
		setSummary();

	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d(TAG, "onAttach");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated");
		addPreferencesFromResource(R.xml.preferences);
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
		setSummary();
	}

	public void setSummary() {
		// ListPreference pref = (ListPreference)
		// findPreference(getString(R.string.string_key_location_settings));
		// SharedPreferences sharedPref = PreferenceManager
		// .getDefaultSharedPreferences(getActivity());
		// if (pref != null) {
		// pref.setSummary(values[Integer.valueOf(sharedPref.getString(
		// getString(R.string.string_key_location_settings), "")) - 1]);
		// }
	}

}
