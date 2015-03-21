package com.example.clickforhelp.controllers;

import com.example.clickforhelp.R;
import com.example.clickforhelp.controllers.EmailFragment.EmailFragmentInterface;
import com.example.clickforhelp.controllers.EmailVerificationFragment.EmailVerificationFragmentInterface;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;

public class ForgotPasswordActivity extends Activity implements
		EmailFragmentInterface, EmailVerificationFragmentInterface {
	private FragmentManager fragmentManager;
	private FragmentTransaction fragmentTransaction;
	private final static String EMAILTAG = "EmailFragment";
	private final static String VERFICATIONTAG = "EmailVerficationTAG";
	private final static String NEWPASSWORDTAG = "NEWPASSWORDTAG";
	private final static String TAG = "ForgotPasswordActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgotpassword);
		fragmentManager = getFragmentManager();
		fragmentTransaction = fragmentManager.beginTransaction();
		Fragment fragment = getFragmentManager().findFragmentByTag(EMAILTAG);
		if (fragment == null) {
			Log.d(TAG, "email fragment is null in onCreate");
			fragmentTransaction.replace(R.id.forgotpassword_linear,
					new EmailFragment(), EMAILTAG).commit();
		}
	}

	@Override
	public void replaceWithVerificationCodeFragment() {
		fragmentTransaction = fragmentManager.beginTransaction();
		Fragment fragment = fragmentManager.findFragmentByTag(VERFICATIONTAG);
		if (fragment != null) {
			fragmentTransaction.replace(R.id.forgotpassword_linear,
					fragment, VERFICATIONTAG).commit();
		} else {
			fragment = new EmailVerificationFragment();
			fragmentTransaction.replace(R.id.forgotpassword_linear,
					fragment, VERFICATIONTAG).commit();
		}
		Bundle bundle = new Bundle();
		bundle.putBoolean("new password", true);
		fragment.setArguments(bundle);

	}

	@Override
	public void replaceWithNewPasswordFragment() {
		fragmentTransaction = fragmentManager.beginTransaction();
		Fragment fragment = fragmentManager.findFragmentByTag(NEWPASSWORDTAG);
		if (fragment != null) {
			fragmentTransaction.replace(R.id.forgotpassword_linear,
					fragment, NEWPASSWORDTAG).commit();
		} else {
			fragment = new NewPasswordFragment();
			fragmentTransaction.replace(R.id.forgotpassword_linear,
					fragment, NEWPASSWORDTAG).commit();
		}
	}
}
