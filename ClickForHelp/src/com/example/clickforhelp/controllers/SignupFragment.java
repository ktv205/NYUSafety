package com.example.clickforhelp.controllers;

import com.example.clickforhelp.R;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.RequestParams;
import com.example.clickforhelp.models.UserModel;

import android.app.Fragment;
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

public class SignupFragment extends Fragment {
	private View view;
	private final String TAG = "WelcomeFragment";
	private final static int NAME_EMPTY = 0;
	private final static int EMAIL_EMPTY = 1;
	private final static int PASSWORD_EMPTY = 2;
	private final static int RETYPE_EMPTY = 3;
	private final static int DONT_MATCH = 4;
	private final static int RESULT_OK = 5;
	private final static int INVALID_EMAIL = 6;
	private String name, email, password, reType;
	private UserModel user;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_signup, container, false);
		Log.d(TAG, "in onCreateView");
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Button submitButton = (Button) view
				.findViewById(R.id.signup_button_submit);
		if (CommonFunctions.isConnected(getActivity())) {
			submitButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String message;
					int flag = getTextFromFields();
					if (flag == NAME_EMPTY) {
						message = "name cant be empty";
					} else if (flag == EMAIL_EMPTY) {
						message = "email cant be empty";
					} else if (flag == PASSWORD_EMPTY) {
						message = "password cant be empty";
					} else if (flag == RETYPE_EMPTY) {
						message = "retype cant be empty";
					} else if (flag == DONT_MATCH) {
						message = "password do not match";
					} else if (flag == INVALID_EMAIL) {
						message = "enter valid nyu email";
					} else {
						message = "everything looks good";
						createUserModel();
						RequestParams params = setParams();
						new SendSignupDetailsAsyncTask().execute(params);
					}
					Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT)
							.show();

				}
			});
		} else {
			Toast.makeText(getActivity(), "no network connection",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void createUserModel() {
		user.setName(name);
		user.setEmail(email);
		user.setPassword(password);
	}

	public RequestParams setParams() {
		RequestParams params = new RequestParams();
		params.setURI("");
		params.setMethod("POST");
		params.setParam("user_name", user.getName());
		params.setParam("user_email", user.getEmail());
		params.setParam("password", user.getPassword());
		return params;

	}

	public int getTextFromFields() {
		EditText nameEdittext = (EditText) view
				.findViewById(R.id.signup_edit_name);
		EditText emailEdittext = (EditText) view
				.findViewById(R.id.signup_edit_email);
		EditText passwordEdittext = (EditText) view
				.findViewById(R.id.signup_edit_password);
		EditText reTypeEdittext = (EditText) view
				.findViewById(R.id.signup_edit_repassword);
		name = nameEdittext.getText().toString();
		email = emailEdittext.getText().toString();
		password = passwordEdittext.getText().toString();
		reType = reTypeEdittext.getText().toString();
		if (name.isEmpty()) {
			return NAME_EMPTY;
		} else if (email.isEmpty()) {
			return EMAIL_EMPTY;
		} else if (password.isEmpty()) {
			return PASSWORD_EMPTY;
		} else if (reType.isEmpty()) {
			return RETYPE_EMPTY;
		} else if (!password.equals(reType)) {
			return DONT_MATCH;
		} else if (!validNyuEmail(email)) {
			return INVALID_EMAIL;
		} else {
			user = new UserModel();
			user.setName(name);
			user.setEmail(email);
			user.setPassword(password);
			return RESULT_OK;

		}
	}

	public boolean validNyuEmail(String email) {
		String[] split = email.split("@");
		if (split[1].equals("nyu.edu")) {
			return true;
		} else {
			return false;
		}
	}

	public class SendSignupDetailsAsyncTask extends
			AsyncTask<RequestParams, Void, String> {

		@Override
		protected String doInBackground(RequestParams... params) {
			// TODO Auto-generated method stub
			// return new HttpManager().sendUserData(params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			jsonString(result);
			Intent intent = new Intent(getActivity(),
					EmailVerificationActivity.class);
			intent.putExtra(AppPreferences.IntentExtras.signuptoverification,
					user);
			getActivity().startActivity(intent);
			getActivity().finish();

		}

		public void jsonString(String result) {

		}

	}

}
