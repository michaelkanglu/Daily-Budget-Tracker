package com.michael.android.budget;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class CalorieHistory extends Activity {
	private static final int BREAKFAST_BOUND = 5;
	private static final int LUNCH_BOUND = 11;
	private static final int DINNER_BOUND = 16;
	
	private TrackingDatabase db_helper;
	private boolean displayToday = true;
	private static final int EDIT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private FoodRow sRow;

    private boolean create_breakfast = true; //flag whether to create a new heading
    private boolean create_lunch = true; //flag whether to create a new heading
    private boolean create_dinner = true; //flag whether to create a new heading
    
    private static class FoodRow extends TableRow{
    	private int myID;
    	private static boolean rowColorFlag = true;  // True is light gray, false is dark gray.
    	
    	public FoodRow (Context context) {
    		super(context);
    	}
    	
    	public void setID(int id){
    		myID = id;
    	}
    	
    	public int getID(){
    		return myID;
    	}
    	
    	public void setCorrectColor() {
    		// Sets the proper, alternating background color.
    		// Ignores text rows (headings).
    		if (rowColorFlag) {
    			setBackgroundResource(R.color.lgray);
    		}
    		else {
    			setBackgroundResource(R.color.dgray);
    		}
    		rowColorFlag = !rowColorFlag;
    	}
    	
    	public static void setFlag(boolean bool) {
    		rowColorFlag = bool;
    	}
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);
        
        TableLayout tbl = (TableLayout)findViewById(R.id.foodtable);
        db_helper = new TrackingDatabase(getApplicationContext());
        
        createTitle(tbl);
        populateTable(tbl);
        setToggleButton();
    }
  
    public void populateTable(TableLayout tbl) {
    	resetHeadingFlags();
    	
		long time = System.currentTimeMillis();
		Calendar today = Calendar.getInstance();
		today.setTimeInMillis(time);
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND,0);
		today.set(Calendar.MILLISECOND,0);
		time = today.getTimeInMillis();
		
    	SQLiteDatabase db = db_helper.getReadableDatabase();
    	Cursor cursor;
    	if(displayToday){
    		cursor = db.rawQuery("SELECT * FROM " + "Track WHERE DateTime > " + time, null);
    	}
    	else{
    		cursor = db.rawQuery("SELECT * FROM " + "Track WHERE DateTime < " + time, null);
    	}
    	
        if (cursor.moveToFirst()) {
            do {
                int id = Integer.parseInt(cursor.getString(0));
            	String name = cursor.getString(1);
            	int value = Integer.parseInt(cursor.getString(2));
            	time = cursor.getLong(cursor.getColumnIndex("DateTime"));
            	Log.i("display", "id: " + id + " name: " + name + " cal: " + value); //TODO
                Food fd = new Food(name, value);
                createFoodRow(tbl, fd, id, time);
            } while (cursor.moveToNext());
        }
        db.close();
    }
    
    public void createFoodRow (TableLayout tbl, Food fd, int id, long time) {
    	// Gets data from the parameters, and creates the visual entry for the food.
    	makeHeading(tbl, time);
    	FoodRow fRow = new FoodRow(this);
    	fRow.setGravity(Gravity.CENTER_HORIZONTAL);
    	fRow.setCorrectColor();
    	
    	// Applying style to the text.
    	TextView name = new TextView(this);
    	TextView cal = new TextView(this);
    	name.setText(fd.getName());
    	cal.setText("" + fd.getValue());
    	name.setTextSize(20);
    	cal.setTextSize(20);
    	cal.setGravity(android.view.Gravity.RIGHT);
    	name.setPadding(10, 0, 0, 0);
    	
    	// Additional suffix to denote Calorie units.
    	TextView calText = new TextView(this);
    	calText.setText(" Cal");
    	calText.setTextSize(14);
    	calText.setPadding(0, 0, 10, 0);
    	
    	fRow.addView(name);
    	fRow.addView(cal);
    	fRow.addView(calText);
    	
        fRow.setID(id);
    	registerForContextMenu(fRow);
    	tbl.addView(fRow);
    }

	public void createTitle (TableLayout tbl) {
		// Create and add the main title to the table display.
    	
    	// Applying styles to the text.
        TextView title = new TextView(this);
        if(displayToday)
        	title.setText("Today you ate");
        else
        	title.setText("Yesterday you ate");
        title.setTextSize(36);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        
        //titleRow.addView(title);
        tbl.addView(title);
    }
	
	public TextView createHeading (TableLayout tbl, String text) {
		// Create a heading for the table display.
		
		// Applying styles to the text.
		TextView head = new TextView(this);
		head.setText(text);
		head.setTextSize(28);
		head.setTypeface(Typeface.DEFAULT_BOLD);
		FoodRow.setFlag(true);
		return head;
	}

	public void makeHeading(TableLayout tbl, long date) {
		// Creates a heading depending on the time of day the food was first entered
		// into the app.
		Calendar time = Calendar.getInstance();
		time.setTimeInMillis(date);
		int hour = time.get(Calendar.HOUR_OF_DAY);
		
		if (withinTime(hour, BREAKFAST_BOUND, LUNCH_BOUND) && create_breakfast) {
			tbl.addView(createHeading(tbl, "In the morning:"));
			create_breakfast = false;
		} else if (withinTime(hour, LUNCH_BOUND, DINNER_BOUND) && create_lunch) {
			tbl.addView(createHeading(tbl, "In the daytime:"));
			create_lunch = false;
		} else if (create_dinner) {
			tbl.addView(createHeading(tbl, "In the evening:"));
			create_dinner = false;
		}
	}
	
	public void resetHeadingFlags() {
		create_breakfast = true;
		create_lunch = true;
		create_dinner = true;
	}
	
	public boolean withinTime(int val, int start, int end) {
		return val >= start && val < end;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		sRow = (FoodRow) v;
		
		menu.add(0, EDIT_ID, 0, R.string.row_edit);
		menu.add(1, DELETE_ID, 1, R.string.row_delete);
	}
	
	public boolean onContextItemSelected(MenuItem item) {	
	    switch(item.getItemId()) {
	    case EDIT_ID:
	    	launchEditMenu();
	    	return true;
	    case DELETE_ID:
			deleteRow();
	    	return true;
	    }
	    return super.onContextItemSelected(item);
	}

	public void updateColors(TableLayout tbl) {
		// Repaint backgrounds starting from first element
		// This is called upon deleting an entry from the history.
		FoodRow.setFlag(true);
		for(int i=0; i<tbl.getChildCount(); i++) {
			if(tbl.getChildAt(i) instanceof FoodRow){
				FoodRow tempRow = (FoodRow) tbl.getChildAt(i);
				tempRow.setCorrectColor();
			}
			else {
				FoodRow.setFlag(true);
			}
		}
	}
		
	private void launchEditMenu() {

		//initialize the popupwindow and its information
		LayoutInflater layoutInflater  = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
	    final View popupView = layoutInflater.inflate(R.layout.edit_history_popup, null);  
	    final PopupWindow popupWindow = new PopupWindow(popupView, 
	               										LayoutParams.WRAP_CONTENT,  
	               										LayoutParams.WRAP_CONTENT);
	    

		
		String oFood = ((TextView)sRow.getVirtualChildAt(0)).getText().toString();
		int oValue = Integer.parseInt(((TextView)sRow.getVirtualChildAt(1)).getText().toString());
		
		EditText foodInputBox = (EditText)popupView.findViewById(R.id.e_food);
		EditText valueInputBox = (EditText)popupView.findViewById(R.id.e_value);
		
		foodInputBox.setText(oFood);
		valueInputBox.setText(""+oValue);
		
		//determine if there's room at the bottom
		if(checkMenuSpace(popupView)){
			//display below the selected row
			popupWindow.showAsDropDown(sRow, 50, -5);
		}
		else{
			//display above the selected row
			popupWindow.showAsDropDown(sRow, 50, -30 + (popupView.getMeasuredHeight() * -1));
		}
		
		popupWindow.setFocusable(true);
		popupWindow.update();
		
		//when edit button clicked
	    Button btnSubmitEdit = (Button)popupView.findViewById(R.id.h_submit_edit);
	    btnSubmitEdit.setOnClickListener(new View.OnClickListener(){
	    	public void onClick(View v) {
	    		editRow(popupView, popupWindow);
	    		popupWindow.dismiss();
	    	}
	    });
	    
	    //when cancel button clicked
	    Button btnCancelEdit = (Button)popupView.findViewById(R.id.h_cancel_edit);
	    btnCancelEdit.setOnClickListener(new View.OnClickListener(){
	    	public void onClick(View v) {
	    		popupWindow.dismiss();
	    	}
	    });
	}
	
    public void deleteRow(){
		TableLayout tbl = (TableLayout) findViewById(R.id.foodtable);
		int id = sRow.getID();
		int value = Integer.parseInt(((TextView)sRow.getVirtualChildAt(1)).getText().toString());
		
		//Update running budget
		if(displayToday){
			SharedPreferences settings = getSharedPreferences(DailyBudgetTrackerActivity.PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			int runningBudget = settings.getInt(DailyBudgetTrackerActivity.RUNNING_BUDGET, 2000);
			runningBudget = runningBudget + value;
			editor.putInt(DailyBudgetTrackerActivity.RUNNING_BUDGET, runningBudget);
			editor.commit();
		}
		
        //remove entry from database
		db_helper.deleteTuple(id);
		
		//remove row from table
		tbl.removeView(sRow);
		updateColors(tbl);
	}
    
	private boolean checkMenuSpace(View popupView){
		popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		ScrollView scroll = (ScrollView)findViewById(R.id.foodscroll);
		int scrollHeight = scroll.getHeight();
		int[] rowPos = {0, 0};
		int[] scrollPos = {0, 0};
		sRow.getLocationOnScreen(rowPos);
		scroll.getLocationOnScreen(scrollPos);
		int rowYPosition = rowPos[1]-scrollPos[1];
		int popHeight = popupView.getMeasuredHeight();

		return (scrollHeight - rowYPosition) > popHeight;
	}
	
	private void editRow(View popupView, PopupWindow popupWindow){
		//String oFood = ((TextView)sRow.getVirtualChildAt(0)).getText().toString();
		//fetch all relevant information
		int oValue = Integer.parseInt(((TextView)sRow.getVirtualChildAt(1)).getText().toString());
		
		EditText foodInputBox = (EditText)popupView.findViewById(R.id.e_food);
		EditText valueInputBox = (EditText)popupView.findViewById(R.id.e_value);
		
		String nFood = foodInputBox.getText().toString();
		int nValue;
		
    	Context context = getApplicationContext();
		int duration = Toast.LENGTH_LONG;
		try{
   		 	nValue = Integer.parseInt(valueInputBox.getText().toString());
		}
		//catch non-numerals
		catch(NumberFormatException e){
   		
			CharSequence text = "The value is not a valid, whole number!";
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
			valueInputBox.setText(null);
			return;
		}
   	
		//catch negative numbers
		if(nValue < 0){
			CharSequence text = "The value is less than zero!";

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
			valueInputBox.setText(null);
			return;
		}
		int rowID = sRow.getID();
		
		Food food = new Food(nFood, nValue);
		
		//update the running budget
		if(displayToday){
			int diff = oValue - nValue;
			SharedPreferences settings = getSharedPreferences(DailyBudgetTrackerActivity.PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			int runningBudget = settings.getInt(DailyBudgetTrackerActivity.RUNNING_BUDGET, 2000);
			runningBudget = runningBudget + diff;
			editor.putInt(DailyBudgetTrackerActivity.RUNNING_BUDGET, runningBudget);
			editor.commit();
		}
			
		//update the row
		((TextView)sRow.getVirtualChildAt(0)).setText(nFood);
		((TextView)sRow.getVirtualChildAt(1)).setText("" + nValue);
        
		//update the database
		db_helper.updateTuple(rowID, food);
		}
	
	public void goBack(View view) {
		finish();
	}
	
	public void displayToggle(View view){
		displayToday = !displayToday;
		TableLayout tbl = (TableLayout)findViewById(R.id.foodtable);
        tbl.removeAllViews();
		createTitle(tbl);
        populateTable(tbl);
        setToggleButton();
	}
	
	public void setToggleButton(){
		if(displayToday)
			((Button)findViewById(R.id.h_display_toggle)).setText(this.getResources().getString(R.string.history_yesterday));
		else
			((Button)findViewById(R.id.h_display_toggle)).setText(this.getResources().getString(R.string.history_today));
	}
}