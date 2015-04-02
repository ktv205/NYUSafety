package com.example.clickforhelp.controllers.ui;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.example.clickforhelp.R;
import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.HttpManager;
import com.example.clickforhelp.controllers.utils.SendLocationsAsyncTask;
import com.example.clickforhelp.controllers.utils.SendLocationsAsyncTask.GetOtherUsersLocations;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.LocationDetailsModel;
import com.example.clickforhelp.models.RequestParams;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationServices;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class HelperActivity extends Activity implements
		OnConnectionFailedListener, ConnectionCallbacks, GetOtherUsersLocations {

	private static final String TAG = HelperActivity.class.getSimpleName();
	private Context mContext;
	public static final int SLEEP_AUTHENTICATION = 3000;
	private Timer mTimer;
	private Handler mHandler;
	private GoogleApiClient mGoogleApiClient;
	private String SENDER_ID = AppPreferences.GOOGLEREGID;
	private GoogleCloudMessaging mGcm;
	private String mRegid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getApplicationContext();
		overridePendingTransition(0, 0);
		if (CommonFunctions.isConnected(mContext)) {
			setContentView(R.layout.activity_helper);
			setActionBar();
			mHandler = new Handler();
			mTimer = new Timer();
			if (CommonFunctions.checkLoggedIn(mContext)) {
				if (CommonFunctions.checkIfGCMInfoIsSent(mContext)) {
					// nothing to do if already info is sent
				} else {
					registerInBackground();

				}
				startGoogleApiClient();

			} else {
				pauseActiviy();
			}
		} else {
			setNoConnectionView();
		}

	}

	@Override
	public void overridePendingTransition(int enterAnim, int exitAnim) {
		super.overridePendingTransition(enterAnim, exitAnim);
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(0, 0);
	}

	@Override
	public void onConnected(Bundle arg0) {
		Location location = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);
		if (location != null) {
			String[] locationValues = {
					"public",
					"index.php",
					"updatelocation",
					getSharedPreferences(
							AppPreferences.SharedPrefAuthentication.name,
							MODE_PRIVATE).getString(
							AppPreferences.SharedPrefAuthentication.user_email,
							""), String.valueOf(location.getLatitude()),
					String.valueOf(location.getLongitude()) };
			RequestParams locationParams = CommonFunctions.setParams(
					AppPreferences.ServerVariables.SCHEME,
					AppPreferences.ServerVariables.AUTHORITY, locationValues);
			if (CommonFunctions.isConnected(this)) {
				new SendLocationsAsyncTask(HelperActivity.this)
						.execute(locationParams);
			}
		}else{
			Toast.makeText(mContext, "location is null", Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {

	}

	// user defined methods

	public void setActionBar() {
		ActionBar bar = getActionBar();
		bar.setIcon(R.drawable.nyu_white);
	}

	public void setNoConnectionView() {
		setContentView(R.layout.no_connection);

		// Button for retrying for network connection
		Button button = (Button) findViewById(R.id.no_connection_retry);

		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (CommonFunctions.isConnected(mContext)) {
					startActivity(new Intent(mContext, HelperActivity.class));
					finish();
				}

			}
		});
	}

	public void goToAuthenticationActivity() {
		startActivity(new Intent(mContext, AuthenticationActivity.class));
		finish();
	}

	public void pauseActiviy() {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						goToAuthenticationActivity();
					}
				});

			}
		};
		mTimer.schedule(timerTask, SLEEP_AUTHENTICATION);

	}

	public void startGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();
		mGoogleApiClient.connect();
	}

	@Override
	public void getData(LocationDetailsModel locations) {
		Toast.makeText(mContext,
				"here you are supposed to get users locations",
				Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(mContext, MainActivity.class);
		startActivity(intent);
		finish();

	}

	// getting and Sending gcm to server

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
					CommonFunctions.storeRegistrationId(mContext, mRegid);
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
				CommonFunctions
						.getSharedPreferences(mContext,
								AppPreferences.SharedPrefAuthentication.name)
						.getString(
								AppPreferences.SharedPrefAuthentication.user_email,
								""), regid };
		RequestParams params = CommonFunctions.setParams(
				AppPreferences.ServerVariables.SCHEME,
				AppPreferences.ServerVariables.AUTHORITY, values);
		new SendGCMInfoAsyncTask().execute(params);
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

}
