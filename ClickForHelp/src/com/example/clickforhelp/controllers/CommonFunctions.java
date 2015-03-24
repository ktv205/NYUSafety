package com.example.clickforhelp.controllers;

import java.util.HashMap;
import java.util.Map;

import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.RequestParams;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;

public class CommonFunctions {
	// private static final String TAG = "CommonFunctions";

	public static boolean isConnected(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info != null && info.isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	public static Boolean isOnline() {
		boolean reachable = false;
		try {
			Process p1 = java.lang.Runtime.getRuntime().exec(
					"ping -c 1 www.google.com");
			int returnVal = p1.waitFor();
			reachable = (returnVal == 0);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return reachable;
	}

	public static RequestParams setParams(String scheme, String authority,
			String[] paths) {
		RequestParams params = new RequestParams();
		Uri.Builder url = new Uri.Builder();
		url.scheme(scheme).authority(authority).build();
		for (String s : paths) {
			url.appendPath(s);
		}
		url.build();
		params.setURI(url.toString());
		params.setMethod("GET");
		return params;
	}

	public static SharedPreferences getSharedPreferences(Context context,
			String name) {
		return context.getSharedPreferences(name, Context.MODE_PRIVATE);
	}

	public static boolean saveInPreferences(Context context, String name,
			HashMap<String, String> values) {
		SharedPreferences pref = getSharedPreferences(context, name);
		SharedPreferences.Editor edit = pref.edit();
		for (Map.Entry<String, String> entry : values.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			edit.putString(key, value);
		}
		edit.commit();
		return true;
	}

	public static boolean isMyServiceRunning(Class<?> serviceClass,
			Context context) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		boolean running = false;
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				running = true;
			}
		}
		return running;
	}

	public static boolean validNyuEmail(String email) {
		String[] split = email.split("@");
		if (split.length > 1) {
			if (split[1].equals("nyu.edu")) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public static void settingUserPreferenceLocationUpdates(
			Context context) {
		int value=userLocationUpdatePreference(context);
		Intent sendLocationIntentService = new Intent(context,
				LocationUpdateService.class);
		if (value == AppPreferences.SharedPrefLocationSettings.NEVER) {
			if (CommonFunctions.isMyServiceRunning(LocationUpdateService.class,
					context)) {
				context.stopService(sendLocationIntentService);
			} else {
			}

		} else if (value == AppPreferences.SharedPrefLocationSettings.PLUGGEDIN) {
			if (checkPluggedIn(context)) {
			} else {
				if (CommonFunctions.isMyServiceRunning(
						LocationUpdateService.class, context)) {
					context.stopService(sendLocationIntentService);
				} else {
				}

			}

		} else if (value == AppPreferences.SharedPrefLocationSettings.RECOMENDED) {
			if (CommonFunctions.isMyServiceRunning(LocationUpdateService.class,
					context)) {
				if (checkChargingLevel(context)) {
					// No need to stop the service
				} else {
					context.stopService(sendLocationIntentService);
				}

			} else {
				if (checkChargingLevel(context)) {
					context.startService(sendLocationIntentService);
				} else {
					// already service started
				}
			}
		} else {
			// already service started
		}
	}

	public static boolean checkPluggedIn(Context context) {
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, ifilter);
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;
		if (isCharging) {
			int chargePlug = batteryStatus.getIntExtra(
					BatteryManager.EXTRA_PLUGGED, -1);
			isCharging = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
		} else {
			isCharging = status == BatteryManager.BATTERY_STATUS_FULL;
		}

		return isCharging;
	}

	public static boolean checkChargingLevel(Context context) {
		boolean level = false;
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, ifilter);
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
		if (status == BatteryManager.BATTERY_HEALTH_GOOD) {
			level = true;
		}
		return level;

	}
	
	public static int userLocationUpdatePreference(Context context){
		
		SharedPreferences pref = CommonFunctions.getSharedPreferences(context,
				AppPreferences.SharedPrefLocationSettings.name);
		final int value = pref.getInt(
				AppPreferences.SharedPrefLocationSettings.Preference,
				AppPreferences.SharedPrefLocationSettings.ALWAYS);
		return value;
	}

}
