package com.michael.android.budget;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class RestartEmails extends BroadcastReceiver {
	private Context mContext;

	@Override
	public void onReceive(Context context, Intent intent) {
		// Reschedule the notifications for everyday on boot (if toggled on).
		mContext = context;
		
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") && sendEmails()) {
			AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Settings.scheduleNotification(context, alarmMgr);
		}
		
	}
	
	public boolean sendEmails() {
		// Retrieve the preference settings for email notifications.
		SharedPreferences settings = mContext.getSharedPreferences(DailyBudgetTracker.PREFS_NAME, 0);
    	return settings.getBoolean(DailyBudgetTracker.EXPORT, false);
	}

}
