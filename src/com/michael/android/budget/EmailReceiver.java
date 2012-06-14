package com.michael.android.budget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class EmailReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// Called when it is time to send out the notification.
		ExportEmail em = new ExportEmail(context);
		em.createNotification();
	}
	
}
