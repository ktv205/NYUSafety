package com.example.clickforhelp.controllers.utils;

import java.util.ArrayList;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.clickforhelp.models.LocationDetailsModel;
import com.example.clickforhelp.models.RequestParams;

public class SendLocationsAsyncTask extends
		AsyncTask<RequestParams, Void, String> {
	Context mContext;
	private GetOtherUsersLocations mUserLocations;
	
	private static final String TAG=SendLocationsAsyncTask.class.getSimpleName();

	public interface GetOtherUsersLocations {
		public void getData(ArrayList<LocationDetailsModel> arrayList);
	}

	public SendLocationsAsyncTask(Context context) {
		mContext = context;
		try {
			mUserLocations = (GetOtherUsersLocations) mContext;
		} catch (ClassCastException e) {
			throw new ClassCastException(mContext.toString()
					+ " must implement OnHeadlineSelectedListener");
		}

	}

	public SendLocationsAsyncTask() {

	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected String doInBackground(RequestParams... params) {
		return HttpManager.sendUserData(params[0]);
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		if (mUserLocations != null) {
			Log.d(TAG,"mUsersNotNull");
			if(result!=null){
				Log.d(TAG,"result not null");
				Log.d(TAG,result);
				mUserLocations.getData(MyJSONParser.parseLocation(result));	
			}
			
		}
	}

}
