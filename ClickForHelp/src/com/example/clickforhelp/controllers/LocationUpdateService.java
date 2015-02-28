package com.example.clickforhelp.controllers;

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
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class LocationUpdateService extends Service implements
		OnConnectionFailedListener, ConnectionCallbacks, LocationListener {
	private static final String TAG = "LocationUpdateService";
	private GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;

	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		buildGoogleApiClient();
		return START_STICKY;
	};

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "in onBind");
		return null;
	}

	protected synchronized void buildGoogleApiClient() {
		Log.d(TAG, "in buildGoogleApiClient");
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
	public void onLocationChanged(Location arg0) {
		Log.d(TAG, "in onLocationChanged");

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

}