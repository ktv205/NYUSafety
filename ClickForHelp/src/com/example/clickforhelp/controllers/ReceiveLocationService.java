package com.example.clickforhelp.controllers;

import java.util.ArrayList;

import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.LocationDetailsModel;
import com.example.clickforhelp.models.RequestParams;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

public class ReceiveLocationService extends Service {
	//private static final String TAG = "ReceiveLocationService";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			if (intent.hasExtra("coord")) {
				String[] values = {
						"public",
						"index.php",
						"trackuser",
						getSharedPreferences(AppPreferences.SharedPrefAuthentication.name,
								MODE_PRIVATE).getString(
								AppPreferences.SharedPrefAuthentication.user_email, ""),
						intent.getExtras().getString("userid") };
				RequestParams params = CommonFunctions.setParams(
						AppPreferences.ServerVariables.SCHEME,
						AppPreferences.ServerVariables.AUTHORITY, values);
				new GetLocationOfPeers().execute(params);
			} else {

				String[] values = {
						"public",
						"index.php",
						"home",
						getSharedPreferences(AppPreferences.SharedPrefAuthentication.name,
								MODE_PRIVATE).getString(
								AppPreferences.SharedPrefAuthentication.user_email, "") };
				RequestParams params = CommonFunctions.setParams(
						AppPreferences.ServerVariables.SCHEME,
						AppPreferences.ServerVariables.AUTHORITY, values);
				new GetLocationOfPeers().execute(params);
			}
		}
		return START_NOT_STICKY;

	}

	public class GetLocationOfPeers extends
			AsyncTask<RequestParams, Void, String> {
		@Override
		protected String doInBackground(RequestParams... params) {
			return new HttpManager().sendUserData(params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			ArrayList<LocationDetailsModel> locations = new MyJSONParser()
					.parseLocation(result);
			Intent intent = new Intent("com.example.clickforhelp.action_send");
			intent.putParcelableArrayListExtra("key", locations);
			sendBroadcast(intent);

		}

	}

}
