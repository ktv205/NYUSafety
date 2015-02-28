package com.example.clickforhelp.controllers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class CommonFunctions {
	private static final String TAG = "CheckConnection";

	public static boolean isConnected(Context context) {
		Log.d(TAG,"isConnected");
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info != null && info.isConnected()) {
			return true;
		} else {
			return false;
		}
	}
}
