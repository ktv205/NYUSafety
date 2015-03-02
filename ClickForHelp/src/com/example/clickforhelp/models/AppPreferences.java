package com.example.clickforhelp.models;

public class AppPreferences {
	public static abstract class Flags{
		public final static int SIGNUP_FLAG=1991;
		public final static int LOGIN_FLAG=1992;
		public final static int VERIFICATION_FLAG=1993;
		public final static int ACTIVE_FLAG=1994;
		public final static int BACK_FLAG=1995;
	}
	public static abstract class IntentExtras{
		public final static String signuptoverification="signuptoverification";
		public final static String verificationtomain="verificationtomain";
		public final static String verificationtoauthentication="verificationtoauthentication";
	}
	public static abstract class SharedPref{
		public final static String name="Authentication";
		public final static String user_name="user_name";
		public final static String user_email="user_email";
		public final static String user_id="user_id";
		public final static String flag="user_status";
		
	}
	

}
