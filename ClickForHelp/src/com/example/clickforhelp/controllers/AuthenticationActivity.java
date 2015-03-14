package com.example.clickforhelp.controllers;

import com.example.clickforhelp.R;
import com.example.clickforhelp.controllers.LoginFragment.LoginInterface;
import com.example.clickforhelp.controllers.SignupFragment.SignupInterface;
import com.example.clickforhelp.controllers.WelcomeFragment.OnClickAuthentication;
import com.example.clickforhelp.models.AppPreferences;

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
	private FragmentManager fragmentManager;
	private FragmentTransaction fragmentTransaction;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authentication);
		Log.d(TAG, "in onCreate");
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
		Log.d(TAG, "onStart");
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.d(TAG, "onRestoreInstanceState");

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "inOnResume");
		Intent intent = getIntent();
		fragmentTransaction = fragmentManager.beginTransaction();
		if (intent != null) {
			if (intent
					.hasExtra(AppPreferences.IntentExtras.verificationtoauthentication)) {
				int flag = intent
						.getExtras()
						.getInt(AppPreferences.IntentExtras.verificationtoauthentication);
				if (flag == AppPreferences.Flags.BACK_FLAG) {
					fragmentTransaction.replace(
							R.id.authentication_parent0_linear,
							new SignupFragment(), SIGNUPTAG);
					fragmentTransaction.commit();
				}
			} else {

			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d(TAG, "onSavedInstance");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop");
	}

	@Override
	public void onClickAuthButton(int flag) {
		Log.d(TAG, "in onClickAuthButton->" + flag);
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
		Log.d(TAG, "onBackPressed");
		Log.d(TAG, "backstack->" + fragmentManager.getBackStackEntryCount());
		Fragment loginFragment = fragmentManager.findFragmentByTag(LOGINTAG);
		Fragment signupFragment = fragmentManager.findFragmentByTag(SIGNUPTAG);
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
		} else {
			super.onBackPressed();
		}

	}

	@Override
	public void switchToLogin() {
		Log.d(TAG, "switchToLogin");
		fragmentTransaction = fragmentManager.beginTransaction();
		Fragment fragment = fragmentManager.findFragmentByTag(LOGINTAG);
		if (fragment != null) {
			Log.d(TAG, "fragment is not null");
			fragmentTransaction.replace(R.id.authentication_parent0_linear,
					fragment, LOGINTAG).commit();
		} else {
			Log.d(TAG, "fragment is null");
			fragmentTransaction.replace(R.id.authentication_parent0_linear,
					new LoginFragment(), LOGINTAG).commit();
		}
	}

	@Override
	public void switchToSignup() {
		fragmentTransaction = fragmentManager.beginTransaction();
		Fragment fragment = fragmentManager.findFragmentByTag(SIGNUPTAG);
		if (fragment != null) {
			Log.d(TAG, "fragment is not null");
			fragmentTransaction.replace(R.id.authentication_parent0_linear,
					fragment, SIGNUPTAG).commit();
		} else {
			Log.d(TAG, "fragment is null");
			fragmentTransaction.replace(R.id.authentication_parent0_linear,
					new SignupFragment(), SIGNUPTAG).commit();
		}
	}
}
