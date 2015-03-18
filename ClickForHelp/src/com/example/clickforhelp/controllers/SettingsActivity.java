package com.example.clickforhelp.controllers;

import com.example.clickforhelp.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Add 'general' preferences, defined in the XML file
		addPreferencesFromResource(R.xml.preferences);

		// For all preferences, attach an OnPreferenceChangeListener so the UI
		// summary can be
		// updated when the preference changes.
		bindPreferenceSummaryToValue(findPreference(getString(R.string.string_key_location_settings)));
	}

	private void bindPreferenceSummaryToValue(Preference findPreference) {
		findPreference.setOnPreferenceChangeListener(this);

		// Trigger the listener immediately with the preference's
		// current value.
		onPreferenceChange(findPreference, PreferenceManager
				.getDefaultSharedPreferences(findPreference.getContext())
				.getString(findPreference.getKey(), ""));

	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String stringValue = newValue.toString();

		if (preference instanceof ListPreference) {
			// For list preferences, look up the correct display value in
			// the preference's 'entries' list (since they have separate
			// labels/values).
			ListPreference listPreference = (ListPreference) preference;
			int prefIndex = listPreference.findIndexOfValue(stringValue);
			if (prefIndex >= 0) {
				preference.setSummary(listPreference.getEntries()[prefIndex]);
			}
		}
		return true;
	}

}
