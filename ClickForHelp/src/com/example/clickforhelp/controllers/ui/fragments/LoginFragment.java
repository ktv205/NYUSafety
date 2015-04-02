package com.example.clickforhelp.controllers.ui.fragments;

import java.util.HashMap;

import com.example.clickforhelp.R;
import com.example.clickforhelp.controllers.ui.ForgotPasswordActivity;
import com.example.clickforhelp.controllers.ui.HelperActivity;
import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.HttpManager;
import com.example.clickforhelp.controllers.utils.MyJSONParser;
import com.example.clickforhelp.models.AppPreferences;
//import com.example.clickforhelp.controllers.SignupFragment.SignupInterface;
import com.example.clickforhelp.models.AppPreferences.ServerVariables;
import com.example.clickforhelp.models.RequestParams;
import com.example.clickforhelp.models.UserModel;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
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
import android.widget.TextView;
import android.widget.Toast;

public class LoginFragment extends Fragment {
	private final String TAG = LoginFragment.class.getSimpleName();
	private View mView;
	private String mEmail, mPassword;
	private final static int EMAIL_EMPTY = 1;
	private final static int PASSWORD_EMPTY = 2;
	private final static int RESULT_OK = 5;
	private UserModel mUser;
	private final static int LOGIN_SUCCESS = 1;

	private LoginInterface mLoginInterface;
	
	private Button mLoginButton;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mLoginInterface = (LoginInterface) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_login, container, false);
		return mView;
	}

	public interface LoginInterface {
		public void switchToSignup();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getActivity().getActionBar().setTitle(R.string.string_text_login);
		TextView signupBack = (TextView) mView.findViewById(R.id.signup_back);
		signupBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mLoginInterface.switchToSignup();

			}
		});
		TextView forgotText = (TextView)mView
				.findViewById(R.id.login_text_forgot);
		forgotText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(),
						ForgotPasswordActivity.class);
				getActivity().startActivity(intent);

			}
		});
		mLoginButton = (Button)mView
				.findViewById(R.id.login_button_submit);
		if (CommonFunctions.isConnected(getActivity())) {
			mLoginButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String message = null;
					int flag = getTextFromFields();
					if (flag == EMAIL_EMPTY) {
						message = "email cant be empty";
					} else if (flag == PASSWORD_EMPTY) {
						message = "password cant be empty";
					} else {
						createUserModel();
						String[] paths = { "public", "index.php", "login",
								mEmail, mPassword };
						RequestParams params = CommonFunctions.setParams(
								ServerVariables.SCHEME,
								ServerVariables.AUTHORITY, paths);
						new SendLoginDetailsAsyncTask().execute(params);
					}
					if (message != null) {
						Toast.makeText(getActivity(), message,
								Toast.LENGTH_LONG).show();
					}

				}
			});
		} else {
			Toast.makeText(getActivity(), "no network connection",
					Toast.LENGTH_SHORT).show();
		}
	}

	public int getTextFromFields() {
		EditText emailEdittext = (EditText) mView
				.findViewById(R.id.login_edit_email);
		EditText passwordEdittext = (EditText) mView
				.findViewById(R.id.login_edit_password);
		mEmail = emailEdittext.getText().toString();
		mPassword = passwordEdittext.getText().toString();
		if (mEmail.isEmpty()) {
			return EMAIL_EMPTY;
		} else if (mPassword.isEmpty()) {
			return PASSWORD_EMPTY;
		} else {
			mUser = new UserModel();
			return RESULT_OK;
		}
	}

	public void createUserModel() {
		mUser.setEmail(mEmail);
		mUser.setPassword(mPassword);
	}

	public class SendLoginDetailsAsyncTask extends
			AsyncTask<RequestParams, Void, String> {
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(getActivity());
			dialog.setTitle("Loading...");
			dialog.setMessage("Please wait while we log you in");
			dialog.show();

		}

		@Override
		protected String doInBackground(RequestParams... params) {
			return new HttpManager().sendUserData(params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			dialog.dismiss();
			Log.d(TAG,result);
			if (result != null) {
				int code=MyJSONParser.AuthenticationParser(result);
				if (code==LOGIN_SUCCESS) {
					HashMap<String, String> values = new HashMap<String, String>();
					values.put(
							AppPreferences.SharedPrefAuthentication.user_email,
							mUser.getEmail());
					values.put(
							AppPreferences.SharedPrefAuthentication.password,
							mPassword);
					values.put(AppPreferences.SharedPrefAuthentication.flag,
							AppPreferences.SharedPrefAuthentication.FLAG_ACTIVE);
					CommonFunctions.saveInPreferences(getActivity(),
							AppPreferences.SharedPrefAuthentication.name,
							values);
					Intent intent = new Intent(getActivity(),
							HelperActivity.class);
					getActivity().startActivity(intent);
					getActivity().finish();
				} else {
					Toast.makeText(getActivity(), "email or password mismatch",
							Toast.LENGTH_SHORT).show();
				}
			}else{
				Toast.makeText(getActivity(), "some thing went wrong please try again",Toast.LENGTH_SHORT).show();
			}
		}
	}

}
