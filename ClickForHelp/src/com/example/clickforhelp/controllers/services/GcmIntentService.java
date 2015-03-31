package com.example.clickforhelp.controllers.services;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.clickforhelp.R;
import com.example.clickforhelp.controllers.receivers.GcmBroadcastReceiver;
import com.example.clickforhelp.controllers.ui.MainActivity;
import com.example.clickforhelp.models.AppPreferences;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class GcmIntentService extends IntentService {
	private static final String TAG = "GcmIntentService";
	private static final int HElP_REQUESTED = 1;
	private static final int HELP_RECEIVED = 0;

	// private static final int ID_HELP_RECEIVED_NOTIFICATION = 2;

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
		Log.d(TAG,msg);
		NotificationManager mNotificationManagerCompat = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		int notificationId = -1;
		Intent intent = new Intent(this, MainActivity.class);
		JSONArray array;
		int helpRequested = -1;
		String userId = null;
		try {
			array = new JSONArray(msg);
			JSONObject obj = array.getJSONObject(0);
			if (obj.has("helprequested")) {
				helpRequested = obj.getInt("helprequested");
			}
			notificationId = obj.getInt("id");
			userId = obj.getString("userid");
			intent.putExtra(
					AppPreferences.IntentExtras.COORDINATES,
					new double[] { obj.getDouble("latitude"),
							obj.getDouble("longitude") });
			intent.putExtra(AppPreferences.IntentExtras.USERID, userId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (helpRequested == HElP_REQUESTED) {
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			PendingIntent notifyPendingIntent = PendingIntent.getActivity(this,
					0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					this).setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Alert")
					.setContentText(userId.split("@")[0] + " needs help");
			mBuilder.setContentIntent(notifyPendingIntent);
			Uri alarmSound = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			mBuilder.setSound(alarmSound);
			mBuilder.setAutoCancel(true);
			// notification.sound = Uri.parse("android.resource://"
			// + context.getPackageName() + "/" + R.raw.siren);
			mNotificationManagerCompat = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			mNotificationManagerCompat.notify(notificationId, mBuilder.build());
		} else if (helpRequested == HELP_RECEIVED) {
			Handler mHandler=new Handler();
			mHandler.post(new DisplayToast(this,userId.split("@")[0] + " received help"));
			mNotificationManagerCompat.cancel(notificationId);
		}
	}
	
	public class DisplayToast implements Runnable {
	    private final Context mContext;
	    String mText;

	    public DisplayToast(Context mContext, String text){
	        this.mContext = mContext;
	        mText = text;
	    }

	    public void run(){
	        Toast.makeText(mContext, mText, Toast.LENGTH_SHORT).show();
	    }
	}
}
