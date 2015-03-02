package com.example.clickforhelp.controllers;

import com.example.clickforhelp.R;
import com.example.clickforhelp.controllers.WelcomeFragment.OnClickAuthentication;
import com.example.clickforhelp.models.AppPreferences;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class AuthenticationActivity extends Activity implements
		OnClickAuthentication {
	private WelcomeFragment welcomeFragment;
	private LoginFragment loginFragment;
	private SignupFragment signupFragment;
	private final String TAG = "AuthenticationActivity";
	private final String WELCOMETAG = "WelcomeFragmentTAG";
	private final String LOGINTAG = "LoginFragmentTAG";
	private final String SIGNUPTAG = "SignupFragmentTAG";
	private FragmentManager fragmentManger;
	private FragmentTransaction fragmentTransaction;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authentication);
		Log.d(TAG, "in oncreate");

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "inOnResume");
		loginFragment = new LoginFragment();
		welcomeFragment = new WelcomeFragment();
		signupFragment = new SignupFragment();
		fragmentManger = getFragmentManager();
		fragmentTransaction = fragmentManger.beginTransaction();
		Intent intent = getIntent();
		if (intent != null) {
			if (intent
					.hasExtra(AppPreferences.IntentExtras.verificationtoauthentication)) {
				int flag = intent
						.getExtras()
						.getInt(AppPreferences.IntentExtras.verificationtoauthentication);
				if (flag == AppPreferences.Flags.BACK_FLAG) {
					fragmentTransaction.replace(
							R.id.authentication_parent0_linear, signupFragment,
							SIGNUPTAG);
				}
			} else {
				fragmentTransaction.replace(R.id.authentication_parent0_linear,
						welcomeFragment, WELCOMETAG);
			}
		}

		fragmentTransaction.commit();
	}

	@Override
	public void onClickAuthButton(int flag) {
		if (CommonFunctions.isConnected(getApplicationContext())) {
			fragmentTransaction = fragmentManger.beginTransaction();
			if (flag == AppPreferences.Flags.LOGIN_FLAG) {

				fragmentTransaction.replace(R.id.authentication_parent0_linear,
						loginFragment, LOGINTAG);
			} else {
				fragmentTransaction.replace(R.id.authentication_parent0_linear,
						signupFragment, SIGNUPTAG);
			}
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} else {
			Toast.makeText(this, "no network connection", Toast.LENGTH_SHORT)
					.show();
		}

	}

	@Override
	public void onBackPressed() {
		Fragment fragment = getFragmentManager().findFragmentByTag(SIGNUPTAG);
		if (fragment != null) {
			fragmentTransaction = getFragmentManager().beginTransaction();
			fragmentTransaction.replace(R.id.authentication_parent0_linear,
					welcomeFragment, WELCOMETAG);
			fragmentTransaction.commit();
		} else {
			super.onBackPressed();
		}

	}
}
