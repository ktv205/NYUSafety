package com.example.clickforhelp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class LocationDetailsModel implements Parcelable {
	private double latitude;
	private double longitude;
	private String user_email;
	private int color;

	public LocationDetailsModel(Parcel source) {
		latitude = source.readDouble();
		longitude = source.readDouble();
		user_email = source.readString();
		color=source.readInt();
	}

	public LocationDetailsModel() {
		// TODO Auto-generated constructor stub
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getUser_email() {
		return user_email;
	}

	public void setUser_email(String user_email) {
		this.user_email = user_email;
	}
	

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<LocationDetailsModel> CREATOR = new Parcelable.Creator<LocationDetailsModel>() {

		@Override
		public LocationDetailsModel createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new LocationDetailsModel(source);
		}

		@Override
		public LocationDetailsModel[] newArray(int size) {
			return new LocationDetailsModel[size];
		}

	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
		dest.writeString(user_email);
		dest.writeInt(color);

	}

}
