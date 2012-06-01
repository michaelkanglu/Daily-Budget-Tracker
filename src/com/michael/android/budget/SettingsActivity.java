package com.michael.android.budget;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		SharedPreferences settings = getSharedPreferences(DailyBudgetTrackerActivity.PREFS_NAME, 0);
		updateBudgetText(settings.getInt(DailyBudgetTrackerActivity.BUDGET, 2000));
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
}
