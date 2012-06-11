package com.michael.android.budget;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
		
		//updated when app created and saved when app is destroyed
		int mDay;
		int mMonth;
		int mYear;
		
		//countdowntimer is an abstract class, so extend it and fill in methods
	public class BudgetCounter extends CountDownTimer{
		public BudgetCounter(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}
		@Override
		public void onFinish() {
			updateRunningBudget();
			unlockAllButtons();
		}
		@Override
		public void onTick(long millisUntilFinished) {
			
			oRunningBudget = oRunningBudget - step;
			double ratio = ((double)oRunningBudget)/mBudget;
			if(oRunningBudget > mRunningBudget){
				mBudgetGoal.setText(Integer.toString(oRunningBudget));
				mBudgetGoal.setTextColor(getColor(ratio));
			}
			else{
				onFinish();
			}
		}
	}
		
    /** Called when the activity is first created. */
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
        
        //checks if it's a newday (this updates the last day opened information)
        if(isNewDay()){
        	resetData();
        }

        //initialize all the information the widgets need to contain
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(	this, 
        																		R.array.unit_array, 
        																		android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mUnitSelect.setAdapter(adapter);
        mUnitValues = getResources().getIntArray(R.array.unit_array);
      }
    
    //checks if any other activity has modified the budget information and refreshes
    @Override
    public void onResume(){
    	super.onResume();
    	
        restoreBudgetInfo(); 
        updateRunningBudget();
    }
    
    //saves most up to date budget information for other activites to use
    @Override
    protected void onPause(){
       super.onPause();
       
      storeData();
    }  
    
    //saves most up to date information and the date app was last OPENED
    @Override
    protected void onDestroy(){
       super.onDestroy();
       
      SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
      SharedPreferences.Editor editor = settings.edit();
      
      editor.putInt(RUNNING_BUDGET, mRunningBudget);
      editor.putInt(BUDGET, mBudget);
      
      editor.putInt(LAST_DAY, mDay);
      editor.putInt(LAST_MONTH, mMonth);
      editor.putInt(LAST_YEAR, mYear);

      editor.putBoolean(FIRST_USE, firstUse);
      editor.commit();
    }
    
    private void storeData(){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        
        editor.putInt(RUNNING_BUDGET, mRunningBudget);
        editor.putInt(BUDGET, mBudget);
        editor.commit();
    }
    
    /*reads food information user inputs and updates database and budget appropriately**/
    public void inputValue(View view){
    	lockAllButtons();
		int value = Integer.parseInt(mInputBox.getText().toString());
    	String f_input = mFoodInputBox.getText().toString();


		// Catch negative numbers
		if(value < 0){
			CharSequence text = "The value is less than zero!";
			Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
			toast.show();
			mInputBox.setText(null);
			unlockAllButtons();
			return;
		}
    	
    	String unit = ((TextView)mUnitSelect.getSelectedView()).getText().toString();
    	value = value * getUnitValue(unit);
    	
    	addFoodToDatabase(f_input,value);
    	
    	oRunningBudget = mRunningBudget; 
    	mRunningBudget = mRunningBudget	- value;
		storeData();
    	step = (oRunningBudget - mRunningBudget)/12;
    	if(step == 0){
    		step = 1;
    	}
    	BudgetCounter counter = new BudgetCounter(1200,60);
    	counter.start();
    }
    
    public void openSettings(View view) {
    	Intent intent = new Intent(this, Settings.class);
    	startActivity(intent);
    }
    
    public void openHistory(View view) {
    	Intent intent = new Intent(this, CalorieHistory.class);
    	startActivity(intent);
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
    
    /**to be called when the app is opened on a new day**/
    private void resetData(){
    	//reset the database
    	db_helper.clearDatabase();
    	
    	//reset the running budget
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(RUNNING_BUDGET, settings.getInt(BUDGET, 2000));
        editor.commit();
    }
    
    /**Changes the Big Number to be what running budget currently is*/  
    private void updateRunningBudget(){
    	double ratio = ((double)mRunningBudget)/mBudget;
    	mBudgetGoal.setText(Integer.toString(mRunningBudget));
    	mBudgetGoal.setTextColor(getColor(ratio));
    	mInputBox.setText(null);
    	mFoodInputBox.setText(null);
    }
    
    /**Updates the master date information to current time then compares to when the app was last used*/    
    private boolean isNewDay(){
        Calendar today = Calendar.getInstance();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        
        mDay = today.get(Calendar.DATE);
        mMonth = today.get(Calendar.MONTH);
        mYear = today.get(Calendar.YEAR) - 1900;
        
        int lastDay = settings.getInt(LAST_DAY, mDay);
        int lastMonth = settings.getInt(LAST_MONTH, mMonth);
        int lastYear = settings.getInt(LAST_YEAR, mYear);
        
        return lastDay != mDay || lastMonth != mMonth || lastYear != mYear;
    }
    
    /**Restores saved budget info including master budget and running budget*/
    private void restoreBudgetInfo(){
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mBudget = settings.getInt(BUDGET, 2000);
        mRunningBudget = settings.getInt(RUNNING_BUDGET, 2000);
        firstUse = settings.getBoolean(FIRST_USE, true);
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
    
    private void lockAllButtons(){
    	mInputBox.setClickable(false);
    	mFoodInputBox.setClickable(false);
    	mUnitSelect.setClickable(false);
    	findViewById(R.id.input_button).setClickable(false);
    	findViewById(R.id.m_history_button).setClickable(false);
    	findViewById(R.id.m_settings_button).setClickable(false);
    }
    
    private void unlockAllButtons(){
    	mInputBox.setClickable(true);
    	mFoodInputBox.setClickable(true);
    	mUnitSelect.setClickable(true);
    	findViewById(R.id.input_button).setClickable(true);
    	findViewById(R.id.m_history_button).setClickable(true);
    	findViewById(R.id.m_settings_button).setClickable(true);    	
    }
    private int getColor(double ratio){
    	int fColor = getResources().getColor(R.color.start);
    	int red;
    	int green;
    	int blue;
    	
    	int fromColor;
    	int toColor;
    	
    	if(ratio > 0.0){
    		fromColor = getResources().getColor(R.color.start);
    		toColor = getResources().getColor(R.color.good);
    		red = (int)((double)(Color.red(fromColor)-Color.red(toColor))*ratio + Color.red(toColor));
    		green = (int)((double)(Color.green(fromColor)-Color.green(toColor))*ratio + Color.green(toColor));
    		blue = (int)((double)(Color.blue(fromColor)-Color.blue(toColor))*ratio + Color.blue(toColor));
    		fColor = Color.rgb(red, green, blue);
    	}
    	else if(ratio > -0.2){
    		fromColor = getResources().getColor(R.color.good);
    		toColor = getResources().getColor(R.color.dangerous); 
    		red = (int)(5*(double)(Color.red(fromColor)-Color.red(toColor))*ratio + Color.red(fromColor));
    		green = (int)(5*(double)(Color.green(fromColor)-Color.green(toColor))*ratio + Color.green(fromColor));
    		blue = (int)(5*(double)(Color.blue(fromColor)-Color.blue(toColor))*ratio + Color.blue(fromColor));
    		fColor = Color.rgb(red, green, blue);
    	}
    	else if(ratio > -0.4){
    		fromColor = getResources().getColor(R.color.dangerous);
    		toColor = getResources().getColor(R.color.bad);  
    		red = (int)(5*(double)(Color.red(fromColor)-Color.red(toColor))*ratio - Color.red(toColor) + 2*Color.red(fromColor));
    		green = (int)(5*(double)(Color.green(fromColor)-Color.green(toColor))*ratio - Color.green(toColor) + 2*Color.green(fromColor));
    		blue = (int)(5*(double)(Color.blue(fromColor)-Color.blue(toColor))*ratio - Color.blue(toColor) + 2*Color.blue(fromColor));
    		fColor = Color.rgb(red, green, blue);
    	}
    	else{
    		fColor = getResources().getColor(R.color.bad);
    	}
    	return fColor;
    }
}