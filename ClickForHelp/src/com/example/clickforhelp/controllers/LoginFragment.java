package com.example.clickforhelp.controllers;

import com.example.clickforhelp.R;
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

public class LoginFragment extends Fragment {
	private final String TAG = "LoginFragment";
	private View view;
	private String email, password;
	private final static int EMAIL_EMPTY = 1;
	private final static int PASSWORD_EMPTY = 2;
	private final static int RESULT_OK = 5;
	private UserModel user;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_login, container, false);
		Log.d(TAG, "in onCreateView");
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Button submitButton = (Button) view
				.findViewById(R.id.login_button_submit);
		if (CommonFunctions.isConnected(getActivity())) {
			submitButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String message;
					int flag = getTextFromFields();
					if (flag == EMAIL_EMPTY) {
						message = "email cant be empty";
					} else if (flag == PASSWORD_EMPTY) {
						message = "password cant be empty";
					} else {
						message = "everything looks good";
						createUserModel();
						RequestParams params = setParams();
						new SendLoginDetailsAsyncTask().execute(params);
					}
					Toast.makeText(getActivity(), message, Toast.LENGTH_LONG)
							.show();

				}
			});
		} else {
			Toast.makeText(getActivity(), "no network connection",
					Toast.LENGTH_SHORT).show();
		}
	}

	public int getTextFromFields() {
		EditText emailEdittext = (EditText) view
				.findViewById(R.id.login_edit_email);
		EditText passwordEdittext = (EditText) view
				.findViewById(R.id.login_edit_password);
		email = emailEdittext.getText().toString();
		password = passwordEdittext.getText().toString();
		if (email.isEmpty()) {
			return EMAIL_EMPTY;
		} else if (password.isEmpty()) {
			return PASSWORD_EMPTY;
		} else {
			user=new UserModel();
			user.setEmail(email);
			user.setPassword(password);
			return RESULT_OK;
		}
	}

	public void createUserModel() {
		user.setEmail(email);
		user.setPassword(password);
	}

	public RequestParams setParams() {
		RequestParams params = new RequestParams();
		params.setURI("");
		params.setMethod("POST");
		params.setParam("user_email", user.getEmail());
		params.setParam("password", user.getPassword());
		return params;

	}

	public class SendLoginDetailsAsyncTask extends
			AsyncTask<RequestParams, Void, String> {

		@Override
		protected String doInBackground(RequestParams... params) {
			return null;
			// return new HttpManager().sendUserData(params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Intent intent = new Intent(getActivity(),
					MainActivity.class);
			//intent.putExtra(AppPreferences.IntentExtras.signuptoverification,
				//	user);
			getActivity().startActivity(intent);
			getActivity().finish();
		}

		public void jsonString(String result) {
			
		}
	}

}
