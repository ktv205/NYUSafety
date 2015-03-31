package com.example.clickforhelp.controllers.receivers;

import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.models.AppPreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PowerConnectionReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// Toast.makeText(context, "in checking if connected to a charger",
		// Toast.LENGTH_LONG).show();
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
