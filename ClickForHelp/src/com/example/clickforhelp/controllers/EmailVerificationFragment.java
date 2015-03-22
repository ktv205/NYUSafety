package com.example.clickforhelp.controllers;

import java.util.HashMap;

import com.example.clickforhelp.R;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.RequestParams;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EmailVerificationFragment extends Fragment {
	private final static String TAG = "EmailVerificationFragment";
	private final static int CODE_EMPTY = 0;
	private final static int RESULT_OK = 1;
	private String code;
	View view;
	private EmailVerificationFragmentInterface emailVerificationFragmentInterface;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			emailVerificationFragmentInterface = (EmailVerificationFragmentInterface) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}

	public interface EmailVerificationFragmentInterface {
		public void replaceWithNewPasswordFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_verification, container,
				false);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		TextView resendTextview = (TextView) view
				.findViewById(R.id.fverification_text_resend);
		Button submitButton = (Button) view
				.findViewById(R.id.fverification_button_submit);
		resendTextview.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String[] values = {
						"public",
						"index.php",
						"verificationcode",
						getActivity()
								.getSharedPreferences(
										AppPreferences.SharedPrefAuthentication.name,
										Context.MODE_PRIVATE)
								.getString(
										AppPreferences.SharedPrefAuthentication.user_email,
										"") };
				RequestParams params = CommonFunctions.setParams(
						AppPreferences.ServerVariables.SCHEME,
						AppPreferences.ServerVariables.AUTHORITY, values);
				new RequestVerificationAsyncTask().execute(params);

			}
		});
		submitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int flag = getTextFromFields();
				String message;
				if (flag == CODE_EMPTY) {
					message = "please enter the verification code";
				} else {
					message = "everything looks good";
					String[] values = {
							"public",
							"index.php",
							"verify",
							getActivity()
									.getSharedPreferences(
											AppPreferences.SharedPrefAuthentication.name,
											Context.MODE_PRIVATE)
									.getString(
											AppPreferences.SharedPrefAuthentication.user_email,
											""), code };
					RequestParams params = CommonFunctions.setParams(
							AppPreferences.ServerVariables.SCHEME,
							AppPreferences.ServerVariables.AUTHORITY, values);
				   Log.d(TAG,params.getURI());
					new SendCodeAsyncTask().execute(params);
				}
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT)
						.show();
			}
		});
	}

	public int getTextFromFields() {
		EditText codeEdittext = (EditText) view
				.findViewById(R.id.fverification_edit_code);
		code = codeEdittext.getText().toString();
		int flag;
		if (code.isEmpty()) {
			flag = CODE_EMPTY;
		} else {
			flag = RESULT_OK;
		}
		return flag;

	}

	public class SendCodeAsyncTask extends
			AsyncTask<RequestParams, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Log.d(TAG,"onPreExecute->");
		}
		@Override
		protected String doInBackground(RequestParams... params) {
			return new HttpManager().sendUserData(params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.d(TAG,"onPostExecute->"+result);
			if (result.contains("1")) {
				HashMap<String, String> values = new HashMap<String, String>();
				values.put(AppPreferences.SharedPrefAuthentication.flag, "1");
				new CommonFunctions().saveInPreferences(getActivity(),
						AppPreferences.SharedPrefAuthentication.name, values);
				if (getArguments().containsKey("new password")) {
                     emailVerificationFragmentInterface.replaceWithNewPasswordFragment();
				} else {
					Intent intent = new Intent(getActivity(),
							MainActivity.class);
					getActivity().startActivity(intent);
					getActivity().finish();
				}
			} else {
				Toast.makeText(getActivity(), "entered code is wrong",
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	public class RequestVerificationAsyncTask extends
			AsyncTask<RequestParams, Void, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			
		}

		@Override
		protected String doInBackground(RequestParams... params) {
			return new HttpManager().sendUserData(params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			
			super.onPostExecute(result);
			if (result.contains("1")) {
				Toast.makeText(getActivity(), "code resend", Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(getActivity(), "please try again",
						Toast.LENGTH_SHORT).show();
			}
		}

	}
}
