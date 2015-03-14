package com.example.clickforhelp.controllers;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class SettingsActivity extends Activity {
	private static final String TAG="SettingsActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction()
        .replace(android.R.id.content, new MyPreferencesFragment())
        .commit();
		Log.d(TAG,"onCreate");
	}

}
