package com.michael.android.budget;

import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class EmailReceiver extends BroadcastReceiver {
	private static final int NOTIFY_EMAIL = 1;
	private static final String EMAIL_SUBJECT = "[Daily Budget Tracker] Yesterday's calorie history:";
	
	private Context mContext;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		createNotification();
	}
	
	public void createNotification() {
		// Create a notification in the top bar, so that the user knows that he/she can send the result email.
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(ns);
		int icon = R.drawable.status; //TODO change this icon lol
		CharSequence contentTitle = "Daily Budget Tracker: Email notice";
		CharSequence contentText = "Yesterday's results are available.";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, contentTitle, when);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		
		Intent email = Intent.createChooser(createEmail(), "Send mail");
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, email, 0);

		notification.setLatestEventInfo(mContext, contentTitle, contentText, contentIntent);
		
		mNotificationManager.notify(NOTIFY_EMAIL, notification);
	}
		public Intent createEmail() {
		// This intent creates the email with fields filled out.
		SharedPreferences settings = mContext.getSharedPreferences(DailyBudgetTrackerActivity.PREFS_NAME, 0);
    	String email = settings.getString(DailyBudgetTrackerActivity.EMAIL, "");
    	
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
		i.putExtra(Intent.EXTRA_SUBJECT, EMAIL_SUBJECT);
		i.putExtra(Intent.EXTRA_TEXT, getHistory());
		return i;
	}
	
	public String getHistory() {
		// Return the body of text for the result email.
		SharedPreferences settings = mContext.getSharedPreferences(DailyBudgetTrackerActivity.PREFS_NAME, 0);
		int budget = settings.getInt(DailyBudgetTrackerActivity.BUDGET, 2000);
		int total = 0;
		StringBuffer history = new StringBuffer();

		// create string of food and value for previous day's consumption
		TrackingDatabase db_helper = new TrackingDatabase(mContext);;
		SQLiteDatabase db = db_helper.getReadableDatabase();
		
		long time = System.currentTimeMillis();
		Calendar today = Calendar.getInstance();
		today.setTimeInMillis(time);
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND,0);
		today.set(Calendar.MILLISECOND,0);
		time = today.getTimeInMillis();	
		
        Cursor cursor = db.rawQuery("SELECT * FROM " + "Track WHERE DateTime < " + time, null);
        if (cursor.moveToFirst()) {
            do {
            	String name = cursor.getString(1);
            	int value = Integer.parseInt(cursor.getString(2));
            	total += value;
				history.append(name + ", " + value + "\n");
            } while (cursor.moveToNext());
        }
        db.close();

        // Header for the result email.
		history.insert(0, "Yesterday, you consumed " + total + " out of " + budget + " calories.\n\nIt consisted of:\n");
		return history.toString();
	}
	
}
