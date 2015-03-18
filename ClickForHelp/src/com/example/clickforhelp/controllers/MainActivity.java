package com.example.clickforhelp.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.clickforhelp.R;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.LocationDetailsModel;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
		OnMapReadyCallback, OnConnectionFailedListener,
		com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks {
	private final static String TAG = "MainActivity";

	private ArrayList<Marker> markers;
	private Context context;

	private MapFragment mapFragment;
	private GoogleMap mMap;
	private Location mLastLocation;
	private GoogleApiClient mGoogleApiClient;

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";

	private IntentFilter intentFilter;
	private BroadcastReceiver mReceiver;
	private Intent sendLocationIntentService;

	String SENDER_ID = "947264921784";
	TextView mDisplay;
	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	String regid;
	AlarmManager alarmManager;
	PendingIntent pendingIntent;
	Button helpButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Log.d(TAG, "in onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_map);
		context = getApplicationContext();
		if (CommonFunctions.isConnected(context)) {

			// GCM
			gcmServiceImplementation();

			// location broadcast receiver
			intentFilter = new IntentFilter(
					"com.example.clickforhelp.action_send");
			intentFilter.addCategory("com.example.clickforhelp");
			// markers
			markers = new ArrayList<Marker>();

		}
	}

	private void setLocationReceivingAlarm() {
		Log.d(TAG, "setLocationReceivingAlaram");
		alarmManager = (AlarmManager) getApplicationContext().getSystemService(
				Context.ALARM_SERVICE);
		Intent intent = new Intent(getApplicationContext(),
				ReceiveLocationService.class);
		if (getIntent() != null) {
			if (getIntent().hasExtra("coord")) {
				Log.d(TAG, "has coord");
				helpButton.setText("Helping a friend(click here after done)");
				intent.putExtra("coord",
						getIntent().getDoubleArrayExtra("coord"));
				intent.putExtra("userid",
						getIntent().getExtras().getString("userid"));

			} else {
				Log.d(TAG, "no coord");
			}
		}
		pendingIntent = PendingIntent.getService(this, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		try {
			alarmManager.cancel(pendingIntent);
		} catch (Exception e) {
			Log.d(TAG, "exception");
		}
		int timeForAlarm = 6000;

		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis() + timeForAlarm, timeForAlarm,
				pendingIntent);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d(TAG, "in new intent");
	}

	public boolean checkPluggedIn() {
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, ifilter);
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		Log.d(TAG, "battery_status->" + status);
		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;
		if (isCharging) {
			Log.d(TAG, "is cherging?->" + String.valueOf(isCharging));
			int chargePlug = batteryStatus.getIntExtra(
					BatteryManager.EXTRA_PLUGGED, -1);
			isCharging = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
		} else {
			isCharging = status == BatteryManager.BATTERY_STATUS_FULL;
		}

		return isCharging;
	}

	public boolean checkChargingLevel() {
		boolean level = false;
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, ifilter);
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
		if (status == BatteryManager.BATTERY_HEALTH_GOOD) {
			level = true;
		}
		return level;

	}

	@Override
	protected void onResume() {
		super.onResume();
		// map stuff
		initializeMapFields();
		// anonymous broadcastReceiver
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (markers.size() != 0) {
					for (int i = 0; i < markers.size(); i++) {
						markers.get(i).remove();
					}
				}
				Toast.makeText(MainActivity.this, "received",
						Toast.LENGTH_SHORT).show();
				ArrayList<LocationDetailsModel> locations = intent
						.getParcelableArrayListExtra("key");
				fillMap(locations);
			}
		};
		this.registerReceiver(mReceiver, intentFilter);

		// help button and accessing AskHelpAsyncTask
		helpButton = (Button) findViewById(R.id.button_help);
		helpButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (helpButton.getText().toString().equals("Ask For Help")) {
					Log.d(TAG, "Ask For Help");
					String[] values = {
							"public",
							"index.php",
							"askhelp",
							getSharedPreferences(
									AppPreferences.SharedPref.name,
									MODE_PRIVATE).getString(
									AppPreferences.SharedPref.user_email, "") };
					RequestParams params = CommonFunctions.setParams(
							AppPreferences.ServerVariables.SCHEME,
							AppPreferences.ServerVariables.AUTHORITY, values);
					new AskHelpAsyncTask().execute(params);
					helpButton.setText("Asked for help(click here when done)");
				} else {
					Log.d(TAG, "setting text back");
					helpButton.setText("Ask For Help");
					getIntent().setData(null);
					setIntent(null);
					alarmManager.cancel(pendingIntent);
					setLocationReceivingAlarm();
				}

			}

		});

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		int value = Integer.valueOf(pref.getString(
				getString(R.string.string_key_location_settings), "-1"));
		sendLocationIntentService = new Intent(this,
				LocationUpdateService.class);
		if (new CommonFunctions().isMyServiceRunning(
				LocationUpdateService.class, this)) {

		} else {

			startService(sendLocationIntentService);
		}

		// starting a service to send location updates to server
		if (value == 4) {
			Log.d(TAG, "value is 4");
			if (new CommonFunctions().isMyServiceRunning(
					LocationUpdateService.class, this)) {
				sendLocationIntentService.putExtra("stop", true);
				this.stopService(sendLocationIntentService);
			} else {

			}

		} else if (value == 3) {
			if (checkPluggedIn()) {

			} else {
				if (new CommonFunctions().isMyServiceRunning(
						LocationUpdateService.class, this)) {
					stopService(sendLocationIntentService);
				} else {

				}

			}

		} else if (value == 2) {
			if (new CommonFunctions().isMyServiceRunning(
					LocationUpdateService.class, this)) {
				stopService(sendLocationIntentService);
			} else {

			}
		} else {

		}

		// setting loction alaram
		setLocationReceivingAlarm();
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.unregisterReceiver(mReceiver);
		alarmManager.cancel(pendingIntent);
		Log.d(TAG, "alarm cancelled");

	}

	@Override
	protected void onStop() {
		super.onStop();
		mGoogleApiClient.disconnect();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// alarmManager.cancel(pendingIntent);

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
		// Log.d(TAG, "in gcmImp");
		if (checkPlayServices()) {
			// Log.d(TAG, "playServices available in gcmImp");
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getRegistrationId(context);
			// Log.d(TAG, "regId" + regid);
			if (regid.isEmpty()) {
				registerInBackground();
			}

		} else {
			// Log.d(TAG, "playServices not available in onCreate");
		}
	}

	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			// Log.i(TAG, "Registration not found.");
			return "";
		}

		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			// Log.i(TAG, "App version changed.");
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
		String[] values = {
				"public",
				"index.php",
				"updategcm",
				getSharedPreferences(AppPreferences.SharedPref.name,
						MODE_PRIVATE).getString(
						AppPreferences.SharedPref.user_email, ""), regid };
		RequestParams params = CommonFunctions.setParams(
				AppPreferences.ServerVariables.SCHEME,
				AppPreferences.ServerVariables.AUTHORITY, values);
		new SendGCMInfoAsyncTask().execute(params);
	}

	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		// Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	public boolean checkPlayServices() {
		// Log.d(TAG, "in checkPlayServices");
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				// Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		} else {
			return true;
		}
	}

	// Map and location stuff
	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API).build();
		mGoogleApiClient.connect();
	}

	private void initializeMapFields() {
		mapFragment = (MapFragment) getFragmentManager().findFragmentById(
				R.id.map);
		mapFragment.getMapAsync(this);
	}

	@Override
	public void onMapReady(GoogleMap arg0) {
		Log.d(TAG, "map is ready");
		mMap = arg0;
		mMap.setMyLocationEnabled(true);
		buildGoogleApiClient();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {

	}

	@Override
	public void onConnected(Bundle arg0) {
		mLastLocation = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);
		if (mLastLocation != null) {
			Log.d(TAG, "mLastLocation is not null");
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					mLastLocation.getLatitude(), mLastLocation.getLongitude()),
					16));
		} else {
			Log.d(TAG, "mLastLocation is null");
		}

	}

	@Override
	public void onConnectionSuspended(int arg0) {
	}

	public void fillMap(ArrayList<LocationDetailsModel> locations) {
		if (locations.size() == 0) {
			Toast.makeText(MainActivity.this, "no one is near you",
					Toast.LENGTH_SHORT).show();
		} else {
			for (int i = 0; i < locations.size(); i++) {
				Marker marker = mMap.addMarker(new MarkerOptions()
						.position(
								new LatLng(locations.get(i).getLatitude(),
										locations.get(i).getLongitude()))
						.title("friend")
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.friends)));
				markers.add(marker);
			}
		}

	}

	public class SendGCMInfoAsyncTask extends
			AsyncTask<RequestParams, Void, String> {

		@Override
		protected String doInBackground(RequestParams... params) {
			// return null;
			return new HttpManager().sendUserData(params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}

	}

	public class AskHelpAsyncTask extends
			AsyncTask<RequestParams, Void, String> {

		@Override
		protected String doInBackground(RequestParams... params) {

			return new HttpManager().sendUserData(params[0]);
		}

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

}
