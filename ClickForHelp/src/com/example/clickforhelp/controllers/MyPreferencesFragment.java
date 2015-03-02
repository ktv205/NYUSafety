package com.example.clickforhelp.controllers;

import com.example.clickforhelp.R;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class MyPreferencesFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 addPreferencesFromResource(R.xml.preferences);
	}

}
