package com.example.clickforhelp.controllers;

import java.util.HashMap;
import java.util.Map;

import com.example.clickforhelp.models.RequestParams;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

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

	public SharedPreferences getSharedPreferences(Context context, String name) {
		return context.getSharedPreferences(name, Context.MODE_PRIVATE);
	}

	public boolean saveInPreferences(Context context, String name,
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

	public boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
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

	public boolean validNyuEmail(String email) {
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

}
