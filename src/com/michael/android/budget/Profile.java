package com.michael.android.budget;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

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
	float mAge;
	float mHeight;
	float mWeight;
	int weeklyIndex;
	int activiIndex;
	
	boolean spinnerMutex;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);
		
	}
	
	@Override
	public void onResume(){
		super.onResume();
		restoreButtonInfo();
		spinnerMutex=false;
		((Spinner)findViewById(R.id.p_activity_spinner)).setOnItemSelectedListener(new OnItemSelectedListener() {
		    
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		        if(spinnerMutex){
		        	activiIndex = position;
		        	updateRecommendation();
		        }
		    }
		    
		    public void onNothingSelected(AdapterView<?> parentView) {
		        // your code here
		    }
		});
		
		((Spinner)findViewById(R.id.p_weekly_spinner)).setOnItemSelectedListener(new OnItemSelectedListener() {
		    
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		        if(spinnerMutex){
		        	weeklyIndex = position;
		        	updateRecommendation();
		        }
		    }
		    
		    public void onNothingSelected(AdapterView<?> parentView) {
		        // your code here
		    }
		});
		spinnerMutex=true;
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
	
	public void restoreButtonInfo(){
		SharedPreferences settings = getSharedPreferences(DailyBudgetTracker.PREFS_NAME, 0);
		unitInches = settings.getBoolean(HEIGHT_METR, true);
		unitPounds = settings.getBoolean(WEIGHT_METR, true);
		mGender = settings.getBoolean(SEX_PROF, true);
		mAge = settings.getFloat(AGE_PROF, 30f);
		mHeight = settings.getFloat(HEIGHT_PROF, 65f);
		mWeight = settings.getFloat(WEIGHT_PROF, 120f);
		weeklyIndex = settings.getInt(WEEKLY_PROF, 2);
		activiIndex = settings.getInt(ACTIVI_PROF, 3);
		
		setSexText(mGender);
		setAgeText((int)mAge);
		setHeightText((int)mHeight);
		setWeightText((int)mWeight);
		setWeeklyItem(weeklyIndex);
		setActivityItem(activiIndex);
		updateRecommendation();
	}
	
	public void saveButtonInfo(){
		SharedPreferences settings = getSharedPreferences(DailyBudgetTracker.PREFS_NAME, 0);
		Editor editor = settings.edit();
		
		editor.putBoolean(HEIGHT_METR, unitInches);
		editor.putBoolean(WEIGHT_METR, unitPounds);
		editor.putBoolean(SEX_PROF, mGender);
		editor.putFloat(AGE_PROF, mAge);
		editor.putFloat(HEIGHT_PROF, mHeight);
		editor.putFloat(WEIGHT_PROF, mWeight);
		editor.putInt(WEEKLY_PROF, weeklyIndex);
		editor.putInt(ACTIVI_PROF, activiIndex);
		
		editor.commit();
	}
	
	public void launchSexWindow(View view){
		LayoutInflater layoutInflater  =
				(LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
	    final View popupView = 
	    		layoutInflater.inflate(R.layout.pro_gender_popup, null);  
	    final PopupWindow popupWindow = new PopupWindow(popupView, 
	            										LayoutParams.WRAP_CONTENT,  
	            										LayoutParams.WRAP_CONTENT);
	    
	    centerPopupWindow(popupView, popupWindow);
	    
		popupWindow.setFocusable(true);
		popupWindow.update();
		
	    Button btnFemale = (Button)popupView.findViewById(R.id.p_female_select);
	    btnFemale.setOnClickListener(new View.OnClickListener(){
	    	public void onClick(View v) {
	    		mGender = true;
	    		setSexText(mGender);
	    		updateRecommendation();
	    		popupWindow.dismiss();
	    		
	    	}
	    });
	    
	    // When cancel button clicked
	    Button btnMale = (Button)popupView.findViewById(R.id.p_male_select);
	    btnMale.setOnClickListener(new View.OnClickListener(){
	    	public void onClick(View v) {
	    		mGender = false;
	    		setSexText(mGender);
	    		updateRecommendation();
	    		popupWindow.dismiss();
	    	}
	    });
	}
	
	public void launchAgeWindow(View view){
		LayoutInflater layoutInflater  =
				(LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
	    final View popupView = 
	    		layoutInflater.inflate(R.layout.pro_age_popup, null);  
	    final PopupWindow popupWindow = new PopupWindow(popupView, 
	            										LayoutParams.WRAP_CONTENT,  
	            										LayoutParams.WRAP_CONTENT);
	    
	    final EditText ageInputBox = (EditText)popupView.findViewById(R.id.p_age_input_box);
	    ageInputBox.setText(Integer.toString((int)mAge));
	    
	    centerPopupWindow(popupView, popupWindow);
	    
		popupWindow.setFocusable(true);
		popupWindow.update();
		
	    Button btnSubmitAge = (Button)popupView.findViewById(R.id.p_submit_age);
	    btnSubmitAge.setOnClickListener(new View.OnClickListener(){
	    	public void onClick(View v) {
	    		if(ageInputBox.getText().length()==0){
	    			return;
	    		}
	    		mAge = Integer.parseInt(ageInputBox.getText().toString());
	    		setAgeText((int)mAge);
	    		updateRecommendation();
	    		popupWindow.dismiss();
	    	}
	    });
	    
	    // When cancel button clicked
	    Button btnCancelAge = (Button)popupView.findViewById(R.id.p_cancel_age);
	    btnCancelAge.setOnClickListener(new View.OnClickListener(){
	    	public void onClick(View v) {
	    		popupWindow.dismiss();
	    	}
	    });
	}
	
	public void launchWeightWindow(View view){
		LayoutInflater layoutInflater  =
				(LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
	    final View popupView = 
	    		layoutInflater.inflate(R.layout.pro_weight_popup, null);  
	    final PopupWindow popupWindow = new PopupWindow(popupView, 
	            										LayoutParams.WRAP_CONTENT,  
	            										LayoutParams.WRAP_CONTENT);
	    
	    final EditText weightInputBox = (EditText)popupView.findViewById(R.id.p_weight_input_box);
	    weightInputBox.setText(Integer.toString((int)mWeight));
	    final TextView weightCaption = (TextView)popupView.findViewById(R.id.p_weight_caption);
	    if(unitPounds){
	    	weightCaption.setText(this.getResources().getString(R.string.p_weight_unit_im));
	    }
	    else{
	    	weightCaption.setText(this.getResources().getString(R.string.p_weight_unit_si));
	    }
	    
	    centerPopupWindow(popupView, popupWindow);
	    
		popupWindow.setFocusable(true);
		popupWindow.update();
		
		Button btnToggleUnit = (Button)popupView.findViewById(R.id.p_weight_toggle);
		btnToggleUnit.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				unitPounds = !unitPounds;
				
				int weight = 0;
				if(weightInputBox.getText().length()!=0){
					weight = Integer.parseInt(weightInputBox.getText().toString());
				}
				
			    if(unitPounds){
			    	weightCaption.setText(v.getResources().getString(R.string.p_weight_unit_im));
			    	mWeight = mWeight * 2.2f;

			    	weight = (int)(weight * 2.2);
			    }
			    else{
			    	weightCaption.setText(v.getResources().getString(R.string.p_weight_unit_si));
			    	mWeight = mWeight / 2.2f;

			    	weight = (int)(weight / 2.2);
			    }
			    
		    	weightInputBox.setText(Integer.toString(weight));
		    	setWeightText((int)mWeight);
			}
		});
		
	    Button btnSubmitWeight = (Button)popupView.findViewById(R.id.p_submit_weight);
	    btnSubmitWeight.setOnClickListener(new View.OnClickListener(){
	    	public void onClick(View v) {
	    		if(weightInputBox.getText().length()==0){
	    			return;
	    		}
	    		mWeight = Integer.parseInt(weightInputBox.getText().toString());
	    		setWeightText((int)mWeight);
	    		updateRecommendation();
	    		popupWindow.dismiss();
	    	}
	    });
	    
	    // When cancel button clicked
	    Button btnCancelWeight = (Button)popupView.findViewById(R.id.p_cancel_weight);
	    btnCancelWeight.setOnClickListener(new View.OnClickListener(){
	    	public void onClick(View v) {
	    		popupWindow.dismiss();
	    	}
	    });
	}
	
	public void launchHeightWindow(View view){
		LayoutInflater layoutInflater  =
				(LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
	    final View popupView = 
	    		layoutInflater.inflate(R.layout.pro_height_popup, null);  
	    final PopupWindow popupWindow = new PopupWindow(popupView, 
	            										LayoutParams.WRAP_CONTENT,  
	            										LayoutParams.WRAP_CONTENT);
	    
	    final EditText firstInputBox = (EditText)popupView.findViewById(R.id.p_first_input_box);
	    final EditText secondInputBox = (EditText)popupView.findViewById(R.id.p_second_input_box);
	    
	    
	    final TextView firstCaption = (TextView)popupView.findViewById(R.id.p_first_caption);
	    final TextView secondCaption = (TextView)popupView.findViewById(R.id.p_second_caption);
	    if(unitInches){
	    	firstCaption.setText(this.getResources().getString(R.string.p_height1_unit_im));
	    	secondCaption.setText(this.getResources().getString(R.string.p_height2_unit_im));
	    	firstInputBox.setText(Integer.toString((int)mHeight/12));
	    	secondInputBox.setText(Integer.toString((int)mHeight%12));
	    }
	    else{
	    	firstCaption.setText(this.getResources().getString(R.string.p_height1_unit_si));
	    	secondCaption.setText(this.getResources().getString(R.string.p_height2_unit_si));
	    	firstInputBox.setText(Integer.toString((int)mHeight/100));
	    	secondInputBox.setText(Integer.toString((int)mHeight%100));
	    }
	    
	    centerPopupWindow(popupView, popupWindow);
	    
		popupWindow.setFocusable(true);
		popupWindow.update();
		
		Button btnToggleUnit = (Button)popupView.findViewById(R.id.p_height_toggle);
		btnToggleUnit.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				unitInches = !unitInches;
				
				int height = 0;
				
			    if(unitInches){
			    	firstCaption.setText(v.getResources().getString(R.string.p_height1_unit_im));
			    	secondCaption.setText(v.getResources().getString(R.string.p_height2_unit_im));
			    	
			    	mHeight = mHeight / 2.54f;
			    	
			    	if(firstInputBox.getText().length()!=0){
			    		height = height + (Integer.parseInt(firstInputBox.getText().toString()) * 100);
			    	}
			    	
			    	if(secondInputBox.getText().length()!=0){
			    		height = height + Integer.parseInt(secondInputBox.getText().toString());
			    	}
			    	height = (int)(height / 2.54);
			    	firstInputBox.setText(Integer.toString(height/12));
			    	secondInputBox.setText(Integer.toString(height%12));
			    }
			    else{
			    	firstCaption.setText(v.getResources().getString(R.string.p_height1_unit_si));
			    	secondCaption.setText(v.getResources().getString(R.string.p_height2_unit_si));
			    	
			    	mHeight = mHeight * 2.54f;
			    	
			    	if(firstInputBox.getText().length()!=0){
			    		height = height + (Integer.parseInt(firstInputBox.getText().toString()) * 12);
			    	}
			    	
			    	if(secondInputBox.getText().length()!=0){
			    		height = height + Integer.parseInt(secondInputBox.getText().toString());
			    	}
			    	height = (int)(height * 2.54);
			    	
			    	firstInputBox.setText(Integer.toString(height/100));
			    	secondInputBox.setText(Integer.toString(height%100));
			    }
			    setHeightText((int)mHeight);
			}
		});
		
	    Button btnSubmitHeight = (Button)popupView.findViewById(R.id.p_submit_height);
	    btnSubmitHeight.setOnClickListener(new View.OnClickListener(){
	    	public void onClick(View v) {
	    		int height = 0;
			    if(unitInches){
			    	if(firstInputBox.getText().length()!=0){
			    		height = height + (Integer.parseInt(firstInputBox.getText().toString()) * 12);
			    	}
			    	
			    	if(secondInputBox.getText().length()!=0){
			    		height = height + Integer.parseInt(secondInputBox.getText().toString());
			    	}
			    }
			    else{
			    	if(firstInputBox.getText().length()!=0){
			    		height = height + (Integer.parseInt(firstInputBox.getText().toString()) * 100);
			    	}
			    	
			    	if(secondInputBox.getText().length()!=0){
			    		height = height + Integer.parseInt(secondInputBox.getText().toString());
			    	}
			    }
		    	if(height == 0){
		    		return;
		    	}
		    	mHeight = height;
		    	setHeightText((int)mHeight);
	    		updateRecommendation();
	    		popupWindow.dismiss();
	    	}
	    });
	    
	    // When cancel button clicked
	    Button btnCancelHeight = (Button)popupView.findViewById(R.id.p_cancel_height);
	    btnCancelHeight.setOnClickListener(new View.OnClickListener(){
	    	public void onClick(View v) {
	    		popupWindow.dismiss();
	    	}
	    });
	}
	
	private void centerPopupWindow(View popupView, PopupWindow popupWindow){
		popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		View parent = findViewById(R.id.p_activity_spinner).getRootView();
		
		popupWindow.showAtLocation(parent, Gravity.TOP, 0, 60);
	}
	
	public void setSexText(Boolean sex){
		Button button = (Button)findViewById(R.id.p_sex_button);
		if(sex)
			button.setText("Female");
		else
			button.setText("Male");
	}
	
	public void setAgeText(int age){
		Button button = (Button)findViewById(R.id.p_age_button);
		button.setText(Integer.toString(age) + "yrs");
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
			text.append(height/100);
			if((height%100)<10){
				text.append(0);
			}
			text.append(height%100);
			text.append("m");
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
	
	//TODO: Actually write the math formula
	public void updateRecommendation(){
		((TextView)findViewById(R.id.p_budget_goal)).setText(Integer.toString(calculateRecommendation()));
	}
	
	private int calculateRecommendation(){
		float height = mHeight;
		if(unitInches){
			height = height * 2.45f;
		}
		float weight = mWeight;
		if(unitPounds){
			weight = weight / 2.2f;
		}
		
		float intake;
		if(mGender){
			intake = (9.36f*weight + 7.26f*height);
			switch(activiIndex){
			case 4:
				intake = intake * 1.0f;
				break;
			case 3:
				intake = intake * 1.12f;
				break;
			case 2:
				intake = intake * 1.27f;
				break;
			case 1:
				intake = intake * 1.45f;
				break;
			case 0:
				intake = intake * 1.55f;
				break;
			default:
				break;
			}
			intake = intake + 354 - 6.91f*mAge;
		}
		else{
			intake = (15.91f*weight + 5.396f*height);
			switch(activiIndex){
			case 4:
				intake = intake * 1.0f;
				break;
			case 3:
				intake = intake * 1.11f;
				break;
			case 2:
				intake = intake * 1.25f;
				break;
			case 1:
				intake = intake * 1.48f;
				break;
			case 0:
				intake = intake * 1.75f;
				break;
			default:
				break;
			}
			intake = intake + 662 - 9.53f*mAge;
		}
		

		
		switch(weeklyIndex){
		case 4:
			intake = intake - 500.0f;
			break;
		case 3:
			intake = intake - 250.0f;
			break;
		case 2:
			break;
		case 1:
			intake = intake + 250.0f;
			break;
		case 0:
			intake = intake + 500.0f;
			break;
		default:
			break;
		}		
		
		return (int)intake;
	}
	
	public void updateMasterBudget(View view){
        SharedPreferences settings = getSharedPreferences(DailyBudgetTracker.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        TextView mUpdateBox = (TextView)findViewById(R.id.p_budget_goal);

    	int value = Integer.parseInt(mUpdateBox.getText().toString());
    	
        int budget = settings.getInt(DailyBudgetTracker.BUDGET, 2000);
        int runningBudget = settings.getInt(DailyBudgetTracker.RUNNING_BUDGET, 2000);
    	int diff = value - budget;
    	budget = value;
    	runningBudget = runningBudget + diff;   
		
        editor.putInt(DailyBudgetTracker.RUNNING_BUDGET, runningBudget);
        editor.putInt(DailyBudgetTracker.BUDGET, budget);
        editor.commit();
	}
	
    public void openSettings(View view) {
    	Intent intent = new Intent(this, Settings.class);
    	startActivity(intent);
    }
	
	public void goBack(View view){
		finish();
	}
}
