package com.example.clickforhelp.controllers.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.example.clickforhelp.models.LocationDetailsModel;
import com.example.clickforhelp.models.RequestParams;

public class SendLocationsAsyncTask extends
		AsyncTask<RequestParams, Void, String> {
	Context mContext;
	private GetOtherUsersLocations mUserLocations;

	public interface GetOtherUsersLocations {
		public void getData(LocationDetailsModel locations);
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

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected String doInBackground(RequestParams... params) {
		return new HttpManager().sendUserData(params[0]);
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		mUserLocations.getData(null);
	}

}
