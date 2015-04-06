package com.example.clickforhelp.controllers.services;

import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.models.AppPreferences;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class ActivityRecognitionService extends IntentService {
	private Context mContext;

	public ActivityRecognitionService() {
		super(ActivityRecognitionService.class.getSimpleName());
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
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
						if (activity == AppPreferences.SharedPrefActivityRecognition.STILL) {
							CommonFunctions
									.settingUserPreferenceLocationUpdates(
											mContext, activity);
						}
					}
				} else if (!serviceRunning && activityRunning) {

					if (intent
							.hasExtra(AppPreferences.IntentExtras.ActivityRecognitionService_EXTRA_MESSAGE)) {
						if (CommonFunctions
								.isActivityRunning(getApplicationContext())) {
							Messenger message = (Messenger) intent
									.getExtras()
									.get(AppPreferences.IntentExtras.ActivityRecognitionService_EXTRA_MESSAGE);
							try {
								Message msg = Message.obtain();
								if (activity != null) {
									Bundle bundle = new Bundle();
									bundle.putString(
											AppPreferences.IntentExtras.ActivityRecognitionService_EXTRA_MESSAGE,
											activity);
									msg.setData(bundle);
									message.send(msg);
								}

							} catch (RemoteException e) {
								e.printStackTrace();
							}
						}

					}

				} else if (!serviceRunning && !activityRunning) {
					if (activity != null) {
						if (activity == AppPreferences.SharedPrefActivityRecognition.WALKING
								|| activity == AppPreferences.SharedPrefActivityRecognition.VEHICLE) {
							CommonFunctions
									.settingUserPreferenceLocationUpdates(
											mContext, activity);
						}
					}
				}

			}

		}
	}
}
