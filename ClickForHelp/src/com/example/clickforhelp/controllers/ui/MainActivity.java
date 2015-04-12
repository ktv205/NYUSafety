package com.example.clickforhelp.controllers.ui;

import java.util.ArrayList;

import com.example.clickforhelp.controllers.services.LocationUpdateService;
import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.CommonResultAsyncTask;
import com.example.clickforhelp.controllers.utils.CommonResultAsyncTask.ServerResponse;
import com.example.clickforhelp.controllers.utils.SendLocationsAsyncTask;
import com.example.clickforhelp.controllers.utils.SendLocationsAsyncTask.GetOtherUsersLocations;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.LocationDetailsModel;
import com.example.clickforhelp.models.RequestParams;
import com.example.clickforhelp.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnMapReadyCallback,
		OnMapLoadedCallback, ConnectionCallbacks, OnConnectionFailedListener,
		LocationListener, LocationSource, ServerResponse,
		GetOtherUsersLocations {

	// TAG for debugging
	private static final String TAG = MainActivity.class.getSimpleName();

	// Application context to be used through out the activity
	private Context mContext;

	// Get a reference to map Fragment
	private MapFragment mMapFragment;

	// GoogleMap object
	private GoogleMap mGoogleMap;

	// googleapiclient object
	private GoogleApiClient mGoogleApiClient;

	// boolean to check and set location reqeust to be high accuracy or low
	// accuracy
	private static boolean mIsHighAccuracy = false;

	// mLocationRequest object
	private LocationRequest mLocationRequest;

	// OnLocationChangeListener object
	private OnLocationChangedListener mOnLocationChangeListener;

	// Markers object
	private ArrayList<Marker> mMarkers;

	// people text view
	private TextView mPeopleTextView;

	// help button
	private Button mHelpButton;

	// paths for server urls
	private static final String ASK_HELP_PATH = "askhelp";
	private static final String HELPED_PATH = "helped";
	private static final String HELP_RECEIVED_PATH = "helpreceived";

	// help button texts and flags
	private static final String ASK_HELP = "Ask For Help";
	private static final String ASKED_HELP = "Asked For Help(click here after receiving help)";
	private static final String HELPING = "Helping a Friend(click here after helping)";

	private static final int ASK_HELP_FLAG = 0;
	private static final int ASKED_HELP_FLAG = 1;
	private static final int HELPING_FLAG = 2;
	private static int mHelpFlag = 0;

	private final static String ASK_HELP_TEXT = "notifying nearby people";
	private final static String HELPED_TEXT = "notifying others";

	// Animation object
	private AlphaAnimation mAnimation;

	// userEmail
	private String mUserEmail = "example@nyu.edu";

	// type of update location
	private static final String UPDATE_HOME = "uh";
	private static final String UPDATE = "u";

	// paths for helperlist,victimlist
	private static final String HELPER_LIST = "helperlist";
	private static final String TRACK_VICTIM = "trackvictim";
	// private static final String HOME = "home";

	// victim useremail
	private String mVictimUserEmail = "example@nyu.edu";

	// Key for savedInstance of flag
	private final static String KEY_STATE = "state_of_user";

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (mContext == null) {
			mContext = getApplicationContext();
		}

		if (CommonFunctions.isConnected(mContext)) {
			if (CommonFunctions.checkLoggedIn(mContext)) {
				setContentView(R.layout.test_map);

				// getting the userEmail
				mUserEmail = CommonFunctions.getEmail(mContext);

				// Markers
				mMarkers = new ArrayList<Marker>();

				// animating button
				buttonAnimation();

				// initialize button and textview
				acessViews();

				// InitializeMap
				initializeMap();

				// get Intent from the notification
				Intent intent = getIntent();
				if (intent != null) {
					retriveIntentExtras(intent);
				}

			} else {
				startActivity(new Intent(mContext, AuthenticationActivity.class));
				finishAffinity();
			}

		} else {
			setNoConnectionView();

		}

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mPeopleTextView.setText("searching..");
		if (savedInstanceState != null) {
			Log.d(TAG, "savedInstanceState is not null");
			int flag = savedInstanceState.getInt(KEY_STATE);
			settingTextOfButton(flag);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (CommonFunctions.isMyServiceRunning(LocationUpdateService.class,
				mContext)) {
			stopService(new Intent(mContext, LocationUpdateService.class));
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(KEY_STATE, mHelpFlag);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mGoogleApiClient != null) {
			mGoogleApiClient = null;
		}
		if (CommonFunctions.isMyServiceRunning(LocationUpdateService.class,
				mContext)) {
		} else {
			CommonFunctions
					.settingUserPreferenceLocationUpdates(mContext, null);
		}
	}

	// creating and accessing menu options
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);

		} else if (id == R.id.action_deactivate) {
			Log.d(TAG, "deactivate clicked");
			this.deactivate();
			mGoogleMap.setMyLocationEnabled(false);
			LocationServices.FusedLocationApi.removeLocationUpdates(
					mGoogleApiClient, this);
		} else if (id == R.id.action_activate) {
			Log.d(TAG, "activate clicked");
			this.activate(mOnLocationChangeListener);
			mGoogleMap.setMyLocationEnabled(true);
			settingUpMapLocationSource();

		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.android.gms.maps.OnMapReadyCallback#onMapReady(com.google.
	 * android.gms.maps.GoogleMap)
	 */

	// call back method from the interface onMapReady()

	@Override
	public void onMapReady(GoogleMap arg0) {
		mGoogleMap = arg0;
		mGoogleMap.setOnMapLoadedCallback(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback#onMapLoaded()
	 */

	// call back method from the interface onMapLoaded

	@Override
	public void onMapLoaded() {
		mGoogleMap.setMyLocationEnabled(true);
		mGoogleMap.setLocationSource(this);
		buildGoogleApiClient();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
	 * #onConnectionFailed(com.google.android.gms.common.ConnectionResult)
	 */

	// callbacks for the googleapiclient

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
	 * #onConnected(android.os.Bundle)
	 */

	@Override
	public void onConnected(Bundle arg0) {
		settingUpMapLocationSource();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
	 * #onConnectionSuspended(int)
	 */

	@Override
	public void onConnectionSuspended(int arg0) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.android.gms.maps.LocationSource#activate(com.google.android
	 * .gms.maps.LocationSource.OnLocationChangedListener)
	 */

	// call backs for location source interface

	@Override
	public void activate(OnLocationChangedListener arg0) {
		mOnLocationChangeListener = arg0;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.android.gms.maps.LocationSource#deactivate()
	 */

	@Override
	public void deactivate() {
		mOnLocationChangeListener = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.android.gms.location.LocationListener#onLocationChanged(android
	 * .location.Location)
	 */

	// call back for interface location listener

	@Override
	public void onLocationChanged(Location arg0) {
		Log.d(TAG, "in onLocationChanged");
		if (mOnLocationChangeListener != null && arg0 != null) {
			mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(arg0
					.getLatitude(), arg0.getLongitude())));
			mOnLocationChangeListener.onLocationChanged(arg0);
			RequestParams updateLocationParams = null, helpParams = null, helpingParams = null;
			double lat = arg0.getLatitude();
			double lng = arg0.getLongitude();
			if (mHelpFlag == ASK_HELP_FLAG) {
				updateLocationParams = CommonFunctions
						.buildLocationUpdateParams(
								mUserEmail,
								lat,
								lng,
								new String[] {
										AppPreferences.SharedPrefActivityRecognition.WALKING,
										UPDATE_HOME });

			} else if (mHelpFlag == ASKED_HELP_FLAG) {
				updateLocationParams = CommonFunctions
						.buildLocationUpdateParams(
								mUserEmail,
								lat,
								lng,
								new String[] {
										AppPreferences.SharedPrefActivityRecognition.WALKING,
										UPDATE });
				helpParams = CommonFunctions.setParams(new String[] {
						HELPER_LIST, mUserEmail });

			} else if (mHelpFlag == HELPING_FLAG) {
				updateLocationParams = CommonFunctions
						.buildLocationUpdateParams(
								mUserEmail,
								lat,
								lng,
								new String[] {
										AppPreferences.SharedPrefActivityRecognition.WALKING,
										UPDATE });
				helpingParams = CommonFunctions.setParams(new String[] {
						TRACK_VICTIM, mUserEmail, mVictimUserEmail });

			}

			if (helpParams != null) {
				new SendLocationsAsyncTask().execute(updateLocationParams);
				new SendLocationsAsyncTask(this).execute(helpParams);
			} else if (helpingParams != null) {
				new SendLocationsAsyncTask().execute(updateLocationParams);
				new SendLocationsAsyncTask(this).execute(helpingParams);
			} else {
				new SendLocationsAsyncTask(this).execute(updateLocationParams);
			}
		}
	}

	/*
	 * call back method of interface ServerResponse in CommonResultAsyncTask
	 */
	@Override
	public void IntegerResponse(int response, int flag) {
		if (flag == ASK_HELP_FLAG) {

		} else if (flag == ASKED_HELP_FLAG) {

		} else if (flag == HELPING_FLAG) {

		}

	}

	/*
	 * get other user location. This is a callback for the interface in the
	 * sendlocations async task
	 */
	@Override
	public void getData(ArrayList<LocationDetailsModel> arrayList) {
		fillMap(arrayList);

	}

	@Override
	public void onBackPressed() {
		if (mGoogleApiClient != null) {
			stopLocationUpdates();
			mGoogleApiClient = null;
		}
		super.onBackPressed();

	}

	// user defined methods

	/*
	 * method called from onCreate to set a no network connection view
	 */

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

	// get a reference to the map id from the xml and connect to map object by
	// calling getMapAsync
	public void initializeMap() {
		mMapFragment = (MapFragment) getFragmentManager().findFragmentById(
				R.id.map);
		mMapFragment.getFragmentManager().findFragmentById(R.id.map);
		mMapFragment.getMapAsync(this);
	}

	// building the googleapi
	public void buildGoogleApiClient() {
		Builder builder = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this);

		mGoogleApiClient = builder.build();
		mGoogleApiClient.connect();
	}

	// setting up location request
	public void settingUpMapLocationSource() {
		mLocationRequest = new LocationRequest();

		if (mIsHighAccuracy) {
			mLocationRequest.setInterval(5000);
			mLocationRequest.setFastestInterval(2000);
			mLocationRequest
					.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		} else {
			mLocationRequest.setInterval(10000);
			mLocationRequest.setFastestInterval(5000);
			mLocationRequest
					.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		}
		LocationServices.FusedLocationApi.requestLocationUpdates(
				mGoogleApiClient, mLocationRequest, this);
	}

	// accessing views and button
	public void acessViews() {
		mPeopleTextView = (TextView) findViewById(R.id.text_people);
		mPeopleTextView.setText("searching...");
		mHelpButton = (Button) findViewById(R.id.button_help);
		mHelpButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mHelpFlag == ASK_HELP_FLAG) {
					if (mAnimation != null) {
						mHelpButton.startAnimation(mAnimation);
					}
					removeMarkers();
					mIsHighAccuracy = true;
					resetAccuracyOfLocation();
					RequestParams params = CommonFunctions.helpParams(
							ASK_HELP_PATH, mUserEmail);
					changeTextOfButton(ASKED_HELP);
					mHelpFlag = ASKED_HELP_FLAG;
					new CommonResultAsyncTask(MainActivity.this, ASK_HELP_TEXT,
							ASK_HELP_FLAG).execute(params);
					mPeopleTextView.setText("searching...");
					NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
					Intent intent = new Intent(MainActivity.this,
							MainActivity.class);
					PendingIntent pendingIntent = PendingIntent.getActivity(
							MainActivity.this, 0, intent, 0);
					Notification.Builder notificationBuilder = new Notification.Builder(
							MainActivity.this);
					Notification notification = notificationBuilder
							.setContentTitle("ASKED FOR HELP")
							.setContentText(
									"click cancel button to remove the request.\n click view to see view people around you.")
							.setAutoCancel(false)
							.addAction(R.drawable.cancel, "cancel",
									pendingIntent)
							.addAction(R.drawable.view, "view", pendingIntent)
							.build();
					notificationManager.notify(0, notification);

				} else if (mHelpFlag == ASKED_HELP_FLAG) {
					removeMarkers();
					RequestParams params = CommonFunctions.helpParams(
							HELP_RECEIVED_PATH, mUserEmail);
					changeTextOfButton(ASK_HELP);
					mHelpFlag = ASK_HELP_FLAG;
					mIsHighAccuracy = false;
					resetAccuracyOfLocation();
					mHelpButton.clearAnimation();
					new CommonResultAsyncTask(MainActivity.this, HELPED_TEXT,
							ASKED_HELP_FLAG).execute(params);
					mPeopleTextView.setText("searching...");

				} else if (mHelpFlag == HELPING_FLAG) {
					removeMarkers();
					changeTextOfButton(ASK_HELP);
					mHelpFlag = ASK_HELP_FLAG;
					mIsHighAccuracy = false;
					resetAccuracyOfLocation();
					mHelpButton.clearAnimation();
					RequestParams params = CommonFunctions.helpParams(
							HELPED_PATH, mUserEmail);
					new CommonResultAsyncTask(MainActivity.this, HELPED_TEXT,
							HELPING_FLAG).execute(params);
					mPeopleTextView.setText("searching...");
				}

			}
		});
	}

	/*
	 * button animation method
	 */
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

	/*
	 * resetting the text on button
	 */
	public void changeTextOfButton(String text) {
		mHelpButton.setText(text);
	}

	/*
	 * method to get stuff from intents
	 */
	public void retriveIntentExtras(Intent intent) {
		if (intent.hasExtra(AppPreferences.IntentExtras.INITIAL_LOCATIONS)) {

		} else if (intent.hasExtra(AppPreferences.IntentExtras.COORDINATES)) {
			mHelpFlag = HELPING_FLAG;
			mHelpButton.setAnimation(mAnimation);
			mIsHighAccuracy = true;
			changeTextOfButton(HELPING);
			mVictimUserEmail = intent
					.getStringExtra(AppPreferences.IntentExtras.USERID);
			resetAccuracyOfLocation();
		}

	}

	/*
	 * method to reset the accuracy of how precise we want the location update
	 */

	public void resetAccuracyOfLocation() {
		stopLocationUpdates();
		startLocationUpdates();

	}

	public void startLocationUpdates() {
		if (mGoogleMap != null) {
			this.activate(mOnLocationChangeListener);
			mGoogleMap.setMyLocationEnabled(true);
			settingUpMapLocationSource();
		}
	}

	public void stopLocationUpdates() {
		if (mGoogleMap != null) {
			mGoogleMap.setMyLocationEnabled(false);
			LocationServices.FusedLocationApi.removeLocationUpdates(
					mGoogleApiClient, this);
			this.deactivate();
		}
	}

	/*
	 * filling map with users
	 */
	public void fillMap(ArrayList<LocationDetailsModel> locations) {
		int count = locations.size();
		removeMarkers();

		if (count == 0) {
			if (mHelpFlag == ASK_HELP_FLAG) {
				mPeopleTextView.setText("0 people around you");
			} else if (mHelpFlag == ASKED_HELP_FLAG
					|| mHelpFlag == HELPING_FLAG) {
				mPeopleTextView.setText("0 people responded");
			}
		} else {
			if (count == 1) {
				if (mHelpFlag == ASK_HELP_FLAG) {
					mPeopleTextView.setText("1 person around you");
				} else if (mHelpFlag == ASKED_HELP_FLAG
						|| mHelpFlag == HELPING_FLAG) {
					mPeopleTextView.setText("0 people responded");
				}
			} else {
				if (mHelpFlag == ASK_HELP_FLAG) {
					mPeopleTextView.setText(count + " persons around you");
				} else if (mHelpFlag == ASKED_HELP_FLAG
						|| mHelpFlag == HELPING_FLAG) {
					if (count - 1 == 1) {
						mPeopleTextView.setText("1" + " person responded");
					} else {
						mPeopleTextView.setText(count + " persons responded");
					}

				}
			}
			for (LocationDetailsModel location : locations) {
				int color = location.getColor();
				if (color == AppPreferences.Flags.USER_COLOR_FLAG) {
					Marker marker = mGoogleMap
							.addMarker(new MarkerOptions()
									.position(
											new LatLng(location.getLatitude(),
													location.getLongitude()))
									.title("user")
									.icon(BitmapDescriptorFactory
											.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
					mMarkers.add(marker);
				} else if (color == AppPreferences.Flags.HELPER_COLOR_FLAG
						&& !location.getUser_email().equals(mUserEmail)) {
					Marker marker = mGoogleMap
							.addMarker(new MarkerOptions()
									.position(
											new LatLng(location.getLatitude(),
													location.getLongitude()))
									.title("helper")
									.icon(BitmapDescriptorFactory
											.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
					mMarkers.add(marker);
				} else if (color == AppPreferences.Flags.VICTIM_COLOR_FLAG
						&& !location.getUser_email().equals(mUserEmail)) {
					Marker marker = mGoogleMap
							.addMarker(new MarkerOptions()
									.position(
											new LatLng(location.getLatitude(),
													location.getLongitude()))
									.title("victim")
									.icon(BitmapDescriptorFactory
											.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
					mMarkers.add(marker);
				}

			}

		}

	}

	public void settingTextOfButton(int flag) {
		if (flag == HELPING_FLAG) {
			mHelpButton.startAnimation(mAnimation);
			mHelpButton.setText(HELPING);
		} else if (flag == ASKED_HELP_FLAG) {
			mHelpButton.startAnimation(mAnimation);
			mHelpButton.setText(ASKED_HELP);
		} else if (flag == ASK_HELP_FLAG) {
			mHelpButton.setText(ASK_HELP);
		}
	}

	public void removeMarkers() {
		if (mMarkers.size() != 0) {
			for (Marker marker : mMarkers) {
				marker.remove();
			}
		}
	}

}
