package com.example.clickforhelp.controllers;

import com.example.clickforhelp.R;
import com.example.clickforhelp.models.RequestParams;
import com.example.clickforhelp.models.AppPreferences.ServerVariables;

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

public class NewPasswordFragment extends Fragment {
	View view;
	private final static int PASSWORD_EMPTY = 2;
	private final static int RETYPE_EMPTY = 3;
	private final static int DONT_MATCH = 4;
	private final static int RESULT_OK = 5;
	private String password, reType;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater
				.inflate(R.layout.fragment_newpassword, container, false);
		Button submitButton = (Button) view
				.findViewById(R.id.newpassword_button_submit);
		submitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String message;
				int flag = getTextFromFields();
				if (flag == PASSWORD_EMPTY) {
					message = "password cant be empty";
				} else if (flag == RETYPE_EMPTY) {
					message = "retype cant be empty";
				} else if (flag == DONT_MATCH) {
					message = "password do not match";
				} else {
					message = "everything looks good";

					String[] paths = { "public", "index.php" };
					RequestParams params = CommonFunctions.setParams(
							ServerVariables.SCHEME, ServerVariables.AUTHORITY,
							paths);
				}
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT)
						.show();

			}
		});
		return view;
	}

	public int getTextFromFields() {

		EditText passwordEdittext = (EditText) view
				.findViewById(R.id.newpassword_edit_password);
		EditText reTypeEdittext = (EditText) view
				.findViewById(R.id.newpassword_edit_reenter);

		password = passwordEdittext.getText().toString();
		reType = reTypeEdittext.getText().toString();
		if (password.isEmpty()) {
			return PASSWORD_EMPTY;
		} else if (reType.isEmpty()) {
			return RETYPE_EMPTY;
		} else if (!password.equals(reType)) {
			return DONT_MATCH;
		} else {

			return RESULT_OK;

		}
	}

	public class SendPasswordAsyncTask extends
			AsyncTask<RequestParams, Void, String> {

		@Override
		protected String doInBackground(RequestParams... params) {
			return null;
		}

	}

}
