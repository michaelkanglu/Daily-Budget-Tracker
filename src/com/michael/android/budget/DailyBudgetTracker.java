package com.michael.android.budget;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class DailyBudgetTracker extends Activity {
	private TrackingDatabase db_helper;
	public static final String PREFS_NAME = "budgettracker.MyPrefsFile";
	static final String RUNNING_BUDGET = "budgettracker.running_budget";
	static final String BUDGET = "budgettracker.budget";
	static final String FIRST_USE = "budgettracker.firstuse";
	static final String EMAIL = "budgettracker.email";
	static final String EXPORT = "budgettracker.export";

	static final String LAST_DAY = "budgettracker.lday";
	static final String LAST_MONTH = "budgettracker.lmonth";
	static final String LAST_YEAR = "budgettracker.lyear";
		
		private TextView mBudgetGoal;
		private EditText mInputBox;
		private Spinner mUnitSelect;
		private EditText mFoodInputBox;
		private boolean firstUse;
		
		int mBudget;
		int mRunningBudget;
		int oRunningBudget;
		int step;
		
		int[] mUnitValues;
		

		
	/**a countdown timer that powers the Big Number's countdown animation**/
	public class BudgetCounter extends CountDownTimer{
		public BudgetCounter(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		
		/**update to the final information and unlock buttons for users to use**/
		@Override
		public void onFinish() {
			updateRunningBudget();
	    	mInputBox.setText(null);
	    	mFoodInputBox.setText(null);
			unlockAllButtons();
		}
		
		/**decrement Big Number by stepSize and update color**/
		@Override
		public void onTick(long millisUntilFinished) {
			
			oRunningBudget = oRunningBudget - step;
			double ratio = ((double)oRunningBudget)/mBudget;
			
			if(oRunningBudget - step > mRunningBudget){
				mBudgetGoal.setText(Integer.toString(oRunningBudget));
				mBudgetGoal.setTextColor(getColor(ratio));
				fillProgress(oRunningBudget);
			}
			else{
				onFinish();
			}
		}
	}
		
    /**called when the activity is first created**/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //extract all the views and databases from the main layout
        mBudgetGoal = (TextView)findViewById(R.id.budget_goal);
        mUnitSelect = (Spinner)findViewById(R.id.unit_select);
        mInputBox = (EditText)findViewById(R.id.input_box);
        mFoodInputBox = (EditText)findViewById(R.id.food_input_box);
        db_helper = new TrackingDatabase(getApplicationContext());
      }
    
    /**checks if any other activity has modified the budget information and refreshes**/
    @Override
    public void onResume(){
    	super.onResume();
        
    	//checks if it's a new day (this updates the last day opened information)
        if(isNewDay()){
        	resetData();
        }
    	
        restoreBudgetInfo(); 
        updateRunningBudget();
    } 

    /**to be called when the app is opened on a new day**/
    private void resetData(){
    	//clear the database of older entries
    	db_helper.clearDatabase();
    	
    	//reset the running budget
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        
        mBudget = settings.getInt(BUDGET, 2000);
        mRunningBudget = mBudget;
        
        editor.putInt(RUNNING_BUDGET, mBudget);
        editor.commit();
    }
    
    /**stores the budget data, that's it**/
    private void storeData(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        
        editor.putInt(RUNNING_BUDGET, mRunningBudget);
        editor.putInt(BUDGET, mBudget);
        editor.commit();
    }

    /**Updates the date information to current time then compares to when the app was last used*/    
    private boolean isNewDay(){
        Calendar today = Calendar.getInstance();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        
        int day = today.get(Calendar.DATE);
        int month = today.get(Calendar.MONTH);
        int year = today.get(Calendar.YEAR) - 1900;
        
        int lastDay = settings.getInt(LAST_DAY, day);
        int lastMonth = settings.getInt(LAST_MONTH, month);
        int lastYear = settings.getInt(LAST_YEAR, year);
        
		editor.putInt(LAST_DAY, day);
		editor.putInt(LAST_MONTH, month);
		editor.putInt(LAST_YEAR, year);
		editor.commit();
		
        return lastDay != day || lastMonth != month || lastYear != year;
    }
    
    /**Restores saved budget info including master budget and running budget*/
    private void restoreBudgetInfo(){
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mBudget = settings.getInt(BUDGET, 2000);
        mRunningBudget = settings.getInt(RUNNING_BUDGET, 2000);
        firstUse = settings.getBoolean(FIRST_USE, true);
        
        //displays information window on first use
        if(firstUse){
        	String title = this.getResources().getString(R.string.first_use_title);
        	String message = this.getResources().getString(R.string.first_use_content);
        	
        	new AlertDialog.Builder(this).setTitle(title).setMessage(message).setNeutralButton("OK", null).show();
        	firstUse = false;
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(FIRST_USE, firstUse);
            editor.commit();
        }
        
    }
    
    /**changes the Big Number to mRunningBudget and colors it*/  
    private void updateRunningBudget(){
    	double ratio = ((double)mRunningBudget)/mBudget;
    	mBudgetGoal.setText(Integer.toString(mRunningBudget));
    	mBudgetGoal.setTextColor(getColor(ratio));
    	fillProgress(mRunningBudget);
    }
    
    /**returns the appropriate color for the passed ratio**/
    private int getColor(double ratio){
    	int fColor = getResources().getColor(R.color.start);
    	int red;
    	int green;
    	int blue;
    	
    	int fromColor;
    	int toColor;
    	
    	//white
    	if(ratio>1.0){
    		fColor = getResources().getColor(R.color.start);
    	}
    	//white <> green
    	else if(ratio > 0.0){
    		fromColor = getResources().getColor(R.color.start);
    		toColor = getResources().getColor(R.color.good);
    		red = (int)((double)(Color.red(fromColor)-Color.red(toColor))*ratio + Color.red(toColor));
    		green = (int)((double)(Color.green(fromColor)-Color.green(toColor))*ratio + Color.green(toColor));
    		blue = (int)((double)(Color.blue(fromColor)-Color.blue(toColor))*ratio + Color.blue(toColor));
    		fColor = Color.rgb(red, green, blue);
    	}
    	//green<>yellow
    	else if(ratio > -0.2){
    		fromColor = getResources().getColor(R.color.good);
    		toColor = getResources().getColor(R.color.dangerous); 
    		red = (int)(5*(double)(Color.red(fromColor)-Color.red(toColor))*ratio + Color.red(fromColor));
    		green = (int)(5*(double)(Color.green(fromColor)-Color.green(toColor))*ratio + Color.green(fromColor));
    		blue = (int)(5*(double)(Color.blue(fromColor)-Color.blue(toColor))*ratio + Color.blue(fromColor));
    		fColor = Color.rgb(red, green, blue);
    	}
    	//yellow<>red
    	else if(ratio > -0.4){
    		fromColor = getResources().getColor(R.color.dangerous);
    		toColor = getResources().getColor(R.color.bad);  
    		red = (int)(5*(double)(Color.red(fromColor)-Color.red(toColor))*ratio - Color.red(toColor) + 2*Color.red(fromColor));
    		green = (int)(5*(double)(Color.green(fromColor)-Color.green(toColor))*ratio - Color.green(toColor) + 2*Color.green(fromColor));
    		blue = (int)(5*(double)(Color.blue(fromColor)-Color.blue(toColor))*ratio - Color.blue(toColor) + 2*Color.blue(fromColor));
    		fColor = Color.rgb(red, green, blue);
    	}
    	//red
    	else{
    		fColor = getResources().getColor(R.color.bad);
    	}
    	return fColor;
    }

    /**Fills the progress bar with val**/
    private void fillProgress(int val) {
    	ProgressBar pbar = (ProgressBar) findViewById(R.id.pBar);
    	pbar.setMax(mBudget);
    	pbar.setProgress(0); // Needed due to bug in android. yes, really.
    	pbar.setProgress(mBudget - val);
    } 
    
    /**reads food information user inputs and updates database and budget appropriately**/
    public void inputValue(View view){
    	
    	//nutrition content of food
    	Editable num = mInputBox.getText();
    	if (num.length() == 0) { 				//check for empty box
    		return;
    	}
		int value = Integer.parseInt(num.toString());
		
		//food name
    	String f_input = mFoodInputBox.getText().toString();
		
    	//if new day, reset the data to ensure food is logged in right place
        if(isNewDay()){
        	resetData();
        }
		
        //read unit selected and find total caloric content of entry
    	String unit = ((TextView)mUnitSelect.getSelectedView()).getText().toString();
    	value = value * getUnitValue(unit);
    	
    	addFoodToDatabase(f_input,value);
    	
    	//set up data for the countdown counter and store final data immediately for integrity purposes
    	oRunningBudget = mRunningBudget; 
    	mRunningBudget = mRunningBudget	- value;
		storeData();
    	step = (oRunningBudget - mRunningBudget)/12;
    	if(step == 0){
    		step = 1;
    	}
    	
    	//lock all the buttons to keep user from interfering with countdown
    	lockAllButtons();
    	BudgetCounter counter = new BudgetCounter(1200,60);
    	counter.start();
    }

    /**lock all buttons on the screen**/
    private void lockAllButtons(){
    	mInputBox.setClickable(false);
    	mFoodInputBox.setClickable(false);
    	mUnitSelect.setClickable(false);
    	findViewById(R.id.input_button).setClickable(false);
    	findViewById(R.id.m_history_button).setClickable(false);
    	findViewById(R.id.m_settings_button).setClickable(false);
    }
    
    /**unlock all buttons on the screen**/
    private void unlockAllButtons(){
    	mInputBox.setClickable(true);
    	mFoodInputBox.setClickable(true);
    	mUnitSelect.setClickable(true);
    	findViewById(R.id.input_button).setClickable(true);
    	findViewById(R.id.m_history_button).setClickable(true);
    	findViewById(R.id.m_settings_button).setClickable(true);    	
    }
    
    /**returns the caloric value of the unit passed into it*/  
    private int getUnitValue(String unit){
    	if(unit.compareTo("calorie")==0)
    		return 1;
    	if(unit.compareTo("g of sugar")==0)
    		return 4;
    	if(unit.compareTo("g of fat")==0)
    		return 7;
    	if(unit.compareTo("g of protein")==0)
    		return 4;
    	return 1;
    }
    
    /**creates food item based on passed information and adds it to the application database*/ 
    private void addFoodToDatabase(String food, int value){
    	Food d_input;
    	if(food == null){
    		d_input = new Food(value);
    	}
    	else{
    		d_input = new Food(food.trim(), value);
    	}
    	
    	//insert food into database
    	db_helper.insertTuple(d_input);
    }
    
    /**open the respective activity**/
    public void openProfile(View view){
    	Intent intent = new Intent(this, Profile.class);
    	startActivity(intent);
    }
    
    public void openSettings(View view) {
    	Intent intent = new Intent(this, Settings.class);
    	startActivity(intent);
    }
    
    public void openHistory(View view) {
    	Intent intent = new Intent(this, CalorieHistory.class);
    	startActivity(intent);
    }  
}
