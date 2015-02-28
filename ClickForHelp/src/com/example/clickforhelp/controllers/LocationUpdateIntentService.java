package com.example.clickforhelp.controllers;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class LocationUpdateIntentService extends IntentService implements
		OnConnectionFailedListener, ConnectionCallbacks, LocationListener {
	private static final String TAG = "LocationUpdateIntentService";
	private GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;

	public LocationUpdateIntentService() {
		super("Location Intent Service");

	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "in onHandle");
		Toast.makeText(LocationUpdateIntentService.this, "onHandle",
				Toast.LENGTH_SHORT).show();
		buildGoogleApiClient();

	}

	protected synchronized void buildGoogleApiClient() {
		Toast.makeText(this, "buildGoogleApiClient", Toast.LENGTH_SHORT).show();
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
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	@Override
	public void onConnected(Bundle arg0) {
		Log.d(TAG, "in onConnected");
		startLocationUpdates();

	}

	protected void startLocationUpdates() {
		Log.d(TAG, "in startLocationUpdates");
		LocationServices.FusedLocationApi.requestLocationUpdates(
				mGoogleApiClient, mLocationRequest, this);
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationChanged(Location arg0) {
		Log.d(TAG, "in onLocationChanged");

	}

}
