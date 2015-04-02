package com.example.clickforhelp.controllers.ui.fragments;

import java.util.HashMap;

import com.example.clickforhelp.R;
import com.example.clickforhelp.controllers.ui.MainActivity;
import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.HttpManager;
import com.example.clickforhelp.controllers.utils.MyJSONParser;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.RequestParams;
import com.example.clickforhelp.models.AppPreferences.ServerVariables;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NewPasswordFragment extends Fragment {
	View mView;
	private final static String TAG = NewPasswordFragment.class.getSimpleName();
	private final static int PASSWORD_EMPTY = 2;
	private final static int mRetype_EMPTY = 3;
	private final static int DONT_MATCH = 4;
	private final static int RESULT_OK = 5;
	private final static int SET = 1;
	private final static int PASSWORD_WRONG = -1;
	private final static int OLD_PASSWORD_WRONG = 6;
	private String mPassword, mRetype, mOldPassword;
	private final static int OLD_PASSWORD_EMPTY = 7;
	EditText mOldPasswordEdittext;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_newpassword, container,
				false);
		Button submitButton = (Button) mView
				.findViewById(R.id.newpassword_button_submit);
		mOldPasswordEdittext = (EditText) mView
				.findViewById(R.id.newpassword_edit_old_password);
		submitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String message = null;
				int flag = getTextFromFields();
				if (flag == PASSWORD_EMPTY) {
					message = "password cant be empty";
				} else if (flag == mRetype_EMPTY) {
					message = "mRetype cant be empty";
				} else if (flag == DONT_MATCH) {
					message = "password do not match";
				} else if (flag == OLD_PASSWORD_EMPTY) {
					message = "enter your current password";
				} else if (flag == OLD_PASSWORD_WRONG) {
					message = "current password wrong";
				} else {
					if (getActivity().getIntent().hasExtra(
							AppPreferences.IntentExtras.CHANGE)) {
						String[] paths = {
								"public",
								"index.php",
								"resetpassword",
								getActivity()
										.getSharedPreferences(
												AppPreferences.SharedPrefAuthentication.name,
												Context.MODE_PRIVATE)
										.getString(
												AppPreferences.SharedPrefAuthentication.user_email,
												""), mOldPassword, mPassword };
						RequestParams params = CommonFunctions.setParams(
								ServerVariables.SCHEME,
								ServerVariables.AUTHORITY, paths);
						new SendPasswordAsyncTask().execute(params);
					} else {
						String[] paths = {
								"public",
								"index.php",
								"updatepassword",
								getActivity()
										.getSharedPreferences(
												AppPreferences.SharedPrefAuthentication.name,
												Context.MODE_PRIVATE)
										.getString(
												AppPreferences.SharedPrefAuthentication.user_email,
												""), mPassword };
						RequestParams params = CommonFunctions.setParams(
								ServerVariables.SCHEME,
								ServerVariables.AUTHORITY, paths);
						new SendPasswordAsyncTask().execute(params);
					}
				}
				if (message != null) {
					Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT)
							.show();
				}

			}
		});
		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (getActivity().getIntent() != null) {
			if (getActivity().getIntent().hasExtra(
					AppPreferences.IntentExtras.CHANGE)) {
				getActivity().getActionBar().setTitle(
						R.string.title_change_password);

			} else {
				getActivity().getActionBar().setTitle(
						R.string.title_new_password);
				mOldPasswordEdittext.setVisibility(View.GONE);
			}
		} else {
			getActivity().getActionBar().setTitle(R.string.title_new_password);
			mOldPasswordEdittext.setVisibility(View.GONE);
		}
	}

	public int getTextFromFields() {

		EditText passwordEdittext = (EditText) mView
				.findViewById(R.id.newpassword_edit_password);
		EditText mRetypeEdittext = (EditText) mView
				.findViewById(R.id.newpassword_edit_reenter);
		mPassword = passwordEdittext.getText().toString();
		mRetype = mRetypeEdittext.getText().toString();
		if (mPassword.isEmpty()) {
			return PASSWORD_EMPTY;
		} else if (mRetype.isEmpty()) {
			return mRetype_EMPTY;
		} else if (!mPassword.equals(mRetype)) {
			return DONT_MATCH;
		} else if (mOldPasswordEdittext.isShown()) {
			mOldPassword = mOldPasswordEdittext.getText().toString();
			if (mOldPassword.isEmpty()) {
				return OLD_PASSWORD_EMPTY;
			} else if (!mOldPassword.equals(CommonFunctions
					.getSharedPreferences(getActivity(),
							AppPreferences.SharedPrefAuthentication.password)
					.getString(
							AppPreferences.SharedPrefAuthentication.password,
							""))) {
				return OLD_PASSWORD_WRONG;

			} else {
				return RESULT_OK;
			}
		} else {

			return RESULT_OK;

		}
	}

	public class SendPasswordAsyncTask extends
			AsyncTask<RequestParams, Void, String> {
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(getActivity());
			dialog.setTitle(AppPreferences.Others.LOADING);
			dialog.setMessage("Please wait while we set your new password");
		}

		@Override
		protected String doInBackground(RequestParams... params) {
			return new HttpManager().sendUserData(params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.d(TAG, result);
			if (result != null) {
				int code = MyJSONParser.AuthenticationParser(result);
				if (code == SET) {
					if (getActivity().getIntent() != null) {
						if (getActivity().getIntent().hasExtra(
								AppPreferences.IntentExtras.CHANGE)) {
							setFlagPreference();
							getActivity().setResult(0);
							getActivity().finish();
						} else {
							startActivity(new Intent(getActivity(),
									MainActivity.class));
							setFlagPreference();
							getActivity().finishAffinity();
						}
					} else {
						startActivity(new Intent(getActivity(),
								MainActivity.class));
						getActivity().finishAffinity();
					}
				} else if (code == PASSWORD_WRONG) {
					Toast.makeText(getActivity(),
							"password you entered is wrong", Toast.LENGTH_SHORT)
							.show();
				} else {
					Toast.makeText(getActivity(),
							"something went wrong please try again",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(getActivity(),
						"something went wrong please try again",
						Toast.LENGTH_SHORT).show();
			}
		}

		public void setFlagPreference() {
			HashMap<String, String> values = new HashMap<String, String>();
			values.put(AppPreferences.SharedPrefAuthentication.password,
					mPassword);
			values.put(AppPreferences.SharedPrefAuthentication.flag,
					AppPreferences.SharedPrefAuthentication.FLAG_ACTIVE);
			CommonFunctions.saveInPreferences(getActivity(),
					AppPreferences.SharedPrefAuthentication.name, values);
		}

	}

}
