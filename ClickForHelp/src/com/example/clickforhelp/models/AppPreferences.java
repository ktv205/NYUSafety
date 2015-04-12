package com.example.clickforhelp.models;

public class AppPreferences {
	public static final String GOOGLEREGID = "947264921784";
	public static int tryConnectionCounter = 0;

	public static abstract class Flags {
		public final static int SIGNUP_FLAG = 1991;
		public final static int LOGIN_FLAG = 1992;
		public final static int VERIFICATION_FLAG = 1993;
		public final static int ACTIVE_FLAG = 1994;
		public final static int BACK_FLAG = 1995;
		public final static int LOGIN_BACK = 1996;
		public final static int SIGNUP_SUCCESS = 1998;
		public final static int HELPER_COLOR_FLAG = 1999;
		public final static int VICTIM_COLOR_FLAG = 2000;
		public final static int USER_COLOR_FLAG = 2001;

	}

	public static abstract class IntentExtras {
		public final static String signuptoverification = "signuptoverification";
		public final static String verificationtomain = "verificationtomain";
		public final static String verificationtoauthentication = "verificationtoauthentication";
		public final static String COORDINATES = "coordinates";
		public final static String USERID = "userid";
		public final static String LOCATIONS = "locations";
		public final static String NOCONNECTION = "no connections";
		public final static String CHANGE = "change password";
		public final static String HIGH_ACCURACY = "high_accuracy";
		public final static String BALANCED_POWER = "balanced_power";
		public final static String NEW_PASSWORD = "new password";
		public final static String INITIAL_LOCATIONS = "initial_locations";
		public final static String ActivityRecognitionService_EXTRA_MESSAGE = "ActivityRecognitionService.EXTRA_MESSAGE";
		public final static String ReceiveLocationService_EXTRA_MESSAGE = "ReceiveLocationService_EXTRA_MESSAGE";
	}

	public static abstract class SharedPrefAuthentication {
		public final static String name = "Authentication";
		public final static String user_name = "user_name";
		public final static String user_email = "user_email";
		public final static String user_id = "user_id";
		public final static String flag = "user_status";
		public final static String password = "password";
		public final static String FLAG_INACTIVE = "-1";
		public final static String FLAG_ACTIVE = "1";
	}

	public static abstract class SharedPrefLocationSettings {
		public final static String name = "LocationUpdatePreference";
		public final static String Preference = "preference";
		public final static int NEVER = 4;
		public final static int ALWAYS = 1;
		public final static int RECOMENDED = 2;
		public final static int PLUGGEDIN = 3;
		public final static String UPDATE = "u";
		public final static String UPDATEHOME = "uh";
	}

	public static abstract class SharedPrefActivityRecognition {
		public final static String name = "ActivityRecognition";
		public final static String enabled = "enabled";
		public final static String WALKING = "walking";
		public final static String STILL = "still";
		public final static String VEHICLE = "onvehicle";
		public final static String activityType = "activityType";
		public final static String type = "type";
	}

	public static abstract class ServerVariables {
		public final static String SCHEME = "http";
		public final static String AUTHORITY = "hacksafety.elasticbeanstalk.com";
		public static final String PUBLIC = "public";
		public  static final String INDEX = "index.php";
	}

	public static abstract class Others {
		public final static String LOADING = "Loading...";
	}
}
