package com.example.clickforhelp.controllers.ui;

import com.example.clickforhelp.R;
import com.example.clickforhelp.controllers.ui.fragments.EmailVerificationFragment;
import com.example.clickforhelp.controllers.ui.fragments.LoginFragment;
import com.example.clickforhelp.controllers.ui.fragments.SignupFragment;
import com.example.clickforhelp.controllers.ui.fragments.WelcomeFragment;
import com.example.clickforhelp.controllers.ui.fragments.LoginFragment.LoginInterface;
import com.example.clickforhelp.controllers.ui.fragments.SignupFragment.SignupInterface;
import com.example.clickforhelp.controllers.ui.fragments.WelcomeFragment.OnClickAuthentication;
import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.CommonResultAsyncTask.ServerResponse;
import com.example.clickforhelp.models.AppPreferences;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AuthenticationActivity extends Activity implements
		OnClickAuthentication, LoginInterface, SignupInterface, ServerResponse {
	// private final static String TAG = "AuthenticationActivity";
	private final static String WELCOMETAG = "WelcomeFragmentTAG";
	private final static String LOGINTAG = "LoginFragmentTAG";
	private final static String SIGNUPTAG = "SignupFragmentTAG";
	private final static String VERIFYTAG = "EmailVerificationFragmentTAG";
	private FragmentManager mFragmentManager;
	private FragmentTransaction mFragmentTransaction;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authentication);
		if (CommonFunctions.isConnected(this)) {
			mFragmentManager = getFragmentManager();
			mFragmentTransaction = mFragmentManager.beginTransaction();
			Fragment fragment = getFragmentManager().findFragmentByTag(
					WELCOMETAG);
			Fragment loginFragment = getFragmentManager().findFragmentByTag(
					LOGINTAG);
			Fragment signupFragment = getFragmentManager().findFragmentByTag(
					SIGNUPTAG);
			if (fragment == null
					&& (loginFragment == null && signupFragment == null)) {
				// Log.d(TAG, "welcome fragment is null in onCreate");
				mFragmentTransaction.replace(
						R.id.authentication_parent0_linear,
						new WelcomeFragment(), WELCOMETAG).commit();
			}
		} else {
			setNoConnectionView();
		}

	}

	public void setNoConnectionView() {
		setContentView(R.layout.no_connection);
		Button button = (Button) findViewById(R.id.no_connection_retry);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (CommonFunctions.isConnected(AuthenticationActivity.this)) {
					startActivity(new Intent(AuthenticationActivity.this,
							AuthenticationActivity.class));
					finish();
				}

			}
		});
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
		if (!CommonFunctions.isConnected(this)) {
			setNoConnectionView();

		}
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
		mFragmentTransaction = mFragmentManager.beginTransaction();
		if (flag == AppPreferences.Flags.LOGIN_FLAG) {

			Fragment fragment = mFragmentManager.findFragmentByTag(LOGINTAG);
			if (fragment != null) {
				mFragmentTransaction.replace(
						R.id.authentication_parent0_linear, fragment, LOGINTAG);
			} else {
				mFragmentTransaction.replace(
						R.id.authentication_parent0_linear,
						new LoginFragment(), LOGINTAG);
			}

		} else if (flag == AppPreferences.Flags.SIGNUP_FLAG) {
			Fragment fragment = mFragmentManager.findFragmentByTag(SIGNUPTAG);
			if (fragment != null) {
				// Log.d(TAG, "fragment is not null");
				mFragmentTransaction
						.replace(R.id.authentication_parent0_linear, fragment,
								SIGNUPTAG);
			} else {
				mFragmentTransaction.replace(
						R.id.authentication_parent0_linear,
						new SignupFragment(), SIGNUPTAG);
			}
		}
		mFragmentTransaction.commit();
	}

	@Override
	public void onBackPressed() {
		if (mFragmentManager != null) {
			Fragment loginFragment = mFragmentManager
					.findFragmentByTag(LOGINTAG);
			Fragment signupFragment = mFragmentManager
					.findFragmentByTag(SIGNUPTAG);
			Fragment verfiyFragment = mFragmentManager
					.findFragmentByTag(VERIFYTAG);
			if ((loginFragment != null && loginFragment.isVisible())
					|| (signupFragment != null && signupFragment.isVisible())) {
				mFragmentTransaction = mFragmentManager.beginTransaction();
				Fragment fragment = mFragmentManager
						.findFragmentByTag(WELCOMETAG);
				if (fragment != null) {
					mFragmentTransaction.replace(
							R.id.authentication_parent0_linear, fragment,
							WELCOMETAG).commit();
				} else {
					mFragmentTransaction.replace(
							R.id.authentication_parent0_linear,
							new WelcomeFragment(), WELCOMETAG).commit();
				}
			} else if (verfiyFragment != null && verfiyFragment.isVisible()) {
				mFragmentTransaction = mFragmentManager.beginTransaction();
				Fragment fragment = mFragmentManager
						.findFragmentByTag(SIGNUPTAG);
				if (fragment != null) {
					mFragmentTransaction.replace(
							R.id.authentication_parent0_linear, fragment,
							SIGNUPTAG).commit();
				} else {
					mFragmentTransaction.replace(
							R.id.authentication_parent0_linear,
							new SignupFragment(), SIGNUPTAG).commit();
				}

			} else {
				super.onBackPressed();
			}

		} else {
			super.onBackPressed();
		}

	}

	@Override
	public void switchToLogin(int flag) {
		if (flag == AppPreferences.Flags.LOGIN_BACK) {
			mFragmentTransaction = mFragmentManager.beginTransaction();
			Fragment fragment = mFragmentManager.findFragmentByTag(LOGINTAG);
			if (fragment != null) {
				mFragmentTransaction.replace(
						R.id.authentication_parent0_linear, fragment, LOGINTAG)
						.commit();
			} else {
				mFragmentTransaction.replace(
						R.id.authentication_parent0_linear,
						new LoginFragment(), LOGINTAG).commit();
			}
		} else if (flag == AppPreferences.Flags.SIGNUP_SUCCESS) {
			mFragmentTransaction = mFragmentManager.beginTransaction();
			Fragment fragment = mFragmentManager.findFragmentByTag(VERIFYTAG);
			if (fragment != null) {
				mFragmentTransaction
						.replace(R.id.authentication_parent0_linear, fragment,
								VERIFYTAG).commit();
			} else {
				mFragmentTransaction.replace(
						R.id.authentication_parent0_linear,
						new EmailVerificationFragment(), VERIFYTAG).commit();
			}
		}
	}

	@Override
	public void switchToSignup() {
		mFragmentTransaction = mFragmentManager.beginTransaction();
		Fragment fragment = mFragmentManager.findFragmentByTag(SIGNUPTAG);
		if (fragment != null) {
			mFragmentTransaction.replace(R.id.authentication_parent0_linear,
					fragment, SIGNUPTAG).commit();
		} else {
			mFragmentTransaction.replace(R.id.authentication_parent0_linear,
					new SignupFragment(), SIGNUPTAG).commit();
		}
	}

	@Override
	public void IntegerResponse(int response,int flag) {
		Fragment loginFragment = mFragmentManager.findFragmentByTag(LOGINTAG);
		Fragment signupFragment = mFragmentManager.findFragmentByTag(SIGNUPTAG);
		Fragment verificationFragment = mFragmentManager
				.findFragmentByTag(VERIFYTAG);
		if (loginFragment != null && loginFragment.isVisible()) {
			((LoginFragment) loginFragment).responseFromServer(response);
		} else if (signupFragment != null && signupFragment.isVisible()) {
			((SignupFragment) signupFragment).responseFromServer(response);
		} else if (verificationFragment != null
				&& verificationFragment.isVisible()) {
			((EmailVerificationFragment) verificationFragment)
					.responseFromServer(response,flag);

		}

	}
}
