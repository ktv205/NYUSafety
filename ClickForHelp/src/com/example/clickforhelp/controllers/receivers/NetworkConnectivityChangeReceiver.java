package com.example.clickforhelp.controllers.receivers;

import com.example.clickforhelp.controllers.services.LocationUpdateService;
import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.InternetConnectionAsyncTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class NetworkConnectivityChangeReceiver extends BroadcastReceiver {
	private static final String TAG = NetworkConnectivityChangeReceiver.class
			.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Toast.makeText(context, "network changed", Toast.LENGTH_SHORT).show();
		// Log.d(TAG, "this is called");
		if (CommonFunctions.checkLoggedIn(context)) {
			if (CommonFunctions.isActivityRunning(context)) {
				Log.d(TAG, "app is running in foreground");
			} else {
				Log.d(TAG, "app is in background");
				if (CommonFunctions.isConnected(context)) {
					// Log.d(TAG, "connected");
					new InternetConnectionAsyncTask(context).execute();
				} else {
					// Log.d(TAG, "not connected");
					if (CommonFunctions.isMyServiceRunning(
							LocationUpdateService.class, context)) {
						context.stopService(new Intent(context,
								LocationUpdateService.class));
					}
				}

			}
		}

	}

}
