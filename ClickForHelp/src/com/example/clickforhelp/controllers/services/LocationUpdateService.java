package com.example.clickforhelp.controllers.services;

import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.SendLocationsAsyncTask;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.RequestParams;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;

public class LocationUpdateService extends Service implements
		OnConnectionFailedListener, ConnectionCallbacks, LocationListener {
	// private static final String TAG = LocationUpdateService.class
	// .getSimpleName();
	private GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;
	public static final String SEND_SERVICE = "com.example.clickforhelp.controllers.LocationUpdateService";
	public boolean high_accuracy = false;
	private Context mContext;
	private static final String UPDATE = "u";
	private String activity;

	public int onStartCommand(Intent intent, int flags, int startId) {

		if (intent != null
				&& intent
						.hasExtra(AppPreferences.IntentExtras.ActivityRecognitionService_EXTRA_MESSAGE)) {
			activity = intent
					.getExtras()
					.getString(
							AppPreferences.IntentExtras.ActivityRecognitionService_EXTRA_MESSAGE);
			mContext = getApplicationContext();
			buildGoogleApiClient();

		} else {
			stopSelf();
		}

		return START_REDELIVER_INTENT;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
		mGoogleApiClient.connect();
		createLocationRequest();
	}

	protected void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(10000);
		mLocationRequest.setFastestInterval(5000);
		if (high_accuracy) {
			mLocationRequest
					.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		} else {
			mLocationRequest
					.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		}

	}

	@Override
	public void onConnected(Bundle arg0) {
		startLocationUpdates();

	}

	protected void startLocationUpdates() {
		LocationServices.FusedLocationApi.requestLocationUpdates(
				mGoogleApiClient, mLocationRequest, this);
	}

	@Override
	public void onConnectionSuspended(int arg0) {

	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Log.d(TAG, "mGoogleClientDisconnected");
		mGoogleApiClient.disconnect();
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// Log.d(TAG, "here in onLocationChanged");
		RequestParams locationParams = CommonFunctions
				.buildLocationUpdateParams(CommonFunctions.getEmail(mContext),
						arg0.getLatitude(), arg0.getLongitude(), new String[] {
								activity, UPDATE });
		if (CommonFunctions.isConnected(mContext)) {
			new SendLocationsAsyncTask().execute(locationParams);
			if (activity == AppPreferences.SharedPrefActivityRecognition.STILL) {
				stopSelf();
			}
		}

	}

}
