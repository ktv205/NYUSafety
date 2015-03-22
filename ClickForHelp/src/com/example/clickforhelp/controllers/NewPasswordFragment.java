package com.example.clickforhelp.controllers;

import com.example.clickforhelp.R;
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
	View view;
	private final static String TAG = "NewPasswordFragment";
	private final static int PASSWORD_EMPTY = 2;
	private final static int RETYPE_EMPTY = 3;
	private final static int DONT_MATCH = 4;
	private final static int RESULT_OK = 5;
	private final static String SET = "1";
	private String password, reType;
	EditText oldEditText;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater
				.inflate(R.layout.fragment_newpassword, container, false);
		Button submitButton = (Button) view
				.findViewById(R.id.newpassword_button_submit);
		oldEditText = (EditText) view
				.findViewById(R.id.newpassword_edit_old_password);

		submitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String message = null;
				int flag = getTextFromFields();
				if (flag == PASSWORD_EMPTY) {
					message = "password cant be empty";
				} else if (flag == RETYPE_EMPTY) {
					message = "retype cant be empty";
				} else if (flag == DONT_MATCH) {
					message = "password do not match";
				} else {
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
											""), password };
					RequestParams params = CommonFunctions.setParams(
							ServerVariables.SCHEME, ServerVariables.AUTHORITY,
							paths);
					Log.d(TAG, params.getURI());
					new SendPasswordAsyncTask().execute(params);
				}
				if (message != null) {
					Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT)
							.show();
				}

			}
		});
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (getActivity().getIntent() != null) {
			if (getActivity().getIntent().hasExtra(
					AppPreferences.IntentExtras.CHANGE)) {

			} else {
				oldEditText.setVisibility(View.GONE);
			}
		} else {
			oldEditText.setVisibility(View.GONE);
		}
	}

	public int getTextFromFields() {

		EditText passwordEdittext = (EditText) view
				.findViewById(R.id.newpassword_edit_password);
		EditText reTypeEdittext = (EditText) view
				.findViewById(R.id.newpassword_edit_reenter);

		password = passwordEdittext.getText().toString();
		reType = reTypeEdittext.getText().toString();
		if (password.isEmpty()) {
			return PASSWORD_EMPTY;
		} else if (reType.isEmpty()) {
			return RETYPE_EMPTY;
		} else if (!password.equals(reType)) {
			return DONT_MATCH;
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
			if (result.contains(SET)) {
				Toast.makeText(getActivity(), "password reset",
						Toast.LENGTH_SHORT).show();
				if (getActivity().getIntent() != null) {
					if (getActivity().getIntent().hasExtra(
							AppPreferences.IntentExtras.CHANGE)) {
						getActivity().setResult(0);
						getActivity().finish();
					} else {
						startActivity(new Intent(getActivity(),
								MainActivity.class));
						getActivity().finishAffinity();
					}
				} else {
					startActivity(new Intent(getActivity(), MainActivity.class));
					getActivity().finishAffinity();
				}
			} else {
				Toast.makeText(getActivity(),
						"connection failure please try again",
						Toast.LENGTH_SHORT).show();
			}
		}

	}

}
