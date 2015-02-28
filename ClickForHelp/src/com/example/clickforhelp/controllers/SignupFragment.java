package com.example.clickforhelp.controllers;

import com.example.clickforhelp.R;
import com.example.clickforhelp.R.layout;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SignupFragment extends Fragment {
	private View view;
	private final String TAG="WelcomeFragment";
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view=inflater.inflate(R.layout.fragment_signup, container, false);
		Log.d(TAG,"in onCreateView");
		return view;
	}

}
