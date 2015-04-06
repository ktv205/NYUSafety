package com.example.clickforhelp.controllers.receivers;

import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.InternetConnectionAsyncTask;
import com.example.clickforhelp.models.AppPreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BatteryLevelReceiver extends BroadcastReceiver {
	// private static final String TAG = "BatteryLevelReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		// Toast.makeText(context, "battery level triggered", Toast.LENGTH_LONG)
		// .show();
		// int status = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
		// int value = CommonFunctions.userLocationUpdatePreference(context);
		if (CommonFunctions.checkLoggedIn(context)) {
			if (CommonFunctions.isActivityRunning(context)) {
				// as the activity is in foreground don't do anything
			} else {
				if (CommonFunctions.isConnected(context)) {
					new InternetConnectionAsyncTask(
							context,
							AppPreferences.SharedPrefActivityRecognition.WALKING);
				}
			}
		} else {
			// Do do anything since user is not logged in
		}

	}

}
