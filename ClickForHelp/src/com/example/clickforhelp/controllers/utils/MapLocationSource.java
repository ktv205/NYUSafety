package com.example.clickforhelp.controllers.utils;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.LocationSource;

public class MapLocationSource implements LocationSource, LocationListener,
		ConnectionCallbacks, OnConnectionFailedListener {
	Context context;
	boolean highAccuracy = false;
	OnLocationChangedListener listener;
	GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;

	public MapLocationSource(Context context, boolean highAccuracy) {
		this.context = context;
		this.highAccuracy = highAccuracy;
		buildGoogleApi();
	}

	public MapLocationSource(Context context) {
		this.context = context;
		buildGoogleApi();
	}

	public void buildGoogleApi() {
		mGoogleApiClient = new GoogleApiClient.Builder(context)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
		mGoogleApiClient.connect();

	}

	@Override
	public void onLocationChanged(Location location) {
		if (listener != null) {
			listener.onLocationChanged(location);
		}
	}

	@Override
	public void activate(OnLocationChangedListener listener) {
		this.listener = listener;
	}

	@Override
	public void deactivate() {
		mGoogleApiClient.disconnect();

	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(10000);

		if (highAccuracy) {
			mLocationRequest.setFastestInterval(3000);
			mLocationRequest
					.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		} else {
			mLocationRequest.setFastestInterval(5000);
			mLocationRequest
					.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		}
		LocationServices.FusedLocationApi.requestLocationUpdates(
				mGoogleApiClient, mLocationRequest, this);
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub

	}

}
