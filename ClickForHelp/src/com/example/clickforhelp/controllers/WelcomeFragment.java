package com.example.clickforhelp.controllers;

import com.example.clickforhelp.R;
import com.example.clickforhelp.R.id;
import com.example.clickforhelp.R.layout;
import com.example.clickforhelp.models.AppPreferences;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class WelcomeFragment extends Fragment {
	private View view;
	private final String TAG = "WelcomeFragment";
	private OnClickAuthentication auth;
	
	public interface OnClickAuthentication {
		public void onClickAuthButton(int flag);
	}
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d(TAG, "in onAttach");
		try {
			Log.d("connected", "in onAttach");
			auth = (OnClickAuthentication) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_welcome, container, false);
		Log.d(TAG, "in onCreateView");
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Button SignupButton = (Button) view
				.findViewById(R.id.welcome_button_signup);
		TextView loginTextView = (TextView) view
				.findViewById(R.id.welcome_text_login);
		SignupButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				auth.onClickAuthButton(AppPreferences.Flags.SIGNUP_FLAG);

			}
		});
		loginTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				auth.onClickAuthButton(AppPreferences.Flags.LOGIN_FLAG);

			}
		});

	}

}