package com.michael.android.budget;

import java.net.URLEncoder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		SharedPreferences settings = getSharedPreferences(DailyBudgetTrackerActivity.PREFS_NAME, 0);
		updateBudgetText(settings.getInt(DailyBudgetTrackerActivity.BUDGET, 2000));
		restorePreviousExports();
		addExportListener();
	}
	
	/**resets the whole app to out of box settings. Debug purposes*/
    public void resetApp(View view){
    	int budget = 2000;
    	int runningBudget = 2000;
    	
        SharedPreferences settings = getSharedPreferences(DailyBudgetTrackerActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(DailyBudgetTrackerActivity.RUNNING_BUDGET, runningBudget);
        editor.putInt(DailyBudgetTrackerActivity.BUDGET, budget);
        editor.commit();
        
    	TrackingDatabase db_helper = new TrackingDatabase(getApplicationContext());
    	db_helper.clearDatabase();
    	
        updateBudgetText(budget);
    }
    
    /**Sets the master  to a new value and scales running budget by appropriate amount*/
    public void newMasterBudget(View view){
        SharedPreferences settings = getSharedPreferences(DailyBudgetTrackerActivity.PREFS_NAME, 0);
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
    	
    	//catch negative numbers
    	if(value < 0){
    		CharSequence text = "The value is less than zero!";

    		Toast toast = Toast.makeText(context, text, duration);
    		toast.show();
    		mUpdateBox.setText(null);
    		return;
    	}
        int budget = settings.getInt(DailyBudgetTrackerActivity.BUDGET, 2000);
        int runningBudget = settings.getInt(DailyBudgetTrackerActivity.RUNNING_BUDGET, 2000);
    	int diff = value - budget;
    	budget = value;
    	runningBudget = runningBudget + diff;   
		
        editor.putInt(DailyBudgetTrackerActivity.RUNNING_BUDGET, runningBudget);
        editor.putInt(DailyBudgetTrackerActivity.BUDGET, budget);
        editor.commit();
        mUpdateBox.setText(null);
        updateBudgetText(budget);
    }
    
    public void goBack(View view) {
    	finish();
    }
    
    private void updateBudgetText(int newMasBudget){
    	TextView text = (TextView)findViewById(R.id.s_budg_text);
    	String value = getResources().getString(R.string.s_budg_disp);
    	value = value + " " + Integer.toString(newMasBudget);
    	text.setText(value);
    }
    
    public void addExportListener() {
    	final CheckBox check = (CheckBox) findViewById(R.id.export_check);
    	check.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			if( ((CheckBox) v).isChecked() ) {
    				// send emails!
    				String email = getEmail();
    				if (email == null) {
    					// Checkbox checked but no email address. Does not send emails.
    					Toast no_email_msg = Toast.makeText(getApplicationContext(), "Please enter an email address.", Toast.LENGTH_SHORT);
    					no_email_msg.show();
    					
    					check.setChecked(false);
    					storeExportSettings(null, false);
    				}
    				else {
    					// Checkbox checked and has an email address. Sends emails.
    					Toast email_msg = Toast.makeText(getApplicationContext(), "Ok, I have an email address.", Toast.LENGTH_SHORT);
    					email_msg.show();
    					
				        storeExportSettings(email, true);
				       sendEmail();
    				}
    			}
    			else {
    				// stop sending emails!
					Toast stop = Toast.makeText(getApplicationContext(), "Emails will not be sent.", Toast.LENGTH_SHORT);
					stop.show();
					
	    			SharedPreferences settings = getSharedPreferences(DailyBudgetTrackerActivity.PREFS_NAME, 0);
			        SharedPreferences.Editor editor = settings.edit();
			        editor.putBoolean(DailyBudgetTrackerActivity.EXPORT, false);
			        editor.commit();
			        
			        stopEmail();
    			}
    		}
    	});
    }
    
    public String getEmail() {
    	EditText et = (EditText) findViewById(R.id.email);
    	Editable email = et.getText();
    	if (email.length() == 0) {
    		return null;
    	}
    	return email.toString();
    }
    
    public void storeExportSettings(String email, boolean sendEmails) {
    	// Stores email and export preference settings.
    	SharedPreferences settings = getSharedPreferences(DailyBudgetTrackerActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(DailyBudgetTrackerActivity.EMAIL, email);
        editor.putBoolean(DailyBudgetTrackerActivity.EXPORT, sendEmails);
        editor.commit();    }
    
    public void restorePreviousExports() {
    	SharedPreferences settings = getSharedPreferences(DailyBudgetTrackerActivity.PREFS_NAME, 0);
    	EditText email = (EditText) findViewById(R.id.email);
    	email.setText(settings.getString(DailyBudgetTrackerActivity.EMAIL, null)); 
    	
    	CheckBox export = (CheckBox) findViewById(R.id.export_check);
    	export.setChecked(settings.getBoolean(DailyBudgetTrackerActivity.EXPORT, false));
    }

	@Override
	protected void onDestroy() {
		// When the settings activity is destroyed, save whatever is in the email field.
		super.onDestroy();
		String email = getEmail();
    	SharedPreferences settings = getSharedPreferences(DailyBudgetTrackerActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(DailyBudgetTrackerActivity.EMAIL, email);
        editor.commit();
	}
	
	public void sendEmail() {
    	SharedPreferences settings = getSharedPreferences(DailyBudgetTrackerActivity.PREFS_NAME, 0);
    	String email = settings.getString(DailyBudgetTrackerActivity.EMAIL, "");
		String uriText =
		    "mailto:" + email +
		    "?subject=" + URLEncoder.encode("[Daily Budget Tracker] Yesterday's calorie history:") + 
		    "&body=" + URLEncoder.encode("stuff");

		Uri uri = Uri.parse(uriText);

		Intent send_email = new Intent(Intent.ACTION_SENDTO);
		send_email.setData(uri);
		startActivity(send_email);
	}
	
	public void stopEmail() {
	}
    
}
