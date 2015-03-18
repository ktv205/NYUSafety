package com.example.clickforhelp.controllers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.clickforhelp.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

public class GcmIntentService extends IntentService {
	private static final String TAG = "GcmIntentService";
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;

	public GcmIntentService() {
		super(TAG);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				sendNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				sendNotification("Deleted messages on server: "
						+ extras.toString());
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {

				sendNotification(extras.getString("message"));
				Log.i(TAG, "Received: " + extras.toString());
				Log.d(TAG, extras.getString("message"));
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);

	}

	private void sendNotification(String msg) {

		// Creates an Intent for the Activity
		Intent intent = new Intent(this, MainActivity.class);
		JSONArray array;
		try {
			array = new JSONArray(msg);
			JSONObject obj = array.getJSONObject(0);
			intent.putExtra("coord", new double[] { obj.getDouble("latitude"),
					obj.getDouble("longitude") });
			Log.d(TAG,
					" " + obj.getDouble("latitude") + " "
							+ obj.getDouble("longitude"));
			intent.putExtra("userid", obj.getString("userid"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Sets the Activity to start in a new, empty task
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		// Creates the PendingIntent
		PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Alert")
				.setContentText("some one near you needs help");
		mBuilder.setContentIntent(notifyPendingIntent);
		NotificationManager mNotificationManagerCompat = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotificationManagerCompat.notify(1, mBuilder.build());

	}

}
