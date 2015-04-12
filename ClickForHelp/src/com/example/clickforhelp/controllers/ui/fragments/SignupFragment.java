package com.example.clickforhelp.controllers.ui.fragments;

import java.util.HashMap;

import com.example.clickforhelp.R;
import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.CommonResultAsyncTask;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.RequestParams;
import com.example.clickforhelp.models.UserModel;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SignupFragment extends Fragment {
	private View mView;
	// private final String TAG = SignupFragment.class.getSimpleName();
	private final static int NAME_EMPTY = 0, EMAIL_EMPTY = 1,
			PASSWORD_EMPTY = 2, RETYPE_EMPTY = 3, DONT_MATCH = 4,
			RESULT_OK = 5, INVALID_EMAIL = 6, PHONE_EMPTY = 7, NEXT_STEP = 1,
			NOT_VERIFIED = -1, ACTIVE = -2, ERROR = 0;
	private Button mSubmitButton;
	private String mName, mEmail, mPassword, mReType, mPhone;
	private UserModel mUser;
	private static final String ADD_USER="adduser";
	private static final String DETAILS_WAIT = "please wait while we process your details";

	private SignupInterface mSignupInterface;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mSignupInterface = (SignupInterface) activity;
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
		mView = inflater.inflate(R.layout.fragment_signup, container, false);
		return mView;
	}

	public interface SignupInterface {
		public void switchToLogin(int flag);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getActivity().getActionBar().setTitle(R.string.string_button_signup);
		TextView loginBack = (TextView) mView.findViewById(R.id.login_back);
		loginBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mSignupInterface.switchToLogin(AppPreferences.Flags.LOGIN_BACK);

			}
		});
		mSubmitButton = (Button) mView.findViewById(R.id.signup_button_submit);
		if (CommonFunctions.isConnected(getActivity())) {
			mSubmitButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String message = null;
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
						createUserModel();
						String[] paths={ADD_USER,
							mEmail, mName, mPassword, mPhone };
						RequestParams params = CommonFunctions.setParams(paths);
						new CommonResultAsyncTask(getActivity(), DETAILS_WAIT,
								0).execute(params);
						mSubmitButton.setEnabled(false);
					}
					if (message != null) {
						Toast.makeText(getActivity(), message,
								Toast.LENGTH_SHORT).show();
					}

				}
			});
		} else {
			Toast.makeText(getActivity(), "no network connection",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void createUserModel() {
		mUser = new UserModel();
		mUser.setName(mName);
		mUser.setEmail(mEmail);
		mUser.setPassword(mPassword);
		HashMap<String, String> values = new HashMap<String, String>();
		values.put(AppPreferences.SharedPrefAuthentication.user_name, mName);
		values.put(AppPreferences.SharedPrefAuthentication.user_email, mEmail);
		values.put(AppPreferences.SharedPrefAuthentication.flag,
				AppPreferences.SharedPrefAuthentication.FLAG_INACTIVE);
		values.put(AppPreferences.SharedPrefAuthentication.password, mPassword);
		CommonFunctions.saveInPreferences(getActivity(),
				AppPreferences.SharedPrefAuthentication.name, values);
	}

	public int getTextFromFields() {
		EditText nameEdittext = (EditText) mView
				.findViewById(R.id.signup_edit_name);
		EditText emailEdittext = (EditText) mView
				.findViewById(R.id.signup_edit_email);
		EditText passwordEdittext = (EditText) mView
				.findViewById(R.id.signup_edit_password);
		EditText reTypeEdittext = (EditText) mView
				.findViewById(R.id.signup_edit_repassword);
		EditText phoneEdittext = (EditText) mView
				.findViewById(R.id.signup_edit_phone);
		mName = nameEdittext.getText().toString();
		mEmail = emailEdittext.getText().toString();
		mPassword = passwordEdittext.getText().toString();
		mReType = reTypeEdittext.getText().toString();
		mPhone = phoneEdittext.getText().toString();
		if (mName.isEmpty()) {
			return NAME_EMPTY;
		} else if (mEmail.isEmpty()) {
			return EMAIL_EMPTY;
		} else if (mPassword.isEmpty()) {
			return PASSWORD_EMPTY;
		} else if (mReType.isEmpty()) {
			return RETYPE_EMPTY;
		} else if (!mPassword.equals(mReType)) {
			return DONT_MATCH;
		} else if (!CommonFunctions.validNyuEmail(mEmail)) {
			return INVALID_EMAIL;
		} else if (mPhone.isEmpty() || mPhone.length() > 10
				|| mPhone.length() < 10) {
			return PHONE_EMPTY;
		} else {
			mUser = new UserModel();
			return RESULT_OK;

		}
	}

	public void responseFromServer(int code) {
		String message = "some thing went wrong please try again";
		if (code == NEXT_STEP) {
			mSignupInterface.switchToLogin(AppPreferences.Flags.SIGNUP_SUCCESS);
		} else if (code == NOT_VERIFIED) {
			message = "Already registered please check your mail for verification code";
			mSignupInterface.switchToLogin(AppPreferences.Flags.SIGNUP_SUCCESS);
		} else if (code == ACTIVE) {
			message = "Already an active user please click on login";
		} else if (code == ERROR) {
			mSubmitButton.setEnabled(true);
			// initialized error message
		}
		if (code != NEXT_STEP) {
			Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
		}

	}

}
