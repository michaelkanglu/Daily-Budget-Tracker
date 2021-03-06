package com.agile_comrade.android.budget;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.agile_comrade.android.budget.R;

public class Settings extends Activity {
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		SharedPreferences settings = getSharedPreferences(DailyBudgetTracker.PREFS_NAME, 0);
		updateBudgetText(settings.getInt(DailyBudgetTracker.BUDGET, 2000));
		restorePreviousExports();
		addExportListener();
	}
	
	/**resets the whole app to out of box settings. Debug purposes*/
    public void resetApp(View view){
    	// Creates confirmation dialog before resetting
    	String title = "Reset";
    	String message = "Reset app to initial settings?";
    	AlertDialog confirm = new AlertDialog.Builder(this).create();
    	confirm.setTitle(title);
    	confirm.setMessage(message);
    	confirm.setButton("Confirm", new DialogInterface.OnClickListener() {			
			public void onClick(DialogInterface dialog, int which) {
				// Reset the app
		        SharedPreferences settings = getSharedPreferences(DailyBudgetTracker.PREFS_NAME, 0);
		        SharedPreferences.Editor editor = settings.edit();
		        editor.clear();
		        editor.commit();
		        
		    	TrackingDatabase db_helper = new TrackingDatabase(getApplicationContext());
		    	db_helper.resetDatabase();
		    	
		        updateBudgetText(2000);
			}
		});

    	confirm.setButton2("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//Do nothing
			}
		});
    	
    	confirm.show();
    }
    
    /**Sets the master  to a new value and scales running budget by appropriate amount*/
    public void newMasterBudget(View view){
        SharedPreferences settings = getSharedPreferences(DailyBudgetTracker.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        EditText mUpdateBox = (EditText)findViewById(R.id.update_box);
        
    	String message = mUpdateBox.getText().toString();
    	int value = 0;
    	
    	Context context = getApplicationContext();
		int duration = Toast.LENGTH_LONG;
    	//catch number format exception
    	try{
    		 value = Integer.parseInt(message);
    	}
    	catch(NumberFormatException e){
    		
    		CharSequence text = "The value is not a valid, whole number!";
    		Toast toast = Toast.makeText(context, text, duration);
    		toast.show();
    		mUpdateBox.setText(null);
    		return;
    	}

        int budget = settings.getInt(DailyBudgetTracker.BUDGET, 2000);
        int runningBudget = settings.getInt(DailyBudgetTracker.RUNNING_BUDGET, 2000);
    	int diff = value - budget;
    	budget = value;
    	runningBudget = runningBudget + diff;   
		
        editor.putInt(DailyBudgetTracker.RUNNING_BUDGET, runningBudget);
        editor.putInt(DailyBudgetTracker.BUDGET, budget);
        editor.commit();
		CharSequence text = "Your budget has been updated!";
		Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
		toast.show();
        mUpdateBox.setText(null);
        updateBudgetText(budget);
    }
    
    public void goBack(View view) {
    	finish();
    }
    
    private void updateBudgetText(int newMasBudget){
    	TextView text = (TextView)findViewById(R.id.s_budg_text);
    	String value = getResources().getString(R.string.s_budg_disp);
    	value = value + " " + Integer.toString(newMasBudget) + " Cal/day";
    	text.setText(value);
    }
    
    public void addExportListener() {
    	// Adds a listener for notification toggles.
    	final CheckBox check = (CheckBox) findViewById(R.id.export_check);
    	check.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
				String email = getEmail();
    			if( ((CheckBox) v).isChecked() ) {
    				if (email == null) {
    					// Checkbox checked but no email address. Does not send notifications.
    					String title = "Emails will not be sent";
    					String message = "Please enter an email address in the field.";
    					new AlertDialog.Builder(Settings.this).setTitle(title).setMessage(message).setNeutralButton("OK", null).show();
    					
    					check.setChecked(false);
    					storeExportSettings(null, false);
    				}
    				else {
    					// Checkbox checked and has an email address. Send email notifications.
    					String title = "Emails will be sent";
    					String message = "Everyday a notification will prompt you for yesterday's results. The email will be populated, but you need to send it.";
    					new AlertDialog.Builder(Settings.this).setTitle(title).setMessage(message).setNeutralButton("OK", null).show();
    					
						// Update preferences for email export.
				        storeExportSettings(email, true);
				        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
				        scheduleNotification(Settings.this, alarmMgr);
    				}
    			}
    			else {
    				// Stop sending notifications!
    				String title = "Emails will not be sent";
					String message = "Daily notifcations of results will end.";
					new AlertDialog.Builder(Settings.this).setTitle(title).setMessage(message).setNeutralButton("OK", null).show();
					
					// Update preferences for email export.
	    			storeExportSettings(email, false);
					stopNotification();
    			}
    		}
    	});
    }
    
    public String getEmail() {
    	// Returns the text found in the EditText email field.
    	EditText et = (EditText) findViewById(R.id.email);
    	Editable email = et.getText();
    	if (email.length() == 0) {
    		return null;
    	}
    	return email.toString();
    }
    
    public void storeExportSettings(String email, boolean sendEmails) {
    	// Stores email and export preference settings.
    	SharedPreferences settings = getSharedPreferences(DailyBudgetTracker.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(DailyBudgetTracker.EMAIL, email);
        editor.putBoolean(DailyBudgetTracker.EXPORT, sendEmails);
        editor.commit();    }
    
    public void restorePreviousExports() {
    	SharedPreferences settings = getSharedPreferences(DailyBudgetTracker.PREFS_NAME, 0);
    	EditText email = (EditText) findViewById(R.id.email);
    	email.setText(settings.getString(DailyBudgetTracker.EMAIL, null));
    	
    	CheckBox export = (CheckBox) findViewById(R.id.export_check);
    	export.setChecked(settings.getBoolean(DailyBudgetTracker.EXPORT, false));
    }

	@Override
	protected void onDestroy() {
		// When the settings activity is destroyed, save whatever is in the email field.
		super.onDestroy();
		String email = getEmail();
		SharedPreferences settings = getSharedPreferences(DailyBudgetTracker.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(DailyBudgetTracker.EMAIL, email);
		editor.commit();
	}
	
	public static void scheduleNotification(Context context, AlarmManager alarmMgr) {
		// Schedule the notifications for everyday.
		Intent intent = new Intent(context, EmailReceiver.class);
		PendingIntent emailNote = PendingIntent.getBroadcast(context, 0, intent, 0);
		
		Calendar time = Calendar.getInstance();
		time.setTimeInMillis(System.currentTimeMillis());
		time.add(Calendar.DATE, 1);  // Add one day so that the notification does not show immediately.
		time.set(Calendar.HOUR_OF_DAY, 0);
		time.set(Calendar.MINUTE, 0);
		time.set(Calendar.SECOND, 0);
		time.set(Calendar.MILLISECOND, 0);
		
		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), AlarmManager.INTERVAL_DAY, emailNote);
	}
	
	public void stopNotification() {
		// Stops the daily notifications.
		AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(this, EmailReceiver.class);
		PendingIntent emailNote = PendingIntent.getBroadcast(this, 0, intent, 0);
		alarmMgr.cancel(emailNote);
	}

	public void resendEmail(View view) {
		// Call this method when you want to send yesterday's results.
		ExportEmail em = new ExportEmail(this);
		em.startChooser();
	}
	
}
