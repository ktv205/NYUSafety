package com.example.clickforhelp.controllers.ui;

import java.util.ArrayList;

import com.example.clickforhelp.controllers.services.ActivityRecognitionService;
import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.SendLocationsAsyncTask;
import com.example.clickforhelp.controllers.utils.SendLocationsAsyncTask.GetOtherUsersLocations;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.LocationDetailsModel;
import com.example.clickforhelp.models.RequestParams;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.example.clickforhelp.R;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements OnMapReadyCallback,
		OnConnectionFailedListener,
		com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks,
		LocationListener, LocationSource, OnMapLoadedCallback,
		GetOtherUsersLocations {

	// TAG for debugging
	private static final String TAG = MainActivity.class.getSimpleName();

	// application Context;
	private Context mContext;

	// google map
	private GoogleMap mGoogleMap;

	// google api client
	private GoogleApiClient mGoogleApiClient;

	// related to activity recognition
	private boolean enabled = false;

	// related to locations retrieved accuracy
	private boolean highAccuracy = false;

	// user email to be sent to server that is retreived from sharedpreferences
	private String user_email;

	// locations
	private LocationRequest mLocationRequest;
	private OnLocationChangedListener mLocationChangedListener;
	private Location mLastLocation;
	private ArrayList<Marker> mMarkers;

	// location broadcast receiver
	private IntentFilter mIntentFilter;
	private BroadcastReceiver mReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// setting the context
		mContext = getApplicationContext();
		if (CommonFunctions.isConnected(mContext)) {
			setContentView(R.layout.test_map);

			// setting up map from where we also set googlemapapi
			initializeMap();

			// getting user email to be sent to server in various calls to
			// server
			getUserEmail();

			// setting a broadcast listener for locations of other users
			setFilterAndCategoryForLocationReceiver();
			registerLocationReceiverBroadcastListener();

			// Markers
			mMarkers = new ArrayList<Marker>();

		} else {
			setNoConnectionView();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mGoogleApiClient.disconnect();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// LocationSource interface methods
	@Override
	public void activate(OnLocationChangedListener arg0) {
		Toast.makeText(mContext, "in onLocationChangedListener",
				Toast.LENGTH_SHORT).show();
		mLocationChangedListener = arg0;

	}

	@Override
	public void deactivate() {
		mLocationChangedListener = null;
	}

	// LocationListner interface method
	@Override
	public void onLocationChanged(Location location) {
		Toast.makeText(
				mContext,
				CommonFunctions
						.getSharedPreferences(
								mContext,
								AppPreferences.SharedPrefActivityRecognition.activityType)
						.getString(
								AppPreferences.SharedPrefActivityRecognition.type,
								AppPreferences.SharedPrefActivityRecognition.WALKING),
				Toast.LENGTH_SHORT).show();
		if (mLocationChangedListener != null && location!=null) {
			mLocationChangedListener.onLocationChanged(location);
			RequestParams locationParams = CommonFunctions
					.buildLocationUpdateParams(user_email,
							location.getLatitude(), location.getLongitude());
			if (CommonFunctions.isConnected(this)) {
				new SendLocationsAsyncTask(this).execute(locationParams);
			}
		}

	}

	// Google api methods
	@Override
	public void onConnected(Bundle arg0) {
		settingUpActivityRecognition();
		settingUpMapLocationSource();
		fillMap();
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	// Google map OnMapReadyMethod
	@Override
	public void onMapReady(GoogleMap arg0) {
		mGoogleMap = arg0;
		mGoogleMap.setOnMapLoadedCallback(this);

	}

	@Override
	public void onMapLoaded() {
		Toast.makeText(mContext, "mapLoaded", Toast.LENGTH_SHORT).show();

		mGoogleMap.setMyLocationEnabled(true);
		mGoogleMap.setLocationSource(this);
		buildGoogleApiClient();

	}

	@Override
	public void getData(LocationDetailsModel locations) {
		Toast.makeText(mContext, "do something here", Toast.LENGTH_SHORT)
				.show();

	}

	// user defined methods

	// method called from onCreate to set a no network connection view
	public void setNoConnectionView() {

		setContentView(R.layout.no_connection);

		// Button for retrying for network connection
		Button button = (Button) findViewById(R.id.no_connection_retry);

		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (CommonFunctions.isConnected(mContext)) {
					startActivity(new Intent(mContext, MainActivity.class));
					finish();
				}

			}
		});
	}

	public void initializeMap() {
		MapFragment mapFragment = (MapFragment) getFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	public void buildGoogleApiClient() {
		Builder builder = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this);
		enabled = CommonFunctions.getSharedPreferences(mContext,
				AppPreferences.SharedPrefActivityRecognition.name).getBoolean(
				AppPreferences.SharedPrefActivityRecognition.enabled, false);
		if (enabled) {
			// DoNothing already activity registration is done
		} else {
			builder.addApi(ActivityRecognition.API);
		}
		mGoogleApiClient = builder.build();
		mGoogleApiClient.connect();
	}

	public void settingUpActivityRecognition() {
		if (enabled) {
			Toast.makeText(this, "already enabled", Toast.LENGTH_SHORT).show();

		} else {
			Toast.makeText(mContext, "enabling", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(this, ActivityRecognitionService.class);
			PendingIntent callbackIntent = PendingIntent.getService(this, 0,
					intent, PendingIntent.FLAG_UPDATE_CURRENT);
			PendingResult<Status> result = ActivityRecognition.ActivityRecognitionApi
					.requestActivityUpdates(mGoogleApiClient, // your connected
																// GoogleApiClient
							300000, // how often you want callbacks
							callbackIntent); // the PendingIntent which will
			// receive updated activities
			result.setResultCallback(new ResultCallback<Status>() {
				@Override
				public void onResult(Status status) {
					if (status.isSuccess()) {
						CommonFunctions
								.saveActivityRecognitionPreference(mContext);
					}
				}
			});
		}
	}

	public void settingUpMapLocationSource() {
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

	public void getUserEmail() {
		user_email = CommonFunctions.getSharedPreferences(mContext,
				AppPreferences.SharedPrefAuthentication.name).getString(
				AppPreferences.SharedPrefAuthentication.user_email, "");
	}

	public void fillMap() {
		mLastLocation = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);

		if (mLastLocation != null) {
			// Log.d(TAG, "mLastLocation is not null");
			mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					mLastLocation.getLatitude(), mLastLocation.getLongitude()),
					16));
		}

	}

	public void setFilterAndCategoryForLocationReceiver() {
		mIntentFilter = new IntentFilter("com.example.clickforhelp.action_send");
		mIntentFilter.addCategory("com.example.clickforhelp");
	}

	public void registerLocationReceiverBroadcastListener() {
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d(TAG, "in onReceive of broadcast receiver");
			}
		};
		this.registerReceiver(mReceiver, mIntentFilter);
	}

}
