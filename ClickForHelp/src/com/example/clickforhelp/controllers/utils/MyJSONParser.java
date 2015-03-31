package com.example.clickforhelp.controllers.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.clickforhelp.models.LocationDetailsModel;

public class MyJSONParser {
	private static final String ARRAYNAME = "friends";
	private static final String LAT = "latitude";
	private static final String LNG = "longitude";
	private static final String EMAIL = "userid";

	public ArrayList<LocationDetailsModel> parseLocation(String jsonString) {
		ArrayList<LocationDetailsModel> locationArrayList = new ArrayList<LocationDetailsModel>();
		try {
			JSONObject mainObject = new JSONObject(jsonString);
			JSONArray locationsArray = mainObject.getJSONArray(ARRAYNAME);
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
			return locationArrayList;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return locationArrayList;

	}
}
