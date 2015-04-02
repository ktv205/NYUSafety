package com.example.clickforhelp.controllers.services;

import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.models.AppPreferences;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.widget.Toast;

public class ActivityRecognitionService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "activity recogniston service", Toast.LENGTH_SHORT)
				.show();
		ActivityRecognitionResult result = ActivityRecognitionResult
				.extractResult(intent);
		SharedPreferences locationUpdatePreferences = CommonFunctions
				.getSharedPreferences(
						getApplicationContext(),
						AppPreferences.SharedPrefActivityRecognition.activityType);
		SharedPreferences.Editor edit = locationUpdatePreferences.edit();
		if (result != null) {
			DetectedActivity detectedActivity = result
					.getMostProbableActivity();
			Toast.makeText(this, "type->" + detectedActivity.getType(),
					Toast.LENGTH_SHORT).show();
			if (detectedActivity.getType() == DetectedActivity.IN_VEHICLE) {
				// doThis();
				edit.putString(
						AppPreferences.SharedPrefActivityRecognition.type,
						AppPreferences.SharedPrefActivityRecognition.VEHICLE);

			} else if (detectedActivity.getType() == DetectedActivity.ON_FOOT) {
				// doThis();
				edit.putString(
						AppPreferences.SharedPrefActivityRecognition.type,
						AppPreferences.SharedPrefActivityRecognition.WALKING);
				Toast.makeText(
						this,
						"detectedActivity.getType()==DetectedActivity.ON_FOOT"
								+ detectedActivity.getType(),
						Toast.LENGTH_SHORT).show();
			} else if (detectedActivity.getType() == DetectedActivity.RUNNING) {
				// doThis();
				edit.putString(
						AppPreferences.SharedPrefActivityRecognition.type,
						AppPreferences.SharedPrefActivityRecognition.WALKING);
				Toast.makeText(
						this,
						"detectedActivity.getType()==DetectedActivity.RUNNING"
								+ detectedActivity.getType(),
						Toast.LENGTH_SHORT).show();
			} else if (detectedActivity.getType() == DetectedActivity.STILL) {
				edit.putString(
						AppPreferences.SharedPrefActivityRecognition.type,
						AppPreferences.SharedPrefActivityRecognition.STILL);
				if (CommonFunctions.isMyServiceRunning(
						LocationUpdateService.class, this)) {
					stopService(new Intent(this, LocationUpdateService.class));
				}
				Toast.makeText(
						this,
						"detectedActivity.getType()==DetectedActivity.STILL"
								+ detectedActivity.getType(),
						Toast.LENGTH_SHORT).show();
			} else if (detectedActivity.getType() == DetectedActivity.ON_BICYCLE) {
				// doThis();
				edit.putString(
						AppPreferences.SharedPrefActivityRecognition.type,
						AppPreferences.SharedPrefActivityRecognition.VEHICLE);
				Toast.makeText(
						this,
						"detectedActivity.getType()==DetectedActivity.ON_BICYCLE"
								+ detectedActivity.getType(),
						Toast.LENGTH_SHORT).show();

			} else if (detectedActivity.getType() == DetectedActivity.UNKNOWN) {
				// if (CommonFunctions.isMyServiceRunning(
				// LocationUpdateService.class, this)) {
				//
				// } else {
				// CommonFunctions.settingUserPreferenceLocationUpdates(this);
				// }
				Toast.makeText(
						this,
						"detectedActivity.getType()==DetectedActivity.UNKNOWN"
								+ detectedActivity.getType(),
						Toast.LENGTH_SHORT).show();
			} else if (detectedActivity.getType() == DetectedActivity.WALKING) {
				edit.putString(
						AppPreferences.SharedPrefActivityRecognition.type,
						AppPreferences.SharedPrefActivityRecognition.WALKING);
				//doThis();
				Toast.makeText(
						this,
						"detectedActivity.getType()==DetectedActivity.WALKING"
								+ detectedActivity.getType(),
						Toast.LENGTH_SHORT).show();
			} else if (detectedActivity.getType() == DetectedActivity.TILTING) {
				Toast.makeText(
						this,
						"detectedActivity.getType()==DetectedActivity.tilting"
								+ detectedActivity.getType(),
						Toast.LENGTH_SHORT).show();
			}
			edit.commit();

		}
		return super.onStartCommand(intent, flags, startId);
	}

	// public void doThis() {
	// if (CommonFunctions.isMyServiceRunning(LocationUpdateService.class,
	// this)) {
	//
	// } else {
	// CommonFunctions.settingUserPreferenceLocationUpdates(this);
	// }
	//
	// }

}
