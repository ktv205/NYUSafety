package com.example.clickforhelp.controllers.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.example.clickforhelp.controllers.services.LocationUpdateService;
import com.example.clickforhelp.models.AppPreferences;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class InternetConnectionAsyncTask extends AsyncTask<Void, Void, Boolean> {
	private final static String TAG = InternetConnectionAsyncTask.class
			.getSimpleName();
	private Context mContext;
	private InternetConntection mInternetConntection;
	private String mActivityTye;

	public InternetConnectionAsyncTask(Context context,String activityType) {
		mContext = context;
		mActivityTye=activityType;

	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.d(TAG,"onPreexecute");
	}

	public interface InternetConntection {
		public void isConnected(boolean connected);
	}

	public InternetConnectionAsyncTask(Context context, boolean activity) {
		Log.d(TAG,"in construtor");
		mContext = context;
		try {
			mInternetConntection = (InternetConntection) mContext;
		} catch (ClassCastException e) {
			throw new ClassCastException(mContext.toString()
					+ " must implement OnHeadlineSelectedListener");
		}

	}

	@Override
	protected Boolean doInBackground(Void... params) {
		boolean connected = false;
		try {
			HttpURLConnection urlc = (HttpURLConnection) (new URL(
					"http://clients3.google.com/generate_204").openConnection());
			urlc.setRequestProperty("User-Agent", "Android");
			urlc.setRequestProperty("Connection", "close");
			urlc.setConnectTimeout(1500);
			urlc.connect();
			Log.d(TAG, "connected");
			connected = true;
			return (urlc.getResponseCode() == 204 && urlc.getContentLength() == 0);
		} catch (IOException e) {
			Log.d(TAG, "Error checking internet connection");
		}
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		Log.d(TAG, "result->" + String.valueOf(result));
		if (mInternetConntection == null) {
			if (result == true) {
				AppPreferences.tryConnectionCounter = 0;
				CommonFunctions.settingUserPreferenceLocationUpdates(mContext,mActivityTye);
			} else {
				if (AppPreferences.tryConnectionCounter == 0) {
					new Thread(new Runnable() {
						public void run() {
							try {
								Thread.sleep(10000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							new InternetConnectionAsyncTask(mContext,mActivityTye);
						}
					}).start();
					AppPreferences.tryConnectionCounter++;
				}
				if (CommonFunctions.isMyServiceRunning(
						LocationUpdateService.class, mContext)) {
					mContext.stopService(new Intent(mContext,
							LocationUpdateService.class));
				}
			}
		} else {
			AppPreferences.tryConnectionCounter = 0;
			mInternetConntection.isConnected(result);
		}
	}

}
