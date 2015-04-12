package com.example.clickforhelp.controllers.ui.fragments;

import java.util.HashMap;

import com.example.clickforhelp.R;
import com.example.clickforhelp.controllers.ui.HelperActivity;
import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.CommonResultAsyncTask;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.RequestParams;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NewPasswordFragment extends Fragment {
	View mView;
	// private final static String TAG =
	// NewPasswordFragment.class.getSimpleName();
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
	private final static String MESSAGE = "Please wait while we set your new password";
	private final static String RESET_PASSWORD = "resetpassword";
	private boolean mIsChange = false;
	private final static String UPDATE_PASSWORD = "updatepassword";
	private static final int RESET = 1;
	private static final int UPDATE = 2;

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
					if (mIsChange) {
						RequestParams params = setPasswordParams(RESET);
						new CommonResultAsyncTask(getActivity(), MESSAGE, 0)
								.execute(params);
					} else {
						RequestParams params = setPasswordParams(UPDATE);
						new CommonResultAsyncTask(getActivity(), MESSAGE, 0)
								.execute(params);
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

	public RequestParams setPasswordParams(int values) {
		String[] finalPath;
		if (values == UPDATE) {
			String[] paths = { UPDATE_PASSWORD,
					CommonFunctions.getEmail(getActivity()), mPassword };
			finalPath = paths;
		} else {
			String[] paths = { RESET_PASSWORD,
					CommonFunctions.getEmail(getActivity()), mOldPassword,
					mPassword };
			finalPath = paths;
		}
		RequestParams params = CommonFunctions.setParams(finalPath);
		return params;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		checkForIntent();
		if (mIsChange) {
			getActivity().getActionBar().setTitle(
					R.string.title_change_password);
		} else {
			getActivity().getActionBar().setTitle(R.string.title_new_password);
			mOldPasswordEdittext.setVisibility(View.GONE);
		}
	}

	public void checkForIntent() {
		if (getActivity().getIntent() != null
				&& getActivity().getIntent().hasExtra(
						AppPreferences.IntentExtras.CHANGE)) {
			mIsChange = true;
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

	public void responseFromServer(int code) {
		if (code == SET) {
			if (mIsChange) {
				setFlagPreference();
				getActivity().setResult(0);
				getActivity().finish();
			} else {
				setFlagPreference();
				startActivity(new Intent(getActivity(), HelperActivity.class));
				getActivity().finishAffinity();
			}
		} else if (code == PASSWORD_WRONG) {
			Toast.makeText(getActivity(), "password you entered is wrong",
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getActivity(),
					"something went wrong please try again", Toast.LENGTH_SHORT)
					.show();
		}
	}

	public void setFlagPreference() {
		HashMap<String, String> values = new HashMap<String, String>();
		values.put(AppPreferences.SharedPrefAuthentication.password, mPassword);
		values.put(AppPreferences.SharedPrefAuthentication.flag,
				AppPreferences.SharedPrefAuthentication.FLAG_ACTIVE);
		CommonFunctions.saveInPreferences(getActivity(),
				AppPreferences.SharedPrefAuthentication.name, values);
	}

}
