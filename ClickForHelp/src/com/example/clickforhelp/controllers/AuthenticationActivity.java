package com.example.clickforhelp.controllers;

import com.example.clickforhelp.R;
import com.example.clickforhelp.controllers.WelcomeFragment.OnClickAuthentication;
import com.example.clickforhelp.models.AppPreferences;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;

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
		loginFragment = new LoginFragment();
		welcomeFragment = new WelcomeFragment();
		signupFragment = new SignupFragment();
		fragmentManger = getFragmentManager();
		fragmentTransaction = fragmentManger.beginTransaction();
		fragmentTransaction.add(R.id.authentication_parent0_linear,
				welcomeFragment, WELCOMETAG);
		fragmentTransaction.commit();
	}

	@Override
	public void onClickAuthButton(int flag) {
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

	}
}
