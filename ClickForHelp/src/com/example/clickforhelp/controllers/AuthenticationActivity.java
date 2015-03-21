package com.example.clickforhelp.controllers;

import com.example.clickforhelp.R;
import com.example.clickforhelp.controllers.LoginFragment.LoginInterface;
import com.example.clickforhelp.controllers.SignupFragment.SignupInterface;
import com.example.clickforhelp.controllers.WelcomeFragment.OnClickAuthentication;
import com.example.clickforhelp.models.AppPreferences;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class AuthenticationActivity extends Activity implements
		OnClickAuthentication, LoginInterface, SignupInterface {
	private final static String TAG = "AuthenticationActivity";
	private final static String WELCOMETAG = "WelcomeFragmentTAG";
	private final static String LOGINTAG = "LoginFragmentTAG";
	private final static String SIGNUPTAG = "SignupFragmentTAG";
	private final static String VERIFYTAG = "EmailVerificationFragmentTAG";
	private FragmentManager fragmentManager;
	private FragmentTransaction fragmentTransaction;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authentication);
		ActionBar bar = getActionBar();
		bar.setIcon(R.drawable.nyu_white);
		if (!getSharedPreferences(AppPreferences.SharedPrefAuthentication.name, MODE_PRIVATE)
				.getString(AppPreferences.SharedPrefAuthentication.user_email, "").isEmpty()
				&& getSharedPreferences(AppPreferences.SharedPrefAuthentication.name,
						MODE_PRIVATE).getString(AppPreferences.SharedPrefAuthentication.flag,
						"").equals("1")) {
			startActivity(new Intent(this, MainActivity.class));
			finish();
		}
		fragmentManager = getFragmentManager();
		fragmentTransaction = fragmentManager.beginTransaction();
		Fragment fragment = getFragmentManager().findFragmentByTag(WELCOMETAG);
		Fragment loginFragment = getFragmentManager().findFragmentByTag(
				LOGINTAG);
		Fragment signupFragment = getFragmentManager().findFragmentByTag(
				SIGNUPTAG);
		if (fragment == null
				&& (loginFragment == null && signupFragment == null)) {
			Log.d(TAG, "welcome fragment is null in onCreate");
			fragmentTransaction.replace(R.id.authentication_parent0_linear,
					new WelcomeFragment(), WELCOMETAG).commit();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onClickAuthButton(int flag) {
		fragmentTransaction = fragmentManager.beginTransaction();
		if (flag == AppPreferences.Flags.LOGIN_FLAG) {

			Fragment fragment = fragmentManager.findFragmentByTag(LOGINTAG);
			if (fragment != null) {
				fragmentTransaction.replace(R.id.authentication_parent0_linear,
						fragment, LOGINTAG);
			} else {
				fragmentTransaction.replace(R.id.authentication_parent0_linear,
						new LoginFragment(), LOGINTAG);
			}

		} else if (flag == AppPreferences.Flags.SIGNUP_FLAG) {
			Fragment fragment = fragmentManager.findFragmentByTag(SIGNUPTAG);
			if (fragment != null) {
				Log.d(TAG, "fragment is not null");
				fragmentTransaction.replace(R.id.authentication_parent0_linear,
						fragment, SIGNUPTAG);
			} else {
				fragmentTransaction.replace(R.id.authentication_parent0_linear,
						new SignupFragment(), SIGNUPTAG);
			}
		}
		fragmentTransaction.commit();
	}

	@Override
	public void onBackPressed() {
		Fragment loginFragment = fragmentManager.findFragmentByTag(LOGINTAG);
		Fragment signupFragment = fragmentManager.findFragmentByTag(SIGNUPTAG);
		Fragment verfiyFragment = fragmentManager.findFragmentByTag(VERIFYTAG);
		if ((loginFragment != null && loginFragment.isVisible())
				|| (signupFragment != null && signupFragment.isVisible())) {
			fragmentTransaction = fragmentManager.beginTransaction();
			Fragment fragment = fragmentManager.findFragmentByTag(WELCOMETAG);
			if (fragment != null) {
				fragmentTransaction.replace(R.id.authentication_parent0_linear,
						fragment, WELCOMETAG).commit();
			} else {
				fragmentTransaction.replace(R.id.authentication_parent0_linear,
						new WelcomeFragment(), WELCOMETAG).commit();
			}
		} else if (verfiyFragment != null && verfiyFragment.isVisible()) {
			fragmentTransaction = fragmentManager.beginTransaction();
			Fragment fragment = fragmentManager.findFragmentByTag(SIGNUPTAG);
			if (fragment != null) {
				fragmentTransaction.replace(R.id.authentication_parent0_linear,
						fragment, SIGNUPTAG).commit();
			} else {
				fragmentTransaction.replace(R.id.authentication_parent0_linear,
						new SignupFragment(), SIGNUPTAG).commit();
			}

		} else {
			super.onBackPressed();
		}

	}

	@Override
	public void switchToLogin(int flag) {
		if (flag == 1) {
			fragmentTransaction = fragmentManager.beginTransaction();
			Fragment fragment = fragmentManager.findFragmentByTag(LOGINTAG);
			if (fragment != null) {
				fragmentTransaction.replace(R.id.authentication_parent0_linear,
						fragment, LOGINTAG).commit();
			} else {
				fragmentTransaction.replace(R.id.authentication_parent0_linear,
						new LoginFragment(), LOGINTAG).commit();
			}
		} else {
			fragmentTransaction = fragmentManager.beginTransaction();
			Fragment fragment = fragmentManager.findFragmentByTag(VERIFYTAG);
			if (fragment != null) {
				fragmentTransaction.replace(R.id.authentication_parent0_linear,
						fragment, VERIFYTAG).commit();
			} else {
				fragmentTransaction.replace(R.id.authentication_parent0_linear,
						new EmailVerificationFragment(), VERIFYTAG).commit();
			}
		}
	}

	@Override
	public void switchToSignup() {
		fragmentTransaction = fragmentManager.beginTransaction();
		Fragment fragment = fragmentManager.findFragmentByTag(SIGNUPTAG);
		if (fragment != null) {
			fragmentTransaction.replace(R.id.authentication_parent0_linear,
					fragment, SIGNUPTAG).commit();
		} else {
			fragmentTransaction.replace(R.id.authentication_parent0_linear,
					new SignupFragment(), SIGNUPTAG).commit();
		}
	}
}
