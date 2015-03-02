package com.example.clickforhelp.controllers;

import com.example.clickforhelp.R;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.RequestParams;
import com.example.clickforhelp.models.UserModel;
import com.example.clickforhelp.models.AppPreferences.SharedPref;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EmailVerificationActivity extends Activity {
	private UserModel user;
	private final static int CODE_EMPTY = 0;
	private final static int RESULT_OK = 1;
	private String code;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_emailverification);
		Intent intent = getIntent();
		setSharedPreferencesFromIntent(intent);
		TextView resendTextview = (TextView) findViewById(R.id.verification_text_resend);
		Button submitButton = (Button) findViewById(R.id.verification_button_submit);
		resendTextview.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

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
					RequestParams params = setParams();
					new SendCodeAsyncTask().execute(params);
				}
				Toast.makeText(EmailVerificationActivity.this, message,
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	public int getTextFromFields() {
		EditText codeEdittext = (EditText) findViewById(R.id.verification_edit_code);
		code = codeEdittext.getText().toString();
		int flag;
		if (code.isEmpty()) {
			flag = CODE_EMPTY;
		} else {
			flag = RESULT_OK;
		}
		return flag;

	}

	public RequestParams setParams() {
		RequestParams params = new RequestParams();
		params.setURI("");
		params.setMethod("POST");
		params.setParam("user_code", "");
		return params;

	}

	public void setSharedPreferencesFromIntent(Intent intent) {
		SharedPreferences pref = getSharedPreferences(SharedPref.name,
				MODE_PRIVATE);
		SharedPreferences.Editor edit = pref.edit();
		user = intent
				.getParcelableExtra(AppPreferences.IntentExtras.signuptoverification);
		edit.putString(AppPreferences.SharedPref.user_name, user.getName());
		edit.putString(AppPreferences.SharedPref.user_email, user.getEmail());
		edit.putInt(AppPreferences.SharedPref.flag,
				AppPreferences.Flags.VERIFICATION_FLAG);
		edit.commit();
	}

	public class SendCodeAsyncTask extends
			AsyncTask<RequestParams, Void, String> {

		@Override
		protected String doInBackground(RequestParams... params) {
			return null;
			// return new HttpManager().sendUserData(params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			jsonString(result);
			Intent intent = new Intent(EmailVerificationActivity.this,
					MainActivity.class);
			// intent.putExtra(AppPreferences.IntentExtras.signuptoverification,
			// user);
			EmailVerificationActivity.this.startActivity(intent);

		}

		public void jsonString(String result) {

		}

	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, AuthenticationActivity.class);
		intent.putExtra(
				AppPreferences.IntentExtras.verificationtoauthentication,
				AppPreferences.Flags.BACK_FLAG);
		startActivity(intent);
		finish();
	}

}
