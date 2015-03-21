package com.example.clickforhelp.controllers;

import com.example.clickforhelp.models.AppPreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.widget.Toast;

public class PowerConnectionReceiver extends BroadcastReceiver {

	// private static final String TAG = "PowerConnectionReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Toast.makeText(context, "in checking if connected to a charger",
				Toast.LENGTH_LONG).show();

		int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

		SharedPreferences pref = new CommonFunctions().getSharedPreferences(
				context, AppPreferences.SharedPrefLocationSettings.name);
		int value = pref.getInt(
				AppPreferences.SharedPrefLocationSettings.Preference,
				AppPreferences.SharedPrefLocationSettings.ALWAYS);
		if (value == 1) {

		} else if (value == 3) {
			if (acCharge) {
				if (new CommonFunctions().isMyServiceRunning(
						LocationUpdateService.class, context)) {

				} else {
					context.startService(new Intent(context,
							LocationUpdateService.class));
				}
			} else {
				if (new CommonFunctions().isMyServiceRunning(
						LocationUpdateService.class, context)) {
					context.stopService(new Intent(context,
							LocationUpdateService.class));

				}
			}

		}

	}

}
