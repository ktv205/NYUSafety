package com.example.clickforhelp.controllers.receivers;

import com.example.clickforhelp.controllers.utils.CommonFunctions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompleteReceiver extends BroadcastReceiver {
	public static final String TAG = "BootReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if(!CommonFunctions.isActivityRunning(context) && CommonFunctions.checkLoggedIn(context))
		CommonFunctions.settingUserPreferenceLocationUpdates(context, null);
	}

}
