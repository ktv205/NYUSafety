package com.example.clickforhelp.controllers.ui;

import java.util.ArrayList;

import com.example.clickforhelp.R;
import com.example.clickforhelp.controllers.services.LocationUpdateService;
import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.HttpManager;
import com.example.clickforhelp.controllers.utils.MyJSONParser;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.LocationDetailsModel;
import com.example.clickforhelp.models.RequestParams;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

public class HelperActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_helper);
		ActionBar bar = getActionBar();
		overridePendingTransition(0, 0);
		bar.setIcon(R.drawable.nyu_white);

		if (getSharedPreferences(AppPreferences.SharedPrefAuthentication.name,
				MODE_PRIVATE).getString(
				AppPreferences.SharedPrefAuthentication.user_email, "")
				.isEmpty()
				&& !getSharedPreferences(
						AppPreferences.SharedPrefAuthentication.name,
						MODE_PRIVATE).getString(
						AppPreferences.SharedPrefAuthentication.flag, "")
						.equals("1")) {
			new HelperAsyncTask().execute();
		} else {
			if (CommonFunctions.isMyServiceRunning(LocationUpdateService.class,
					this)) {
				// do nothing

			} else {
				startService(new Intent(this, LocationUpdateService.class));
			}
			String[] values = {
					"public",
					"index.php",
					"home",
					getSharedPreferences(
							AppPreferences.SharedPrefAuthentication.name,
							MODE_PRIVATE).getString(
							AppPreferences.SharedPrefAuthentication.user_email,
							"") };
			RequestParams params = CommonFunctions.setParams(
					AppPreferences.ServerVariables.SCHEME,
					AppPreferences.ServerVariables.AUTHORITY, values);
			if (CommonFunctions.isConnected(this)) {
				new GetLocationOfPeers().execute(params);
			} else {

			}
		}

	}

	@Override
	public void overridePendingTransition(int enterAnim, int exitAnim) {
		super.overridePendingTransition(enterAnim, exitAnim);
	}

	public class HelperAsyncTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			startActivity(new Intent(HelperActivity.this,
					AuthenticationActivity.class));
			finish();
		}

	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(0, 0);
	}

	public class GetLocationOfPeers extends
			AsyncTask<RequestParams, Void, String> {
		@Override
		protected String doInBackground(RequestParams... params) {
			try {
				if (CommonFunctions.isMyServiceRunning(
						LocationUpdateService.class, HelperActivity.this)) {
					Thread.sleep(1000);
				} else {
					Thread.sleep(4000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return new HttpManager().sendUserData(params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			ArrayList<LocationDetailsModel> locations = new MyJSONParser()
					.parseLocation(result);
			Intent intent = new Intent(HelperActivity.this, MainActivity.class);
			intent.putParcelableArrayListExtra(
					AppPreferences.IntentExtras.LOCATIONS, locations);
			startActivity(intent);
			finish();

		}

	}
}
