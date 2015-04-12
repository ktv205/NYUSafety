package com.example.clickforhelp.controllers.utils;

import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.RequestParams;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class CommonResultAsyncTask extends
		AsyncTask<RequestParams, Void, String> {
	private Context mContext;
	private ServerResponse mServerResponse;
	private int mFlag;
	private String mMessage;
	private ProgressDialog mProgressDialog;

	public interface ServerResponse {
		public void IntegerResponse(int response, int flag);
	}

	public CommonResultAsyncTask(Context context, String message, int flag) {
		mContext = context;
		mMessage = message;
		mFlag = flag;
		try {
			mServerResponse = (ServerResponse) mContext;
		} catch (ClassCastException e) {
			throw new ClassCastException(mContext.toString()
					+ " must implement OnHeadlineSelectedListener");
		}

	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		if (mProgressDialog != null && mMessage!=null) {
			mProgressDialog = new ProgressDialog(mContext);
			mProgressDialog.setTitle(AppPreferences.Others.LOADING);
			mProgressDialog.setMessage(mMessage);
			mProgressDialog.show();
		}

	}

	@Override
	protected String doInBackground(RequestParams... params) {
		return HttpManager.sendUserData(params[0]);
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
		if (result != null) {
			int code = MyJSONParser.AuthenticationParser(result);
			mServerResponse.IntegerResponse(code, mFlag);
		} else {
			mServerResponse.IntegerResponse(-10, mFlag);
		}

	}

}
