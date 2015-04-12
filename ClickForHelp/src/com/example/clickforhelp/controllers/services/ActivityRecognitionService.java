package com.example.clickforhelp.controllers.services;

import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.models.AppPreferences;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.test.ActivityTestCase;
import android.util.Log;

public class ActivityRecognitionService extends Service {
	private Context mContext;
	private final static String TAG = ActivityRecognitionService.class
			.getSimpleName();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		if (mContext == null) {
			mContext = getApplicationContext();
		}
		String activity = null;
		if (intent != null) {
			ActivityRecognitionResult result = ActivityRecognitionResult
					.extractResult(intent);
			if (result != null) {
				DetectedActivity detectedActivity = result
						.getMostProbableActivity();
				if (detectedActivity.getType() == DetectedActivity.IN_VEHICLE) {
					activity = AppPreferences.SharedPrefActivityRecognition.VEHICLE;
				} else if (detectedActivity.getType() == DetectedActivity.ON_FOOT) {
					activity = AppPreferences.SharedPrefActivityRecognition.WALKING;
				} else if (detectedActivity.getType() == DetectedActivity.RUNNING) {
					activity = AppPreferences.SharedPrefActivityRecognition.WALKING;
				} else if (detectedActivity.getType() == DetectedActivity.STILL) {
					activity = AppPreferences.SharedPrefActivityRecognition.STILL;
				} else if (detectedActivity.getType() == DetectedActivity.ON_BICYCLE) {
					activity = AppPreferences.SharedPrefActivityRecognition.WALKING;
				} else if (detectedActivity.getType() == DetectedActivity.UNKNOWN) {

				} else if (detectedActivity.getType() == DetectedActivity.WALKING) {
					activity = AppPreferences.SharedPrefActivityRecognition.WALKING;
				} else if (detectedActivity.getType() == DetectedActivity.TILTING) {

				}
				boolean serviceRunning = CommonFunctions.isMyServiceRunning(
						LocationUpdateService.class, mContext);
				boolean activityRunning = CommonFunctions
						.isActivityRunning(mContext);
				if (serviceRunning && !activityRunning) {
					if (activity != null) {
						if (activity
								.equals(AppPreferences.SharedPrefActivityRecognition.STILL)) {
							CommonFunctions
									.settingUserPreferenceLocationUpdates(
											mContext, activity);
						}
					}
				} else if (!serviceRunning && activityRunning) {
					Intent sendIntent = new Intent();
					sendIntent
							.setAction("com.example.clickforhelp.controllers.ui.action_send");
					sendIntent
							.putExtra(
									AppPreferences.IntentExtras.ActivityRecognitionService_EXTRA_MESSAGE,
									activity);
					sendBroadcast(sendIntent);

				} else if (!serviceRunning && !activityRunning) {
					if (activity != null) {
						if (activity
								.equals(AppPreferences.SharedPrefActivityRecognition.WALKING)
								|| activity
										.equals(AppPreferences.SharedPrefActivityRecognition.VEHICLE)) {
							CommonFunctions
									.settingUserPreferenceLocationUpdates(
											mContext, activity);
						}
					}
				}

			}

		}

		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
