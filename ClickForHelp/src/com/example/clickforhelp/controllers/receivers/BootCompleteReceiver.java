package com.example.clickforhelp.controllers.receivers;

import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.InternetConnectionAsyncTask;
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
		if (CommonFunctions.checkLoggedIn(context)) {
			if (CommonFunctions.isActivityRunning(context)) {
				// as the activity is in foreground don't do anything
			} else {
				if (CommonFunctions.isConnected(context)) {
					new InternetConnectionAsyncTask(context,AppPreferences.SharedPrefActivityRecognition.WALKING);
				}
			}
		} else {
			// Do do anything since user is not logged in
		}
	}

}
