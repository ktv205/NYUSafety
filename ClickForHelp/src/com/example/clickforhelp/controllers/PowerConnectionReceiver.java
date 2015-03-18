package com.example.clickforhelp.controllers;

import com.example.clickforhelp.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class PowerConnectionReceiver extends BroadcastReceiver {

	private static final String TAG = "PowerConnectionReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "charging broadcast receiver");
		int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
				|| status == BatteryManager.BATTERY_STATUS_FULL;

		int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
		boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		int value = Integer
				.valueOf(pref.getString(context
						.getString(R.string.string_key_location_settings), "-1"));
		if (value == 1) {

		} else if (value == 3) {
			if (acCharge) {
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
