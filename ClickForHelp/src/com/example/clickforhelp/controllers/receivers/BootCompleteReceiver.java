package com.example.clickforhelp.controllers.receivers;

import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.models.AppPreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver {
	public static final String TAG = "BootReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		// Toast.makeText(context, "boot complete", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "onReceive");
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
