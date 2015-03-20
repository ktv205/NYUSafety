package com.example.clickforhelp.controllers;

import com.example.clickforhelp.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.preference.PreferenceManager;

import android.widget.Toast;

public class BatteryLevelReceiver extends BroadcastReceiver {
	//private static final String TAG = "BatteryLevelReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Toast.makeText(context, "battery level triggered", Toast.LENGTH_LONG)
				.show();
		int status = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		int value = Integer
				.valueOf(pref.getString(context
						.getString(R.string.string_key_location_settings), "-1"));
		if (value == 1) {

		} else if (value == 2) {

			if (status == BatteryManager.BATTERY_HEALTH_COLD
					|| status == BatteryManager.BATTERY_HEALTH_DEAD) {
				if (new CommonFunctions().isMyServiceRunning(
						LocationUpdateService.class, context)) {
					context.stopService(new Intent(context,
							LocationUpdateService.class));
				}

			} else if (status == BatteryManager.BATTERY_HEALTH_GOOD) {
				if (new CommonFunctions().isMyServiceRunning(
						LocationUpdateService.class, context)) {

				} else {
					context.startService(new Intent(context,
							LocationUpdateService.class));
				}

			}
		}

	}

}
