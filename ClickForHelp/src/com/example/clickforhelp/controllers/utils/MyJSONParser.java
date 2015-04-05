package com.example.clickforhelp.controllers.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.example.clickforhelp.models.LocationDetailsModel;

public class MyJSONParser {
	private static final String ARRAYNAME = "friends";
	private static final String LAT = "latitude";
	private static final String LNG = "longitude";
	private static final String EMAIL = "userid";
	private static final String CODE = "code";
	private static final String DATA = "data";
	private static final String TAG = MyJSONParser.class.getSimpleName();

	public static ArrayList<LocationDetailsModel> parseLocation(
			String jsonString) {
		ArrayList<LocationDetailsModel> locationArrayList = new ArrayList<LocationDetailsModel>();
		int code = 0;
		try {
			JSONObject mainObject = new JSONObject(jsonString);
			code = mainObject.getInt(CODE);
			Log.d(TAG,"code->"+code);
			if (code == 0) {
				
				// Do nothing
			} else {
				Log.d(TAG,"here in else");
				JSONObject dataObject = mainObject.getJSONObject(DATA);
				JSONArray locationsArray = dataObject.getJSONArray(ARRAYNAME);
				for (int i = 0; i < locationsArray.length(); i++) {
					JSONObject obj = locationsArray.getJSONObject(i);
					LocationDetailsModel myModel = new LocationDetailsModel();
					myModel.setLatitude(obj.getDouble(LAT));
					myModel.setLongitude(obj.getDouble(LNG));
					if (obj.has(EMAIL)) {
						myModel.setUser_email(obj.getString(EMAIL));
					}
					locationArrayList.add(myModel);
				}
			}
		} catch (JSONException e) {
			Log.d(TAG,"some exception");
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
