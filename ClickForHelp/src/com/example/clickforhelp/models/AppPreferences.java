package com.example.clickforhelp.models;

public class AppPreferences {
	public static abstract class Flags {
		public final static int SIGNUP_FLAG = 1991;
		public final static int LOGIN_FLAG = 1992;
		public final static int VERIFICATION_FLAG = 1993;
		public final static int ACTIVE_FLAG = 1994;
		public final static int BACK_FLAG = 1995;
	}

	public static abstract class IntentExtras {
		public final static String signuptoverification = "signuptoverification";
		public final static String verificationtomain = "verificationtomain";
		public final static String verificationtoauthentication = "verificationtoauthentication";
		public final static String COORDINATES = "coordinates";
		public final static String USERID = "userid";
		public final static String LOCATIONS = "locations";
		public final static String NOCONNECTION = "no connections";
		public final static String CHANGE="change password";
	}

	public static abstract class SharedPrefAuthentication {
		public final static String name = "Authentication";
		public final static String user_name = "user_name";
		public final static String user_email = "user_email";
		public final static String user_id = "user_id";
		public final static String flag = "user_status";
	}

	public static abstract class SharedPrefLocationSettings {
		public final static String name = "LocationUpdatePreference";
		public final static String Preference = "preference";
		public final static int NEVER = 4;
		public final static int ALWAYS = 1;
		public final static int RECOMENDED = 2;
		public final static int PLUGGEDIN = 3;
	}

	public static abstract class ServerVariables {
		public final static String SCHEME = "http";
		public final static String AUTHORITY = "hacksafety.elasticbeanstalk.com";
	}

	public static abstract class Others {
		public final static String LOADING = "Loading...";
	}

}
