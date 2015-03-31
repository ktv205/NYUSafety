package com.example.clickforhelp.controllers.ui.fragments;

import com.example.clickforhelp.R;
import com.example.clickforhelp.controllers.utils.CommonFunctions;
import com.example.clickforhelp.models.AppPreferences;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class LocationSettingsDialogFragment extends DialogFragment {
	View view;
	private String[] locationValues = { "all the time",
			"if the power is 15 percent or more",
			"if plugged into a power source", "never(not recommended)" };
	private SummaryInterface summaryInterface;

	public interface SummaryInterface {
		public void setSummary(int flag);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			summaryInterface = (SummaryInterface) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_location_settings_dialog,
				container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getDialog().setTitle("Send location updates");
		Toast.makeText(getActivity(), "onActivityCreated", Toast.LENGTH_SHORT)
				.show();
		ListView listView = (ListView) view.findViewById(R.id.dialog_list);
		DialogListAdapter dialogAdapter = new DialogListAdapter();
		listView.setAdapter(dialogAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Toast.makeText(getActivity(), "clicked", Toast.LENGTH_SHORT)
						.show();
				RadioButton radioButton = (RadioButton) view
						.findViewById(R.id.list_item_radio);
				SharedPreferences pref =CommonFunctions
						.getSharedPreferences(getActivity(),
								AppPreferences.SharedPrefLocationSettings.name);
				SharedPreferences.Editor edit = pref.edit();
				edit.putInt(
						AppPreferences.SharedPrefLocationSettings.Preference,
						position + 1);
				edit.commit();
				radioButton.setChecked(true);
				summaryInterface.setSummary(position);
				dismiss();

			}
		});
	}

	public class DialogListAdapter extends BaseAdapter {
		int value =CommonFunctions.getSharedPreferences(getActivity(),
				AppPreferences.SharedPrefLocationSettings.name).getInt(
				AppPreferences.SharedPrefLocationSettings.Preference, 2);
		private int LIST_COUNT = 4;

		@Override
		public int getCount() {
			return LIST_COUNT;
		}

		public class Holder {
			TextView text1;
			RadioButton radioButton;

			public Holder(View view) {
				text1 = (TextView) view.findViewById(R.id.list_item_text);
				radioButton = (RadioButton) view
						.findViewById(R.id.list_item_radio);
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			view = convertView;
			Holder holder;

			if (view == null) {
				view = LayoutInflater.from(getActivity()).inflate(
						R.layout.list_item_dialog, parent, false);
				holder = new Holder(view);
				view.setTag(holder);
			} else {
				holder = (Holder) view.getTag();
			}
			holder.radioButton.setChecked(false);
			holder.text1.setText(locationValues[position]);
			if (position == value - 1) {
				holder.radioButton.setChecked(true);
			}
			return view;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

	}

}
