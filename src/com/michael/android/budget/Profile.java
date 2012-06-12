package com.michael.android.budget;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Spinner;

public class Profile extends Activity {
	
	static final String HEIGHT_METR = "budgettracker.heightmetr";
	static final String WEIGHT_METR = "budgettracker.weightmetr";
	
	static final String SEX_PROF = "budgettracker.sexprof";
	static final String AGE_PROF = "budgettracker.ageprof";
	static final String HEIGHT_PROF = "budgettracker.heightprof";
	static final String WEIGHT_PROF = "budgettracker.weightprof";
	static final String WEEKLY_PROF = "budgettracker.weeklyprof";
	static final String ACTIVI_PROF = "budgettracker.activiprof";
	
	boolean unitInches;
	boolean unitPounds;
	boolean mGender;
	int mAge;
	int mHeight;
	int mWeight;
	int weeklyIndex;
	int activiIndex;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);
		
	}
	
	@Override
	public void onResume(){
		super.onResume();
		restoreButtonInfo();
	}
	
	@Override
	public void onPause(){
		super.onPause();
		saveButtonInfo();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		saveButtonInfo();
	}
	
	private void launchSexWindow(View view){
		LayoutInflater layoutInflater  =
				(LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
	    final View popupView = 
	    		layoutInflater.inflate(R.layout.pro_gender_popup, null);  
	    final PopupWindow popupWindow = new PopupWindow(popupView, 
	            										LayoutParams.WRAP_CONTENT,  
	            										LayoutParams.WRAP_CONTENT);
	}
	
	public void restoreButtonInfo(){
		SharedPreferences settings = getSharedPreferences(DailyBudgetTracker.PREFS_NAME, 0);
		unitInches = settings.getBoolean(HEIGHT_METR, true);
		unitPounds = settings.getBoolean(WEIGHT_METR, true);
		mGender = settings.getBoolean(SEX_PROF, false);
		mAge = settings.getInt(AGE_PROF, 30);
		mHeight = settings.getInt(HEIGHT_PROF, 68);
		mWeight = settings.getInt(WEIGHT_METR, 130);
		weeklyIndex = settings.getInt(WEEKLY_PROF, 2);
		activiIndex = settings.getInt(ACTIVI_PROF, 3);
		
		setSexText(mGender);
		setAgeText(mAge);
		setHeightText(mHeight);
		setWeightText(mWeight);
		setWeeklyItem(weeklyIndex);
		setActivityItem(activiIndex);
	}
	
	public void saveButtonInfo(){
		SharedPreferences settings = getSharedPreferences(DailyBudgetTracker.PREFS_NAME, 0);
		Editor editor = settings.edit();
		
		editor.putBoolean(HEIGHT_METR, unitInches);
		editor.putBoolean(WEIGHT_METR, unitPounds);
		editor.putBoolean(SEX_PROF, mGender);
		editor.putInt(AGE_PROF, mAge);
		editor.putInt(HEIGHT_PROF, mHeight);
		editor.putInt(WEIGHT_METR, mWeight);
		editor.putInt(WEEKLY_PROF, weeklyIndex);
		editor.putInt(ACTIVI_PROF, activiIndex);
		
		editor.commit();
	}
	
	public void setSexText(Boolean sex){
		Button button = (Button)findViewById(R.id.p_sex_button);
		if(sex)
			button.setText("Male");
		else
			button.setText("Female");
	}
	
	public void setAgeText(int age){
		Button button = (Button)findViewById(R.id.p_age_button);
		button.setText(Integer.toString(age));
	}
	
	public void setHeightText(int height){
		Button button = (Button)findViewById(R.id.p_height_button);
		StringBuffer text = new StringBuffer();
		if(unitInches){
			text.append(height/12);
			text.append("ft ");
			text.append(height%12);
			text.append("in");
		}
		else{
			text.append(height);
			text.append("cm");
		}
		button.setText(text.toString());
	}
	
	public void setWeightText(int weight){
		Button button = (Button)findViewById(R.id.p_weight_button);
		StringBuffer text = new StringBuffer();
		text.append(weight);
		if(unitPounds){
			text.append("lbs");
		}
		else{
			text.append("kg");
		}
		button.setText(text.toString());
	}
	
	public void setWeeklyItem(int index){
		Spinner spinner = (Spinner)findViewById(R.id.p_weekly_spinner);
		spinner.setSelection(index);
	}
	
	public void setActivityItem(int index){
		Spinner spinner = (Spinner)findViewById(R.id.p_activity_spinner);
		spinner.setSelection(index);
	}
	
    public void openSettings(View view) {
    	Intent intent = new Intent(this, Settings.class);
    	startActivity(intent);
    }
	
	public void goBack(View view){
		finish();
	}
}
