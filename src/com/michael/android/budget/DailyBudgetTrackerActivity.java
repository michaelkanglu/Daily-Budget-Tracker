package com.michael.android.budget;

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class DailyBudgetTrackerActivity extends Activity {
	private TrackingDatabase db_helper;
	public static final String PREFS_NAME = "budgettracker.MyPrefsFile";
	static final String RUNNING_BUDGET = "budgettracker.running_budget";
	static final String BUDGET = "budgettracker.budget";
	
	static final String EMAIL = "budgettracker.email";
	static final String EXPORT = "budgettracker.export";

	static final String LAST_DAY = "budgettracker.lday";
	static final String LAST_MONTH = "budgettracker.lmonth";
	static final String LAST_YEAR = "budgettracker.lyear";
		
		TextView mBudgetGoal;
		EditText mInputBox;
		Spinner mUnitSelect;
		EditText mFoodInputBox;
		
		int mBudget;
		int mRunningBudget;
		
		int[] mUnitValues; //currently unused
		
		//updated when app created and saved when app is destroyed
		int mDay;
		int mMonth;
		int mYear;
		
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
       
      SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
      SharedPreferences.Editor editor = settings.edit();
      
      editor.putInt(RUNNING_BUDGET, mRunningBudget);
      editor.putInt(BUDGET, mBudget);
      
      editor.commit();
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

      editor.commit();
    }
    
    /*reads food information user inputs and updates database and budget appropriately**/
    public void inputValue(View view){
    	String v_input = mInputBox.getText().toString();
    	String f_input = mFoodInputBox.getText().toString();
    	int value = 0;
    	
    	Context context = getApplicationContext();
		int duration = Toast.LENGTH_LONG;
    	//catch number format exception
    	try{
    		 value = Integer.parseInt(v_input);
    	}
    	catch(NumberFormatException e){
    		
    		CharSequence text = "The value is not a valid, whole number!";
    		Toast toast = Toast.makeText(context, text, duration);
    		toast.show();
    		mInputBox.setText(null);
    		return;
    	}
    	
    	//catch negative numbers
    	if(value < 0){
    		CharSequence text = "The value is less than zero!";

    		Toast toast = Toast.makeText(context, text, duration);
    		toast.show();
    		mInputBox.setText(null);
    		return;
    	}
    	
    	String unit = ((TextView)mUnitSelect.getSelectedView()).getText().toString();
    	value = value * getUnitValue(unit);
    	
    	addFoodToDatabase(f_input,value);
    	
    	mRunningBudget = mRunningBudget - value;
    	updateRunningBudget();
    }
    
    public void openSettings(View view) {
    	Intent intent = new Intent(this, SettingsActivity.class);
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
    		d_input = new Food(food, value);
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
    	mBudgetGoal.setText(Integer.toString(mRunningBudget));
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
    }
}