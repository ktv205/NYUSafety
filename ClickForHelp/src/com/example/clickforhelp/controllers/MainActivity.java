package com.example.clickforhelp.controllers;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.clickforhelp.R;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.RequestParams;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

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
	private IntentFilter intentFilter;
	private BroadcastReceiver mReceiver;

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
			gcmServiceImplementation();
			intentFilter = new IntentFilter(
					"com.google.android.c2dm.intent.RECEIVE");
			intentFilter.addCategory("com.example.clickforhelp");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapFragment.getMapAsync(this);
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d(TAG, "received");
				String message = intent.getExtras().getString("message");
				Log.d(TAG, intent.getExtras().toString());
				if (message != null) {
					Log.d(TAG, "message->" + message);
					JSONObject obj = null;
					try {
						obj = new JSONObject(message);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

			}
		};

		this.registerReceiver(mReceiver, intentFilter);

	}

	@Override
	protected void onPause() {
		super.onPause();
		this.unregisterReceiver(mReceiver);
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

	// GCM STUFF
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

	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}

		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	private SharedPreferences getGCMPreferences(Context context) {

		return getSharedPreferences(MainActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}

	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
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
					sendRegistrationIdToBackend(regid);
					storeRegistrationId(context, regid);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
				}
				return msg;
			}

		}.execute();
	}

	private void sendRegistrationIdToBackend(String regid) {
		String[] values = { "public", "index.php", "updategcm", "useremail",
				regid };
		RequestParams params = CommonFunctions.setParams(
				AppPreferences.ServerVariables.SCHEME,
				AppPreferences.ServerVariables.AUTHORITY, values);
		new SendGCMInfoAsyncTask().execute(params);
	}

	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
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

	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
		mGoogleApiClient.connect();
	}

	// MAP stuff
	private void initializeMapFields() {
		mapFragment = (MapFragment) getFragmentManager().findFragmentById(
				R.id.map);
		mapFragment.getMapAsync(this);
	}

	@Override
	public void onMapReady(GoogleMap arg0) {
		mMap = arg0;
		mMap.setMyLocationEnabled(true);
		if (mLastLocation != null && mMap != null) {
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					mLastLocation.getLatitude(), mLastLocation.getLongitude()),
					14));
		} else {

		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {

	}

	@Override
	public void onConnected(Bundle arg0) {
		mLastLocation = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);
		if (mLastLocation != null && mMap != null) {
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					mLastLocation.getLatitude(), mLastLocation.getLongitude()),
					16));
			String[] locationValues = {
					"public",
					"index.php",
					"updatelocation",
					getSharedPreferences(AppPreferences.SharedPref.name,
							MODE_PRIVATE).getString(
							AppPreferences.SharedPref.user_email, ""),
					String.valueOf(mLastLocation.getLatitude()),
					String.valueOf(mLastLocation.getLongitude()) };
			RequestParams locationParams = CommonFunctions.setParams(
					AppPreferences.ServerVariables.SCHEME,
					AppPreferences.ServerVariables.AUTHORITY, locationValues);
			new SendLocationsAsyncTask().execute(locationParams);

		} else {
		}
		String[] values = {
				"public",
				"index.php",
				"home",
				getSharedPreferences(AppPreferences.SharedPref.name,
						MODE_PRIVATE).getString(
						AppPreferences.SharedPref.user_email, "") };
		RequestParams params = CommonFunctions.setParams(
				AppPreferences.ServerVariables.SCHEME,
				AppPreferences.ServerVariables.AUTHORITY, values);
		new GetLocationOfPeers().execute(params);

	}

	@Override
	public void onConnectionSuspended(int arg0) {
	}

	// asnyc tasks
	public class GetLocationOfPeers extends
			AsyncTask<RequestParams, Void, String> {

		@Override
		protected String doInBackground(RequestParams... params) {
			return new HttpManager().sendUserData(params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			Log.d(TAG, "in onPostExecuted");
			mMap.addMarker(new MarkerOptions()
					.position(new LatLng(40.694296, -73.986164))
					.title("peer")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.custommarker)));
			mMap.addMarker(new MarkerOptions()
					.position(new LatLng(40.694270, -73.986150))
					.title("peer")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.custommarker)));
			mMap.addMarker(new MarkerOptions()
					.position(new LatLng(40.694100, -73.986200))
					.title("peer")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.custommarker)));
			mMap.addMarker(new MarkerOptions()
					.position(new LatLng(40.694350, -73.986250))
					.title("peer")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.custommarker)));
			mMap.addMarker(new MarkerOptions()
					.position(new LatLng(40.6907870, -73.987409))
					.title("peer")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.custommarker)));
			mMap.addMarker(new MarkerOptions()
					.position(new LatLng(40.690855, -73.9885060))
					.title("peer")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.custommarker)));

		}

	}

	public class SendGCMInfoAsyncTask extends
			AsyncTask<RequestParams, Void, String> {

		@Override
		protected String doInBackground(RequestParams... params) {
			return null;
			// return new HttpManager().sendUserData(params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}

	}

	public class SendLocationsAsyncTask extends
			AsyncTask<RequestParams, Void, String> {

		@Override
		protected String doInBackground(RequestParams... params) {
			return null;
			// return new HttpManager().sendUserData(params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}

	}
}
