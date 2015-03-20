package com.example.clickforhelp.controllers;

import com.example.clickforhelp.R;
import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.RequestParams;

import android.app.Fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EmailFragment extends Fragment {
	//private final static String TAG = "EmailFragment";
	private final static int EMAIL_EMPTY = 0;
	private final static int RESULT_OK = 1;
	private final static int INVALID_EMAIL = 6;
	private String email;
	View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_email, container, false);
		Button emailButton = (Button) view
				.findViewById(R.id.email_button_submit);
		emailButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int flag = getTextFromFields();
				String message;
				if (flag == EMAIL_EMPTY) {
					message = "please enter the nyu email";
				} else if (flag == INVALID_EMAIL) {
					message = "enter valid nyu email id";
				} else {
					message = "everything looks good";
					String[] values = {
							"public",
							"index.php",
							 };
					RequestParams params = CommonFunctions.setParams(
							AppPreferences.ServerVariables.SCHEME,
							AppPreferences.ServerVariables.AUTHORITY, values);
				}
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT)
						.show();

			}
		});
		return view;
	}

	public int getTextFromFields() {
		EditText emailEdittext = (EditText) view
				.findViewById(R.id.email_edit_email);
		email = emailEdittext.getText().toString();
		int flag;
		if (email.isEmpty()) {
			flag = EMAIL_EMPTY;
		} else if (!new CommonFunctions().validNyuEmail(email)) {
			flag = INVALID_EMAIL;
		} else {
			flag = RESULT_OK;
		}
		return flag;

	}
	public class SendEmailAsyncTask extends AsyncTask<RequestParams, Void, String>{

		@Override
		protected String doInBackground(RequestParams... params) {
			
			return null;
		}
		
	}

}
