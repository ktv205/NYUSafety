package com.example.clickforhelp.controllers.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.clickforhelp.R;
import com.example.clickforhelp.controllers.services.ActivityRecognitionService;
import com.example.clickforhelp.controllers.services.LocationUpdateService;
import com.example.clickforhelp.controllers.services.ReceiveLocationService;
import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.HttpManager;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.LocationDetailsModel;
import com.example.clickforhelp.models.RequestParams;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnMapReadyCallback,
		OnConnectionFailedListener,
		com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks,
		LocationListener, LocationSource {
	// private final static String TAG = "MainActivity";
	private ArrayList<Marker> markers;
	private Context mContext;
	private MapFragment mMapFragment;
	private GoogleMap mMap;
	private Location mLastLocation;
	private GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";

	private IntentFilter mIntentFilter;
	private BroadcastReceiver mReceiver;
	private Intent mSendLocationIntentService;

	String SENDER_ID = "947264921784";
	TextView mDisplay;
	GoogleCloudMessaging mGcm;
	AtomicInteger mMsgId = new AtomicInteger();
	String mRegid;
	AlarmManager mAlarmManager;
	PendingIntent mPendingIntent;
	Button mHelpButton;

	private static final String ASK_HELP = "Ask For Help";
	private static final String ASKED_HELP = "Asked For Help(click here after receiving help)";
	private static final String HELPING = "Helping a Friend(click here after helping)";
	protected static final String TAG = "MainActivity";

	Animation mAnimation = null;

	OnLocationChangedListener mListener;

	boolean highAccuracy = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_map);
		mContext = getApplicationContext();
		buttonAnimation();
		if (CommonFunctions.isConnected(mContext)) {
			// GCM
			gcmServiceImplementation();

			// location broadcast receiver
			mIntentFilter = new IntentFilter(
					"com.example.clickforhelp.action_send");
			mIntentFilter.addCategory("com.example.clickforhelp");
			
			
			// markers
			markers = new ArrayList<Marker>();

		} else {
			setNoConnectionView();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (CommonFunctions.isConnected(mContext)) {
			// intent for starting location update service
			mSendLocationIntentService = new Intent(this,
					LocationUpdateService.class);
			if (CommonFunctions.isMyServiceRunning(LocationUpdateService.class,
					this)) {

			} else {

				startService(mSendLocationIntentService);
			}

			// map stuff
			initializeMapFields();

			// anonymous broadcastReceiver for the location of friends
			mReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					if (markers.size() != 0) {
						for (int i = 0; i < markers.size(); i++) {
							markers.get(i).remove();
						}
					}

					if (intent
							.hasExtra(AppPreferences.IntentExtras.NOCONNECTION)) {
						setNoConnectionView();
					} else {
						ArrayList<LocationDetailsModel> locations = intent
								.getParcelableArrayListExtra(AppPreferences.IntentExtras.LOCATIONS);
						fillMap(locations);
					}
				}
			};
			this.registerReceiver(mReceiver, mIntentFilter);

			// help button and accessing AskHelpAsyncTask
			mHelpButton = (Button) findViewById(R.id.button_help);
			mHelpButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mHelpButton.getText().toString().equals(ASK_HELP)) {
						mHelpButton.startAnimation(mAnimation);
						// Log.d(TAG, "asked for help");
						String[] values = {
								"public",
								"index.php",
								"askhelp",
								getSharedPreferences(
										AppPreferences.SharedPrefAuthentication.name,
										MODE_PRIVATE)
										.getString(
												AppPreferences.SharedPrefAuthentication.user_email,
												"") };
						RequestParams params = CommonFunctions.setParams(
								AppPreferences.ServerVariables.SCHEME,
								AppPreferences.ServerVariables.AUTHORITY,
								values);
						if (CommonFunctions.isMyServiceRunning(
								LocationUpdateService.class, mContext)) {
							stopService(new Intent(MainActivity.this,
									LocationUpdateService.class));
							Intent intent = new Intent(MainActivity.this,
									LocationUpdateService.class);
							intent.putExtra(
									AppPreferences.IntentExtras.HIGH_ACCURACY,
									true);
						}
						new AskHelpAsyncTask().execute(params);
						mHelpButton.setText(ASKED_HELP);
					} else if (mHelpButton.getText().toString()
							.equals(ASKED_HELP)) {
						mHelpButton.clearAnimation();
						// Log.d(TAG, "in help received");
						String[] values = {
								"public",
								"index.php",
								"helpreceived",
								getSharedPreferences(
										AppPreferences.SharedPrefAuthentication.name,
										MODE_PRIVATE)
										.getString(
												AppPreferences.SharedPrefAuthentication.user_email,
												"") };
						RequestParams params = CommonFunctions.setParams(
								AppPreferences.ServerVariables.SCHEME,
								AppPreferences.ServerVariables.AUTHORITY,
								values);
						new AskHelpAsyncTask().execute(params);
						mHelpButton.setText(ASK_HELP);
						mAlarmManager.cancel(mPendingIntent);
						setIntentToNull();
						setLocationReceivingAlarm();
					} else if (mHelpButton.getText().toString().equals(HELPING)) {
						mHelpButton.clearAnimation();
						mHelpButton.setText(ASK_HELP);
						mAlarmManager.cancel(mPendingIntent);
						setIntentToNull();
						setLocationReceivingAlarm();
					}

				}

			});

			// setting location alarm
			setLocationReceivingAlarm();
		}
	}

	public void buttonAnimation() {
		mAnimation = new AlphaAnimation(1, 0); // Change alpha from fully
												// visible
												// to invisible
		mAnimation.setDuration(500); // duration - half a second
		mAnimation.setInterpolator(new LinearInterpolator()); // do not alter
																// animation
																// rate
		mAnimation.setRepeatCount(Animation.INFINITE); // Repeat animation
														// infinitely
		mAnimation.setRepeatMode(Animation.REVERSE); // Reverse animation at the
														// end so the button
														// will
														// fade back in

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mReceiver != null) {
			this.unregisterReceiver(mReceiver);
		}
		if (mPendingIntent != null) {
			mAlarmManager.cancel(mPendingIntent);
		}
		if (mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// starting a service to send location updates to server
		if (CommonFunctions
				.getSharedPreferences(mContext,
						AppPreferences.SharedPrefAuthentication.name)
				.getString(AppPreferences.SharedPrefAuthentication.flag, "")
				.isEmpty()) {

		} else {
			CommonFunctions.settingUserPreferenceLocationUpdates(mContext);
		}
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

	// Utils
	private void setLocationReceivingAlarm() {
		mAlarmManager = (AlarmManager) getApplicationContext()
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(getApplicationContext(),
				ReceiveLocationService.class);
		if (getIntent() != null) {
			if (getIntent().hasExtra(AppPreferences.IntentExtras.COORDINATES)
					&& getIntent().hasExtra(AppPreferences.IntentExtras.USERID)) {
				mHelpButton.setText(HELPING);
				intent.putExtra(
						AppPreferences.IntentExtras.COORDINATES,
						getIntent().getDoubleArrayExtra(
								AppPreferences.IntentExtras.COORDINATES));
				intent.putExtra(
						AppPreferences.IntentExtras.USERID,
						getIntent().getExtras().getString(
								AppPreferences.IntentExtras.USERID));

			} else {
			}
		}
		mPendingIntent = PendingIntent.getService(this, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		try {
			mAlarmManager.cancel(mPendingIntent);
		} catch (Exception e) {
		}
		int timeForAlarm = 6000;

		mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis() + timeForAlarm, timeForAlarm,
				mPendingIntent);
	}

	public void setNoConnectionView() {
		setContentView(R.layout.no_connection);
		Button button = (Button) findViewById(R.id.no_connection_retry);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (CommonFunctions.isConnected(MainActivity.this)) {
					startActivity(new Intent(MainActivity.this,
							MainActivity.class));
					finish();
				}

			}
		});
	}

	// GCM STUFF
	public void gcmServiceImplementation() {
		mContext = getApplicationContext();
		if (checkPlayServices()) {
			mGcm = GoogleCloudMessaging.getInstance(this);
			mRegid = getRegistrationId(mContext);
			if (mRegid.isEmpty()) {
				registerInBackground();
			}

		} else {
		}
	}

	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			return "";
		}
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
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
					if (mGcm == null) {
						mGcm = GoogleCloudMessaging.getInstance(mContext);
					}
					mRegid = mGcm.register(SENDER_ID);
					msg = "Device registered, registration ID=" + mRegid;
					sendRegistrationIdToBackend(mRegid);
					storeRegistrationId(mContext, mRegid);
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
				getSharedPreferences(
						AppPreferences.SharedPrefAuthentication.name,
						MODE_PRIVATE).getString(
						AppPreferences.SharedPrefAuthentication.user_email, ""),
				regid };
		RequestParams params = CommonFunctions.setParams(
				AppPreferences.ServerVariables.SCHEME,
				AppPreferences.ServerVariables.AUTHORITY, values);
		new SendGCMInfoAsyncTask().execute(params);
	}

	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	public boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
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
				.addApi(LocationServices.API).addApi(ActivityRecognition.API)
				.build();
		mGoogleApiClient.connect();
	}

	private void initializeMapFields() {
		mMapFragment = (MapFragment) getFragmentManager().findFragmentById(
				R.id.map);
		mMapFragment.getMapAsync(this);
	}

	@Override
	public void onMapReady(GoogleMap arg0) {
		mMap = arg0;
		mMap.setMyLocationEnabled(true);
		mMap.setLocationSource(this);
		buildGoogleApiClient();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {

	}

	@Override
	public void onConnected(Bundle arg0) {

		if (CommonFunctions.getSharedPreferences(mContext,
				AppPreferences.SharedPrefActivityRecognition.name).getBoolean(
				AppPreferences.SharedPrefActivityRecognition.enabled, false)) {
			// Already enabled
		} else {
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
						// success of registration of activity registration
					}
				}
			});
			SharedPreferences.Editor edit = CommonFunctions
					.getSharedPreferences(mContext,
							AppPreferences.SharedPrefActivityRecognition.name)
					.edit();
			edit.putBoolean(
					AppPreferences.SharedPrefActivityRecognition.enabled, true);
			edit.commit();
		}

		mLastLocation = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);

		if (mLastLocation != null) {
			// Log.d(TAG, "mLastLocation is not null");
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					mLastLocation.getLatitude(), mLastLocation.getLongitude()),
					16));
			if (getIntent() != null) {
				if (getIntent().hasExtra(AppPreferences.IntentExtras.LOCATIONS)) {
					ArrayList<LocationDetailsModel> locations = getIntent()
							.getParcelableArrayListExtra(
									AppPreferences.IntentExtras.LOCATIONS);
					fillMap(locations);
					setIntentToNull();
				}
			}
		} else {
		}

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

	public void setIntentToNull() {
		if (getIntent() != null) {
			getIntent().setData(null);
			setIntent(null);
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
	}

	public void fillMap(ArrayList<LocationDetailsModel> locations) {
		if (locations.size() == 0) {
			// Toast.makeText(MainActivity.this, "no one is near you",
			// Toast.LENGTH_SHORT).show();
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
			return new HttpManager().sendUserData(params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}

	}

	public class AskHelpAsyncTask extends
			AsyncTask<RequestParams, Void, String> {
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(MainActivity.this);
			dialog.setTitle(AppPreferences.Others.LOADING);
			dialog.setMessage("Notifying your friends");
		}

		protected String doInBackground(RequestParams... params) {
			return new HttpManager().sendUserData(params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			dialog.dismiss();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void onLocationChanged(Location location) {
		if (mListener != null) {
			mListener.onLocationChanged(location);
		}

	}

	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;

	}

	@Override
	public void deactivate() {
		// Toast.makeText(this, "deactivated", Toast.LENGTH_SHORT).show();
		mListener = null;

	}

}
