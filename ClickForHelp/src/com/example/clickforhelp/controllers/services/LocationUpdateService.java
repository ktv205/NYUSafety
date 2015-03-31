package com.example.clickforhelp.controllers.services;

import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.HttpManager;
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
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

public class LocationUpdateService extends Service implements
		OnConnectionFailedListener, ConnectionCallbacks, LocationListener {
	//private static final String TAG = "LocationUpdateService";
	private GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;
	public static final String SEND_SERVICE = "com.example.clickforhelp.controllers.LocationUpdateService";
	public boolean high_accuracy = false;

	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			if (intent.hasExtra(AppPreferences.IntentExtras.HIGH_ACCURACY)) {
				high_accuracy = true;
			}
		}
		buildGoogleApiClient();
		return START_STICKY;
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
		String[] locationValues = {
				"public",
				"index.php",
				"updatelocation",
				getSharedPreferences(
						AppPreferences.SharedPrefAuthentication.name,
						MODE_PRIVATE).getString(
						AppPreferences.SharedPrefAuthentication.user_email, ""),
				String.valueOf(arg0.getLatitude()),
				String.valueOf(arg0.getLongitude()) };
		RequestParams locationParams = CommonFunctions.setParams(
				AppPreferences.ServerVariables.SCHEME,
				AppPreferences.ServerVariables.AUTHORITY, locationValues);
		if (CommonFunctions.isConnected(this)) {
			new SendLocationsAsyncTask().execute(locationParams);
		}

	}

	public class SendLocationsAsyncTask extends
			AsyncTask<RequestParams, Void, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(RequestParams... params) {
			return new HttpManager().sendUserData(params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			// Log.d(TAG, "in onPostexecute locationupdate");
			super.onPostExecute(result);
		}

	}

}
