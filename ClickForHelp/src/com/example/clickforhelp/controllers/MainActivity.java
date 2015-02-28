package com.example.clickforhelp.controllers;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.clickforhelp.R;
import com.example.clickforhelp.R.drawable;
import com.example.clickforhelp.R.id;
import com.example.clickforhelp.R.layout;
import com.example.clickforhelp.R.menu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
		OnMapReadyCallback, OnConnectionFailedListener,
		com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks {
	private Context context;
	private MapFragment mapFragment;
	private GoogleMap mMap;
	private final static String TAG = "MainActivity";
	private GoogleApiClient mGoogleApiClient;
	private Location mLastLocation;
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	/**
	 * Substitute you own sender ID here. This is the project number you got
	 * from the API Console, as described in "Getting Started."
	 */
	String SENDER_ID = "947264921784";
	TextView mDisplay;
	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	SharedPreferences prefs;

	String regid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "in onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_map);
		context = getApplicationContext();
		if (CommonFunctions.isConnected(context)) {
			buildGoogleApiClient();
			initializeMapFields();
			// Intent serviceIntent = new Intent(this,
			// LocationUpdateService.class);
			// startService(serviceIntent);
			buildANotification();
			gcmServiceImplementation();

		}
	}

	public void gcmServiceImplementation() {
		context = getApplicationContext();
		Log.d(TAG, "in gcmImp");
		if (checkPlayServices()) {
			Log.d(TAG, "playServices available in gcmImp");
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getRegistrationId(context);
			Log.d(TAG, "regId" + regid);
			if (regid.isEmpty()) {
				registerInBackground();
			}

		} else {
			Log.d(TAG, "playServices not available in onCreate");
		}
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing registration ID is not guaranteed to work with
		// the new app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences,
		// but
		// how you store the registration ID in your app is up to you.
		return getSharedPreferences(MainActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	public boolean checkPlayServices() {
		Log.d(TAG, "in checkPlayServices");
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		} else {
			return true;
		}
	}

	public void buildANotification() {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Help Required")
				.setContentText("Some one nearby is in trouble");
		Intent resultIntent = new Intent(this, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(10, mBuilder.build());
	}

	protected synchronized void buildGoogleApiClient() {
		Toast.makeText(this, "buildGoogleApiClient", Toast.LENGTH_SHORT).show();
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
		mGoogleApiClient.connect();
	}

	private void initializeMapFields() {
		Toast.makeText(this, "intializeMapFields", Toast.LENGTH_SHORT).show();
		mapFragment = (MapFragment) getFragmentManager().findFragmentById(
				R.id.map);
		mapFragment.getMapAsync(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapFragment.getMapAsync(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onMapReady(GoogleMap arg0) {
		Toast.makeText(this, "onMapReady", Toast.LENGTH_SHORT).show();
		mMap = arg0;
		mMap.setMyLocationEnabled(true);
		if (mLastLocation != null && mMap != null) {
			Toast.makeText(this, "mLastLocation is not null",
					Toast.LENGTH_SHORT).show();
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					mLastLocation.getLatitude(), mLastLocation.getLongitude()),
					14));
			new GetLocationOfPeers().execute();

		} else {
			Toast.makeText(this, "mLastLocation is null", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Toast.makeText(this, "connection failed", Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onConnected(Bundle arg0) {
		Toast.makeText(this, "in onconnected of location services",
				Toast.LENGTH_SHORT).show();
		mLastLocation = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);
		if (mLastLocation != null && mMap != null) {
			Toast.makeText(this, "mLastLocation is not null",
					Toast.LENGTH_SHORT).show();
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					mLastLocation.getLatitude(), mLastLocation.getLongitude()),
					16));
			new GetLocationOfPeers().execute();

		} else {
			Toast.makeText(this, "mLastLocation is null", Toast.LENGTH_SHORT)
					.show();
		}

	}

	@Override
	public void onConnectionSuspended(int arg0) {
		Toast.makeText(this, "in onConnectionSuspended", Toast.LENGTH_SHORT)
				.show();

	}

	public void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					regid = gcm.register(SENDER_ID);
					msg = "Device registered, registration ID=" + regid;

					// You should send the registration ID to your server over
					// HTTP,
					// so it can use GCM/HTTP or CCS to send messages to your
					// app.
					// The request to your server should be authenticated if
					// your app
					// is using accounts.
					sendRegistrationIdToBackend();

					// For this demo: we don't need to send it because the
					// device
					// will send upstream messages to a server that echo back
					// the
					// message using the 'from' address in the message.

					// Persist the registration ID - no need to register again.
					storeRegistrationId(context, regid);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}
				return msg;
			}

		}.execute();
	}

	private void sendRegistrationIdToBackend() {
		// Your implementation here.
	}

	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	public class GetLocationOfPeers extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(MainActivity.this, "in onPostExecute",
					Toast.LENGTH_SHORT).show();
			Log.d(TAG, "in onPostExecuted");
			mMap.addMarker(new MarkerOptions().position(
					new LatLng(40.694296, -73.986164)).title("peer"));
			mMap.addMarker(new MarkerOptions().position(
					new LatLng(40.694270, -73.986150)).title("peer"));
			mMap.addMarker(new MarkerOptions().position(
					new LatLng(40.694100, -73.986200)).title("peer"));
			mMap.addMarker(new MarkerOptions().position(
					new LatLng(40.694350, -73.986250)).title("peer"));
			mMap.addMarker(new MarkerOptions().position(
					new LatLng(40.6907870, -73.987409)).title("peer"));
			mMap.addMarker(new MarkerOptions().position(
					new LatLng(40.690855, -73.9885060)).title("peer"));

		}

	}

}
