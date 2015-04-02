package com.example.clickforhelp.controllers.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.clickforhelp.controllers.services.LocationUpdateService;
import com.example.clickforhelp.controllers.ui.MainActivity;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.RequestParams;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.internal.mc;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
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
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static GoogleCloudMessaging mGcm;
	private static String mRegid;

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

	public static void settingUserPreferenceLocationUpdates(Context context) {
		int value = userLocationUpdatePreference(context);
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
			if (isMyServiceRunning(LocationUpdateService.class, context)) {
				// already service started
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
			double latitude, double longitude) {
		String[] locationValues = { "public", "index.php", "updatelocation",
				user_id, String.valueOf(latitude), String.valueOf(longitude) };
		RequestParams locationParams = CommonFunctions.setParams(
				AppPreferences.ServerVariables.SCHEME,
				AppPreferences.ServerVariables.AUTHORITY, locationValues);
		return locationParams;

	}

	public static RequestParams helpParams(String path, String user_id) {
		String[] values = { "public", "index.php", path, user_id };
		RequestParams params = CommonFunctions.setParams(
				AppPreferences.ServerVariables.SCHEME,
				AppPreferences.ServerVariables.AUTHORITY, values);
		return params;

	}

	public static HashMap<String, String> gettingAuthValuesFromSharedPreferences(
			Context context) {
		HashMap<String, String> authHashMap = new HashMap<String, String>();
		SharedPreferences authPref = getSharedPreferences(context,
				AppPreferences.SharedPrefAuthentication.name);
		authHashMap
				.put(AppPreferences.SharedPrefAuthentication.user_email,
						authPref.getString(
								AppPreferences.SharedPrefAuthentication.user_email,
								""));
		authHashMap.put(AppPreferences.SharedPrefAuthentication.user_name,
				authPref.getString(
						AppPreferences.SharedPrefAuthentication.user_name, ""));
		authHashMap.put(AppPreferences.SharedPrefAuthentication.password,
				authPref.getString(
						AppPreferences.SharedPrefAuthentication.password, ""));

		return authHashMap;
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
			mGcm = GoogleCloudMessaging.getInstance(context);
			mRegid = getRegistrationId(context);
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
		SharedPreferences.Editor edit = CommonFunctions.getSharedPreferences(
				context, AppPreferences.SharedPrefActivityRecognition.name)
				.edit();
		edit.putBoolean(AppPreferences.SharedPrefActivityRecognition.enabled,
				true);
		edit.commit();
	}

	public boolean isForeground(String myPackage, Context context) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager
				.getRunningTasks(1);

		ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
		if (componentInfo.getPackageName().equals(myPackage)) {
			return true;
		} else {
			return false;
		}
	}

}
