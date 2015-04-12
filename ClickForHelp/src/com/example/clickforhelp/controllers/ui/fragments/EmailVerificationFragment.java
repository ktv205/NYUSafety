package com.example.clickforhelp.controllers.ui.fragments;

import java.util.HashMap;

import com.example.clickforhelp.R;
import com.example.clickforhelp.controllers.ui.ForgotPasswordActivity;
import com.example.clickforhelp.controllers.ui.HelperActivity;
import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.controllers.utils.CommonResultAsyncTask;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.RequestParams;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
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
	private final static String TAG = EmailVerificationFragment.class
			.getSimpleName();
	private final static int CODE_EMPTY = 0, RESULT_OK = 1, CODE_ACCEPTED = 1,
			RESEND_CODE = 1, CODE_NOT_ACCEPTED = 0, RESEND_CODE_FAILED = 0;
	private String mCode;
	private static final String CODE_SENT_WAITING = "Please wait while we verify the code",
			CODE_REQUEST_WATING = "Please wait while we resend the code";
	View mView;
	private EmailVerificationFragmentInterface mEmailVerificationFragmentInterface;
	private Button mSubmitButton;
	private static final int RESEND_FLAG = 0;
	private static final int SUBMIT_FLAG = 1;
	private static final String VERFICATION_CODE = "verificationcode";
	private static final String VERIFY = "verify";
	private boolean mIsForgotPassword = false;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (getActivity()
				.getComponentName()
				.getShortClassName()
				.equals(".controllers.ui."
						+ ForgotPasswordActivity.class.getSimpleName())) {
			try {
				mEmailVerificationFragmentInterface = (EmailVerificationFragmentInterface) activity;
			} catch (ClassCastException e) {
				throw new ClassCastException(activity.toString()
						+ " must implement OnHeadlineSelectedListener");
			}
		}
	}

	public interface EmailVerificationFragmentInterface {
		public void replaceWithNewPasswordFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_verification, container,
				false);
		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getActivity().getActionBar().setTitle(R.string.verify);
		checkForIntent();
		TextView resendTextview = (TextView) mView
				.findViewById(R.id.fverification_text_resend);
		mSubmitButton = (Button) mView
				.findViewById(R.id.fverification_button_submit);
		resendTextview.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String[] values = {
						VERFICATION_CODE,
						CommonFunctions.getEmail(getActivity()
								.getApplicationContext()) };
				RequestParams params = CommonFunctions.setParams(values);
				new CommonResultAsyncTask(getActivity(), CODE_REQUEST_WATING,
						RESEND_FLAG).execute(params);

			}
		});
		mSubmitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int flag = getTextFromFields();
				String message;
				if (flag == CODE_EMPTY) {
					message = "please enter the verification code";
				} else {
					message = "everything looks good";
					String[] paths = {
							VERIFY,
							getActivity()
									.getSharedPreferences(
											AppPreferences.SharedPrefAuthentication.name,
											Context.MODE_PRIVATE)
									.getString(
											AppPreferences.SharedPrefAuthentication.user_email,
											""), mCode };
					RequestParams params = CommonFunctions.setParams(paths);
					Log.d(TAG, params.getURI());
					new CommonResultAsyncTask(getActivity(), CODE_SENT_WAITING,
							SUBMIT_FLAG).execute(params);
					mSubmitButton.setEnabled(false);
				}
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT)
						.show();
			}
		});
	}

	public void checkForIntent() {
		if (getArguments() != null) {
			if (getArguments().containsKey(
					AppPreferences.IntentExtras.NEW_PASSWORD)) {
				mIsForgotPassword = true;

			}
		}

	}

	public int getTextFromFields() {
		EditText codeEdittext = (EditText) mView
				.findViewById(R.id.fverification_edit_code);
		mCode = codeEdittext.getText().toString();
		int flag;
		if (mCode.isEmpty()) {
			flag = CODE_EMPTY;
		} else {
			flag = RESULT_OK;
		}
		return flag;

	}

	public void responseFromServer(int code, int flag) {
		if (flag == SUBMIT_FLAG) {
			if (code == CODE_ACCEPTED) {

				if (mIsForgotPassword) {
					mEmailVerificationFragmentInterface
							.replaceWithNewPasswordFragment();
				} else {
					HashMap<String, String> values = new HashMap<String, String>();
					values.put(AppPreferences.SharedPrefAuthentication.flag,
							AppPreferences.SharedPrefAuthentication.FLAG_ACTIVE);
					CommonFunctions.saveInPreferences(getActivity(),
							AppPreferences.SharedPrefAuthentication.name,
							values);
					Intent intent = new Intent(getActivity(),
							HelperActivity.class);
					getActivity().startActivity(intent);
					getActivity().finish();
				}

			} else if (code == CODE_NOT_ACCEPTED) {
				Toast.makeText(getActivity(),
						"entered code is wrong please try again",
						Toast.LENGTH_SHORT).show();
				mSubmitButton.setEnabled(true);
			} else {
				Toast.makeText(getActivity(),
						"something went wrong please try again",
						Toast.LENGTH_SHORT).show();
			}
		} else if (flag == RESEND_FLAG) {
			String message = "some thing went wrong please try again";
			if (code == RESEND_CODE) {
				message = "code resent";
			} else if (code == RESEND_CODE_FAILED) {
				message = "please try again";
			}
			Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
		}

	}
}
