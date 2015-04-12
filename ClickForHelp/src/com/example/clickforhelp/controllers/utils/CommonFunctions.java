package com.example.clickforhelp.controllers.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.clickforhelp.controllers.services.LocationUpdateService;
import com.example.clickforhelp.controllers.ui.MainActivity;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.RequestParams;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.util.Log;

public class CommonFunctions {
	private static final String TAG = CommonFunctions.class.getSimpleName();
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static String mRegid;
	private static final String UPDATE_LOCATION = "updatelocation";

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

	public static boolean hasActiveInternetConnection(Context context) {

		new Thread(new Runnable() {
			public void run() {
				try {
					HttpURLConnection urlc = (HttpURLConnection) (new URL(
							"http://clients3.google.com/generate_204")
							.openConnection());
					urlc.setRequestProperty("User-Agent", "Android");
					urlc.setRequestProperty("Connection", "close");
					urlc.setConnectTimeout(1500);
					urlc.connect();
					Log.d(TAG, "connected");
				} catch (IOException e) {
					Log.d(TAG, "Error checking internet connection");
				}

			}

		}).start();

		return true;
	}

	public static RequestParams setParams(String[] paths) {
		RequestParams params = new RequestParams();
		Uri.Builder url = new Uri.Builder();
		url.scheme(AppPreferences.ServerVariables.SCHEME)
				.authority(AppPreferences.ServerVariables.AUTHORITY).build();
		url.appendPath(AppPreferences.ServerVariables.PUBLIC);
		url.appendPath(AppPreferences.ServerVariables.INDEX);
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

	public static void settingUserPreferenceLocationUpdates(Context context,
			String activity) {
		int value = userLocationUpdatePreference(context);
		Intent sendLocationIntentService = new Intent(context,
				LocationUpdateService.class);
		boolean isServiceRunning = CommonFunctions.isMyServiceRunning(
				LocationUpdateService.class, context);
		if (value == AppPreferences.SharedPrefLocationSettings.NEVER) {
			if (isServiceRunning) {
				context.stopService(sendLocationIntentService);
			} else {

			}

		} else if (value == AppPreferences.SharedPrefLocationSettings.PLUGGEDIN) {
			if (checkPluggedIn(context)) {
				if (isServiceRunning) {

				} else {
					context.startService(sendLocationIntentService);
				}
			} else {
				if (isServiceRunning) {
					context.stopService(sendLocationIntentService);
				} else {

				}

			}

		} else if (value == AppPreferences.SharedPrefLocationSettings.RECOMENDED) {
			if (isServiceRunning) {
				if (checkChargingLevel(context)) {

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
			if (isServiceRunning) {

			} else {
				context.startService(sendLocationIntentService);
			}
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

	public static int userLocationUpdatePreference(Context context) {

		SharedPreferences pref = CommonFunctions.getSharedPreferences(context,
				AppPreferences.SharedPrefLocationSettings.name);
		final int value = pref.getInt(
				AppPreferences.SharedPrefLocationSettings.Preference,
				AppPreferences.SharedPrefLocationSettings.ALWAYS);
		return value;
	}

	public static RequestParams buildLocationUpdateParams(String user_id,
			double latitude, double longitude, String[] strings) {
		String[] locationValues = { UPDATE_LOCATION, user_id,
				String.valueOf(latitude), String.valueOf(longitude),
				strings[0], strings[1] };
		RequestParams locationParams = CommonFunctions
				.setParams(locationValues);
		return locationParams;

	}

	public static RequestParams helpParams(String path, String user_id) {
		String[] values = { path, user_id };
		RequestParams params = CommonFunctions.setParams(values);
		return params;

	}

	public static boolean checkLoggedIn(Context context) {
		SharedPreferences authPref = CommonFunctions.getSharedPreferences(
				context, AppPreferences.SharedPrefAuthentication.name);
		String name = authPref.getString(
				AppPreferences.SharedPrefAuthentication.user_email, "");
		String flag = authPref.getString(
				AppPreferences.SharedPrefAuthentication.flag, "");
		Log.d(TAG, flag);
		if (!name.isEmpty()
				&& flag.equals(AppPreferences.SharedPrefAuthentication.FLAG_ACTIVE)) {
			return true;
		} else {
			return false;
		}

	}

	public static boolean checkIfGCMInfoIsSent(Context context) {
		if (checkPlayServices(context)) {
			mRegid = getRegistrationId(context);
			Log.d(TAG, "mRegid->" + mRegid);
			if (mRegid.isEmpty()) {
				// registerInBackground();
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	public static boolean checkPlayServices(Context context) {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(context);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				// GooglePlayServicesUtil.getErrorDialog(resultCode, mContext,
				// PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				// finish();
			}
			return false;
		} else {
			return true;
		}
	}

	private static String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			return "";
		}
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			return "";
		}
		return registrationId;
	}

	private static SharedPreferences getGCMPreferences(Context context) {
		return getSharedPreferences(context, MainActivity.class.getSimpleName());
	}

	public static void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	public static void saveActivityRecognitionPreference(Context context) {
		SharedPreferences.Editor edit = getSharedPreferences(context,
				AppPreferences.SharedPrefActivityRecognition.name).edit();
		edit.putBoolean(AppPreferences.SharedPrefActivityRecognition.enabled,
				true);
		edit.commit();
	}

	public static Boolean isActivityRunning(Context context) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> services = activityManager
				.getRunningTasks(Integer.MAX_VALUE);
		boolean isActivityFound = false;

		if (services.get(0).topActivity.getPackageName().toString()
				.equalsIgnoreCase(context.getPackageName().toString())) {
			isActivityFound = true;
		}

		if (isActivityFound) {
			return true;
		} else {
			return false;
		}
	}

	public static String getEmail(Context context) {
		return CommonFunctions.getSharedPreferences(context,
				AppPreferences.SharedPrefAuthentication.name).getString(
				AppPreferences.SharedPrefAuthentication.user_email, "");
	}

}
