package com.example.clickforhelp.controllers.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import android.util.Log;

import com.example.clickforhelp.models.AppPreferences;
import com.example.clickforhelp.models.LocationDetailsModel;

public class MyJSONParser {
	private static final String LAT = "latitude";
	private static final String LNG = "longitude";
	private static final String EMAIL = "userid";
	private static final String CODE = "code";
	private static final String DATA = "data";
	// private static final String TAG = MyJSONParser.class.getSimpleName();
	private static final String USER = "user";
	private static final String VICTIM = "victim";
	private static final String HELPER = "helper";
	private static final String HELPERS = "helpers";
	private static final String ROLE = "role";

	public static ArrayList<LocationDetailsModel> parseLocation(
			String jsonString) {
		ArrayList<LocationDetailsModel> locationArrayList = new ArrayList<LocationDetailsModel>();
		int code = 0;
		JSONObject mainObject;
		try {
			mainObject = new JSONObject(jsonString);
			code = mainObject.getInt(CODE);
			if (code == 0) {

				// Do nothing
			} else {
				JSONObject dataObject = mainObject.getJSONObject(DATA);
				JSONArray usersArray = dataObject.getJSONArray(HELPERS);
				for (int i = 0; i < usersArray.length(); i++) {
					JSONObject obj = usersArray.getJSONObject(i);
					LocationDetailsModel myModel = new LocationDetailsModel();
					myModel.setLatitude(obj.getDouble(LAT));
					myModel.setLongitude(obj.getDouble(LNG));
					myModel.setUser_email(obj.getString(EMAIL));
					String role = obj.getString(ROLE);
					if (role.equals(VICTIM)) {
						myModel.setColor(AppPreferences.Flags.VICTIM_COLOR_FLAG);
					} else if (role.equals(USER)) {
						myModel.setColor(AppPreferences.Flags.USER_COLOR_FLAG);
					} else if (role.equals(HELPER)) {
						myModel.setColor(AppPreferences.Flags.HELPER_COLOR_FLAG);
					}

					locationArrayList.add(myModel);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return locationArrayList;

	}

	public static int AuthenticationParser(String jsonString) {
		int code = 0;
		try {
			JSONObject mainObject = new JSONObject(jsonString);
			code = mainObject.getInt(CODE);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return code;
	}
}
