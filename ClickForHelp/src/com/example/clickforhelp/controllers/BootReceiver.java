package com.example.clickforhelp.controllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Toast.makeText(context, "boot complete", Toast.LENGTH_SHORT).show();
		CommonFunctions.settingUserPreferenceLocationUpdates(context);

	}

}
