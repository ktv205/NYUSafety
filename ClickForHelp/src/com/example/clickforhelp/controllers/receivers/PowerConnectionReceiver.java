package com.example.clickforhelp.controllers.receivers;

import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.InternetConnectionAsyncTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PowerConnectionReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// Toast.makeText(context, "in checking if connected to a charger",
		// Toast.LENGTH_LONG).show();
		if (CommonFunctions.checkLoggedIn(context)) {
			if (CommonFunctions.isActivityRunning(context)) {
				// as the activity is in foreground don't do anything
			} else {
				if (CommonFunctions.isConnected(context)) {
					new InternetConnectionAsyncTask(context);
				}
			}
		} else {
			// Do do anything since user is not logged in
		}
	}

}
