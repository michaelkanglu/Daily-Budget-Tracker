package com.michael.android.budget;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
			Intent i = new Intent(context, EmailReceiver.class);
			SettingsActivity.emailNote = PendingIntent.getBroadcast(context, 0, i, 0);
			
			Calendar time = Calendar.getInstance();
			time.setTimeInMillis(System.currentTimeMillis());
			time.set(Calendar.HOUR_OF_DAY, 0);
			time.set(Calendar.MINUTE, 0);
			time.set(Calendar.SECOND,0);
			time.set(Calendar.MILLISECOND,0);
			alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), AlarmManager.INTERVAL_DAY, SettingsActivity.emailNote);
		}
		
	}
	
	public boolean sendEmails() {
		SharedPreferences settings = mContext.getSharedPreferences(DailyBudgetTrackerActivity.PREFS_NAME, 0);
    	return settings.getBoolean(DailyBudgetTrackerActivity.EXPORT, false);
	}

}
