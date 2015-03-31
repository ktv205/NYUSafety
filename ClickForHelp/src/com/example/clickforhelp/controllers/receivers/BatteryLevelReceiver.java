package com.example.clickforhelp.controllers.receivers;

import com.example.clickforhelp.controllers.utils.CommonFunctions;
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
		if (CommonFunctions
				.getSharedPreferences(context,
						AppPreferences.SharedPrefAuthentication.name)
				.getString(AppPreferences.SharedPrefAuthentication.flag, "")
				.isEmpty()
				|| CommonFunctions.getSharedPreferences(context,
						AppPreferences.SharedPrefAuthentication.name)
						.getString(
								AppPreferences.SharedPrefAuthentication.flag,
								"") == "-1") {
			// Dont do anything since user is not logged in

		} else {
			CommonFunctions.settingUserPreferenceLocationUpdates(context);
		}

	}

}
