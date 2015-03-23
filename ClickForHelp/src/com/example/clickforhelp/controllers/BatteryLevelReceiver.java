package com.example.clickforhelp.controllers;

import com.example.clickforhelp.models.AppPreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.widget.Toast;

public class BatteryLevelReceiver extends BroadcastReceiver {
	// private static final String TAG = "BatteryLevelReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Toast.makeText(context, "battery level triggered", Toast.LENGTH_LONG)
				.show();
		int status = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
		int value = CommonFunctions.userLocationUpdatePreference(context);
		if (value == AppPreferences.SharedPrefLocationSettings.ALWAYS) {

		} else if (value == AppPreferences.SharedPrefLocationSettings.RECOMENDED) {

			if (status == BatteryManager.BATTERY_HEALTH_COLD
					|| status == BatteryManager.BATTERY_HEALTH_DEAD) {
				if (CommonFunctions.isMyServiceRunning(
						LocationUpdateService.class, context)) {
					context.stopService(new Intent(context,
							LocationUpdateService.class));
				}

			} else if (status == BatteryManager.BATTERY_HEALTH_GOOD) {
				if (CommonFunctions.isMyServiceRunning(
						LocationUpdateService.class, context)) {

				} else {
					context.startService(new Intent(context,
							LocationUpdateService.class));
				}

			}
		}

	}

}
