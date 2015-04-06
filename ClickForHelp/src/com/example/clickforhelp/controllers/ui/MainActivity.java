package com.example.clickforhelp.controllers.ui;

import java.util.ArrayList;

import com.example.clickforhelp.controllers.services.ActivityRecognitionService;
import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.CommonResultAsyncTask.ServerResponse;
import com.example.clickforhelp.controllers.utils.InternetConnectionAsyncTask;
import com.example.clickforhelp.controllers.utils.SendLocationsAsyncTask;
import com.example.clickforhelp.controllers.utils.InternetConnectionAsyncTask.InternetConntection;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.clickforhelp.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnMapReadyCallback,
		OnConnectionFailedListener,
		com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks,
		LocationListener, LocationSource, OnMapLoadedCallback,
		GetOtherUsersLocations, InternetConntection, ServerResponse {

	// TAG for debugging
	private static final String TAG = MainActivity.class.getSimpleName();

	// Text for button
	private static final String ASK_HELP = "Ask For Help";
	private static final String ASKED_HELP = "Asked For Help(click here after receiving help)";
	private static final String HELPING = "Helping a Friend(click here after helping)";

	private static final int ASK_HELP_FLAG = 0;
	private static final int ASKED_HELP_FLAG = 1;
	private static final int HELPING_FLAG = 2;

	private AlphaAnimation mAnimation;

	private int mHelpFlag = 0;

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
	private String userEmail;

	// locations
	private LocationRequest mLocationRequest;
	private OnLocationChangedListener mLocationChangedListener;
	private Location mLastLocation;
	private ArrayList<Marker> mMarkers;

	// locationParams

	private RequestParams mLocationParams;

	// TextView of people around

	private TextView mPeopleTextView;

	// Button for asking help
	private Button mHelpButton;

	// handler
	private Handler mHandler;

	// arraylist of locations
	private ArrayList<LocationDetailsModel> mLocations;

	private static final String UPDATE_HOME = "uh";

	private MapFragment mMapFragment;

	@SuppressLint("HandlerLeak")
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
			userEmail = CommonFunctions.getEmail(mContext);

			// Markers
			mMarkers = new ArrayList<Marker>();

			// initialize button and textview
			acessViews();

			// animating button
			buttonAnimation();

			// get Intent from the notification
			Intent intent = getIntent();

			if (getIntent() != null) {
				// Log.d(TAG, "intent not null");
				retriveIntentExtras(intent);
			}

			mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					Bundle bundle = msg.getData();
					if (bundle != null) {
						String activity = bundle
								.getString(AppPreferences.IntentExtras.ActivityRecognitionService_EXTRA_MESSAGE);
						if ((activity == AppPreferences.SharedPrefActivityRecognition.VEHICLE
								|| activity == AppPreferences.SharedPrefActivityRecognition.WALKING) && mGoogleApiClient==null) {
                            //start the location update inside the activity
						}else if(activity==AppPreferences.SharedPrefActivityRecognition.STILL && mGoogleApiClient!=null){
							//stop the google api and start receive location service
						}
					}
				}
			};

		} else {
			setNoConnectionView();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
		}
		// if (mLocationReceiver != null) {
		// this.unregisterReceiver(mLocationReceiver);
		// }
		this.deactivate();
		if (mGoogleMap != null) {
			mGoogleMap = null;
		}
		// if (mActivityReceiver != null) {
		// this.unregisterReceiver(mActivityReceiver);
		// }
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
		// Toast.makeText(mContext, "in onLocationChangedListener",
		// Toast.LENGTH_SHORT).show();
		mLocationChangedListener = arg0;

	}

	@Override
	public void deactivate() {
		Log.d(TAG, "deactivated");
		mLocationChangedListener = null;
	}

	// LocationListner interface method
	@Override
	public void onLocationChanged(Location location) {
		// Toast.makeText(
		// mContext,
		// CommonFunctions
		// .getSharedPreferences(
		// mContext,
		// AppPreferences.SharedPrefActivityRecognition.activityType)
		// .getString(
		// AppPreferences.SharedPrefActivityRecognition.type,
		// AppPreferences.SharedPrefActivityRecognition.WALKING),
		// Toast.LENGTH_SHORT).show();
		if (mLocationChangedListener != null && location != null) {
			mLocationChangedListener.onLocationChanged(location);
			mLocationParams = CommonFunctions
					.buildLocationUpdateParams(
							userEmail,
							location.getLatitude(),
							location.getLongitude(),
							new String[] {
									AppPreferences.SharedPrefActivityRecognition.WALKING,
									UPDATE_HOME });
			if (CommonFunctions.isConnected(mContext)) {
				new InternetConnectionAsyncTask(this, true).execute();
			}
		}

	}

	// Google api methods
	@Override
	public void onConnected(Bundle arg0) {
		settingUpActivityRecognition();
		settingUpMapLocationSource();
		moveCameraToCurrentLocation();
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
		// Toast.makeText(mContext, "mapLoaded", Toast.LENGTH_SHORT).show();

		mGoogleMap.setMyLocationEnabled(true);
		mGoogleMap.setLocationSource(this);
		buildGoogleApiClient();

	}

	@Override
	public void getData(ArrayList<LocationDetailsModel> locations) {
		Toast.makeText(mContext, "do something here", Toast.LENGTH_SHORT)
				.show();
		mLocations = locations;
		fillMap(mLocations);

	}

	@Override
	public void isConnected(boolean connected) {
		// Log.d(TAG, String.valueOf(connected));
		if (connected) {
			new SendLocationsAsyncTask(this).execute(mLocationParams);
		}

	}

	@Override
	public void IntegerResponse(int response, int flag) {

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
		mMapFragment = (MapFragment) getFragmentManager().findFragmentById(
				R.id.map);
		mMapFragment.getFragmentManager().findFragmentById(R.id.map);
		mMapFragment.getMapAsync(this);
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
			intent.putExtra(
					AppPreferences.IntentExtras.ActivityRecognitionService_EXTRA_MESSAGE,
					new Messenger(mHandler));
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

	public void fillMap(ArrayList<LocationDetailsModel> locations) {
		if (!mMarkers.isEmpty()) {
			for (Marker marker : mMarkers) {
				marker.remove();
			}
		}
		if (locations != null) {
			// Log.d(TAG, "locations not null");
			if (locations.isEmpty()) {
				// Log.d(TAG, "locations empty");
				mPeopleTextView.setText("No one around you");
				// empty
			} else {
				int size = locations.size();
				if (size == 1) {
					mPeopleTextView.setText(locations.size()
							+ " person near you");
				} else {
					mPeopleTextView.setText(locations.size()
							+ " persons near you");
				}

				if (mGoogleMap != null) {
					// Log.d(TAG, "mGoogleMap is not empty");
					for (LocationDetailsModel location : locations) {
						// Log.d(TAG, "inside locations of fill map");
						// Log.d(TAG,
						// location.getLatitude() + " "
						// + location.getLongitude());

						Marker marker = mGoogleMap
								.addMarker(new MarkerOptions().position(
										new LatLng(location.getLatitude(),
												location.getLongitude()))
										.title("friend"));
						mMarkers.add(marker);
					}
				} else {
					Log.d(TAG, "mGoogleMap is empty");
				}
			}

		}

	}

	public void acessViews() {
		mPeopleTextView = (TextView) findViewById(R.id.text_people);
		mHelpButton = (Button) findViewById(R.id.button_help);
		mHelpButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mHelpFlag == ASK_HELP_FLAG) {
					if (mAnimation != null) {
						mHelpButton.startAnimation(mAnimation);
					}
					highAccuracy = true;
					resetGoogleApiClient();
					RequestParams params = CommonFunctions.helpParams(
							"askhelp", userEmail);
					changeTextOfButton(ASKED_HELP);
					mHelpFlag = ASKED_HELP_FLAG;
					// new CommonResultAsyncTask().execute(params);

				} else if (mHelpFlag == ASKED_HELP_FLAG) {
					RequestParams params = CommonFunctions.helpParams(
							"helpreceived", userEmail);
					changeTextOfButton(ASK_HELP);
					mHelpFlag = ASK_HELP_FLAG;
					highAccuracy = false;
					resetGoogleApiClient();
					mHelpButton.clearAnimation();
					// new CommonResultAsyncTask().execute(params);

				} else if (mHelpFlag == HELPING_FLAG) {
					changeTextOfButton(ASK_HELP);

					mHelpFlag = ASK_HELP_FLAG;
					highAccuracy = false;
					resetGoogleApiClient();
					mHelpButton.clearAnimation();
				}

			}
		});
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

	public void changeTextOfButton(String text) {
		mHelpButton.setText(text);
	}

	public void retriveIntentExtras(Intent intent) {
		if (intent.hasExtra(AppPreferences.IntentExtras.INITIAL_LOCATIONS)) {
			Log.d(TAG, "has extras as intial locations");
			mLocations = intent
					.getParcelableArrayListExtra(AppPreferences.IntentExtras.INITIAL_LOCATIONS);
			for (LocationDetailsModel location : mLocations) {
				Log.d(TAG, location.getUser_email());
			}
			fillMap(mLocations);

		} else if (intent.hasExtra(AppPreferences.IntentExtras.COORDINATES)) {
			highAccuracy = true;
			mHelpButton.setText(HELPING);
			mHelpFlag = HELPING_FLAG;
			if (mAnimation != null) {
				mHelpButton.startAnimation(mAnimation);
				resetGoogleApiClient();
			}
		}

	}

	public void moveCameraToCurrentLocation() {
		mLastLocation = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);

		if (mLastLocation != null) {
			// Log.d(TAG, "mLastLocation is not null");
			mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
					mLastLocation.getLatitude(), mLastLocation.getLongitude()),
					16));
		}
	}

	public void resetGoogleApiClient() {
		if (mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
			if (mGoogleMap != null) {
				mGoogleMap = null;
				mMapFragment = null;
				this.deactivate();
				initializeMap();
			}
		} else {

		}
	}
}
