package com.example.clickforhelp.controllers;

import com.example.clickforhelp.R;
import com.example.clickforhelp.controllers.LocationSettingsDialogFragment.SummaryInterface;
import com.example.clickforhelp.models.AppPreferences;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SettingsActivity extends Activity implements SummaryInterface {
	private String[] locationValues = { "send updates all the time",
			"send updates if the power is 30 percent or more(recommended)",
			"when plugged in to a power source", "never(not recommended)" };
	private TextView text2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		ListView listView = (ListView) findViewById(R.id.settings_list);
		SettingsListAdapter adapter = new SettingsListAdapter(this, 0);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				if (position == 0) {
					LocationSettingsDialogFragment fragment = new LocationSettingsDialogFragment();
					fragment.show(getFragmentManager(), null);
					text2 = (TextView) view.findViewById(android.R.id.text2);

				} else if (position == 1) {
					Intent intent = new Intent(SettingsActivity.this,
							ForgotPasswordActivity.class);
					intent.putExtra("Extra_Int", 1);
					startActivityForResult(intent,0);
				} else if (position == 2) {
					logout();
				}
			}
		});

	}

	public class SettingsListAdapter extends ArrayAdapter<String> {

		private static final int VIEW_TYPE_COUNT = 2;
		private static final int VIEW_TYPE_TWOTEXT = 0;
		private static final int VIEW_TYPE_SINGLETEXT = 1;
		private static final int VIEW_COUNT = 3;
		private String LocationUpdates = "Location Update Settings";
		private String[] otherSettings = { "Change Password", "Logout" };
		int value = new CommonFunctions().getSharedPreferences(
				SettingsActivity.this,
				AppPreferences.SharedPrefLocationSettings.name).getInt(
				AppPreferences.SharedPrefLocationSettings.Preference, 2);

		public SettingsListAdapter(Context context, int resource) {
			super(context, resource);

		}

		@Override
		public int getCount() {
			return VIEW_COUNT;
		}

		public class Holder {
			TextView textView1, textView2;

			public Holder(View view) {
				textView1 = (TextView) view.findViewById(android.R.id.text1);
				textView2 = (TextView) view.findViewById(android.R.id.text2);

			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			Holder holder;
			if (view == null) {
				if (getItemViewType(position) == VIEW_TYPE_TWOTEXT) {
					view = LayoutInflater.from(SettingsActivity.this).inflate(
							android.R.layout.simple_list_item_2, parent, false);
					holder = new Holder(view);
					view.setTag(holder);
				} else {
					view = LayoutInflater.from(SettingsActivity.this).inflate(
							android.R.layout.simple_list_item_1, parent, false);
					holder = new Holder(view);
				}
			} else {
				holder = (Holder) view.getTag();
			}
			if (getItemViewType(position) == VIEW_TYPE_TWOTEXT) {
				holder.textView1.setText(LocationUpdates);
				holder.textView2.setText(locationValues[value - 1]);
			} else {
				holder.textView1.setText(otherSettings[position - 1]);
			}
			return view;
		}

		@Override
		public int getViewTypeCount() {
			return VIEW_TYPE_COUNT;
		}

		@Override
		public int getItemViewType(int position) {
			return (position == 0) ? VIEW_TYPE_TWOTEXT : VIEW_TYPE_SINGLETEXT;
		}
	}

	public void logout() {
		SharedPreferences pref = new CommonFunctions().getSharedPreferences(
				this, AppPreferences.SharedPrefAuthentication.name);
		SharedPreferences.Editor edit = pref.edit();
		edit.putString(AppPreferences.SharedPrefAuthentication.name, "");
		edit.putString(AppPreferences.SharedPrefAuthentication.user_email, "");
		edit.putString(AppPreferences.SharedPrefAuthentication.flag,"");
		edit.commit();
		Intent intent = new Intent(this, AuthenticationActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finishAffinity();

	}

	@Override
	public void setSummary(int flag) {
		text2.setText(locationValues[flag]);

	}

}
