package com.example.clickforhelp.controllers;

import java.util.HashMap;

import com.example.clickforhelp.R;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.AppPreferences.ServerVariables;
import com.example.clickforhelp.models.RequestParams;
import com.example.clickforhelp.models.UserModel;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SignupFragment extends Fragment {
	private View view;
	private final String TAG = "SignupFragment";
	private final static int NAME_EMPTY = 0;
	private final static int EMAIL_EMPTY = 1;
	private final static int PASSWORD_EMPTY = 2;
	private final static int RETYPE_EMPTY = 3;
	private final static int DONT_MATCH = 4;
	private final static int RESULT_OK = 5;
	private final static int INVALID_EMAIL = 6;
	private final static int PHONE_EMPTY = 7;
	private String name, email, password, reType, phone;
	private UserModel user;

	private SignupInterface signupInterface;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d(TAG, "in onAttach");
		try {
			Log.d("connected", "in onAttach");
			signupInterface = (SignupInterface) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_signup, container, false);
		Log.d(TAG, "in onCreateView");
		return view;
	}

	public interface SignupInterface {
		public void switchToLogin(int flag);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		TextView loginBack = (TextView) view.findViewById(R.id.login_back);
		loginBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				signupInterface.switchToLogin(1);

			}
		});
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
					} else if (flag == PHONE_EMPTY) {
						message = "enter a valid phone number";
					} else {
						message = "everything looks good";
						createUserModel();
						String[] paths = { "public", "index.php", "adduser",
								email, name, password, phone };
						RequestParams params = CommonFunctions.setParams(
								ServerVariables.SCHEME,
								ServerVariables.AUTHORITY, paths);
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
		user = new UserModel();
		user.setName(name);
		user.setEmail(email);
		user.setPassword(password);
		HashMap<String, String> values = new HashMap<String, String>();
		values.put(AppPreferences.SharedPref.user_name, name);
		values.put(AppPreferences.SharedPref.user_email, email);
		values.put(AppPreferences.SharedPref.flag, "0");
		new CommonFunctions().saveInPreferences(getActivity(),
				AppPreferences.SharedPref.name, values);
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
		EditText phoneEdittext = (EditText) view
				.findViewById(R.id.signup_edit_phone);
		name = nameEdittext.getText().toString();
		email = emailEdittext.getText().toString();
		password = passwordEdittext.getText().toString();
		reType = reTypeEdittext.getText().toString();
		phone = phoneEdittext.getText().toString();
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
		} else if (phone.isEmpty() || phone.length() > 10
				|| phone.length() < 10) {
			return PHONE_EMPTY;
		} else {
			user = new UserModel();
			return RESULT_OK;

		}
	}

	public boolean validNyuEmail(String email) {
		String[] split = email.split("@");
		if (split.length > 1) {
			if (split[1].equals("nyu.edu")) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public class SendSignupDetailsAsyncTask extends
			AsyncTask<RequestParams, Void, String> {

		@Override
		protected String doInBackground(RequestParams... params) {
			// return null;
			return new HttpManager().sendUserData(params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null) {
				Log.d(TAG, "result is not null->" + result);
				if (result.contains("1")) {
					signupInterface.switchToLogin(2);
				} else {
					Toast.makeText(getActivity(),
							"something went wrong please signup again",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Log.d(TAG, "result is null");
				Toast.makeText(getActivity(),
						"something went wrong please signup again",
						Toast.LENGTH_SHORT).show();
			}

		}

	}

}
