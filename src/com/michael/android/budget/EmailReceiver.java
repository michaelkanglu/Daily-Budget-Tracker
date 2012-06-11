package com.michael.android.budget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class EmailReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		ExportEmail em = new ExportEmail(context);
		em.createNotification();
	}
	
}
