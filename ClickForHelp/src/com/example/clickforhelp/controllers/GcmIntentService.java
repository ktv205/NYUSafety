package com.example.clickforhelp.controllers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.clickforhelp.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

public class GcmIntentService extends IntentService {
	private static final String TAG = "GcmIntentService";

	public GcmIntentService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				sendNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				sendNotification("Deleted messages on server: "
						+ extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {

				sendNotification(extras.getString("message"));
			}
		}
		GcmBroadcastReceiver.completeWakefulIntent(intent);

	}

	private void sendNotification(String msg) {

		Intent intent = new Intent(this, MainActivity.class);
		JSONArray array;
		try {
			array = new JSONArray(msg);
			JSONObject obj = array.getJSONObject(0);
			intent.putExtra("coord", new double[] { obj.getDouble("latitude"),
					obj.getDouble("longitude") });
			intent.putExtra("userid", obj.getString("userid"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Alert")
				.setContentText("some one near you needs help");
		mBuilder.setContentIntent(notifyPendingIntent);
		Uri alarmSound = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		mBuilder.setSound(alarmSound);
		mBuilder.setAutoCancel(true);
		// notification.sound = Uri.parse("android.resource://"
		// + context.getPackageName() + "/" + R.raw.siren);
		NotificationManager mNotificationManagerCompat = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotificationManagerCompat.notify(1, mBuilder.build());

	}

}
