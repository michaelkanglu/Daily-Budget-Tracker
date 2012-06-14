package com.michael.android.budget;

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
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

public class CalorieHistory extends Activity {
	
	// Defines times for the different meals of the day
	private static final int BREAKFAST_BOUND = 5;
	private static final int LUNCH_BOUND = 11;
	private static final int DINNER_BOUND = 16;
	private static final int MIDNIGHT_BOUND = 24;
	
	// Boolean toggle for displaying today's or yesterday's contents
	private boolean displayToday = true;
	
	// For Database access and manipulation
	private TrackingDatabase db_helper;
	private static final int EDIT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private FoodRow sRow;
    
    // Flag whether to create a new heading
    private boolean create_breakfast = true; 
    private boolean create_lunch = true; 
    private boolean create_dinner = true; 
    private boolean create_midnight = true;
    
    private static class FoodRow extends TableRow {
        // TableRow that contains food information 
    	// and contains id of corresponding database entry

    	private int databaseID;
    	private static boolean rowColorFlag = true;  
    	// True is light gray, false is dark gray.
    	
    	public FoodRow (Context context) {
    		super(context);
    	}
    	
    	public void setDatabaseID(int id){
    		databaseID = id;
    	}
    	
    	public int getDatabaseID(){
    		return databaseID;
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
        // Populate the history table with all database entries
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
    	
    	// Select either yesterday's or today's entries.
    	if(displayToday){
    		cursor = db.rawQuery("SELECT * FROM Track " + 
    							 "WHERE DateTime > " + time, null);
    	}
    	else{
    		cursor = db.rawQuery("SELECT * FROM Track " + 
    							 "WHERE DateTime < " + time, null);
    	}
    	
		// Retrieve entries and add them to table.
        if (cursor.moveToFirst()) {
            do {
                int id = Integer.parseInt(cursor.getString(0));
            	String name = cursor.getString(1);
            	int value = Integer.parseInt(cursor.getString(2));
            	time = cursor.getLong(cursor.getColumnIndex("DateTime"));
                Food fd = new Food(name, value);
                createFoodRow(tbl, fd, id, time);
            } while (cursor.moveToNext());
        }
        db.close();
    }  
	
	public void resetHeadingFlags() {
		// Resets the flags used for generating time-of-day groups.
		create_breakfast = true;
		create_lunch = true;
		create_dinner = true;
		create_midnight = true;
	}
	
    public void createFoodRow (TableLayout tbl, Food fd, int id, long time) {
    	// Generates a foodRow object from fd and adds it to the table
    	
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
    	
        fRow.setDatabaseID(id);
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
		// Creates a heading depending on the time of day the food was first entered into the app.
		
		Calendar time = Calendar.getInstance();
		time.setTimeInMillis(date);
		int hour = time.get(Calendar.HOUR_OF_DAY);
		
		if (withinTime(hour, MIDNIGHT_BOUND, BREAKFAST_BOUND) 
				&& create_midnight) {
			tbl.addView(createHeading(tbl, "Past midnight:"));
			create_midnight = false;
		} else if (withinTime(hour, BREAKFAST_BOUND, LUNCH_BOUND) 
				&& create_breakfast) {
			tbl.addView(createHeading(tbl, "In the morning:"));
			create_breakfast = false;
		} else if (withinTime(hour, LUNCH_BOUND, DINNER_BOUND) 
				&& create_lunch) {
			tbl.addView(createHeading(tbl, "In the daytime:"));
			create_lunch = false;
		} else if (withinTime(hour, DINNER_BOUND, MIDNIGHT_BOUND) 
				&& create_dinner) {
			tbl.addView(createHeading(tbl, "In the evening:"));
			create_dinner = false;
		}
	}
	
	public boolean withinTime(int val, int start, int end) {
		return (val%24) >= (start%24) && val < end;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// Generates a context menu for the purpose of editing/deleting database entries.
		
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


	private void launchEditMenu() {
		// Launches the edit menu to edit entries
		
		// Initialize the popupwindow and its information
		LayoutInflater layoutInflater  =
				(LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
	    final View popupView = 
	    		layoutInflater.inflate(R.layout.edit_history_popup, null);  
	    final PopupWindow popupWindow = new PopupWindow(popupView, 
	            										LayoutParams.WRAP_CONTENT,  
	            										LayoutParams.WRAP_CONTENT);
	    
		String oFood = ((TextView)sRow.getVirtualChildAt(0)).getText().toString();
		int oValue = Integer.parseInt(((TextView)sRow.getVirtualChildAt(1)).getText().toString());
		
		EditText foodInputBox = (EditText)popupView.findViewById(R.id.e_food);
		EditText valueInputBox = (EditText)popupView.findViewById(R.id.e_value);
		
		foodInputBox.setText(oFood);
		valueInputBox.setText(""+oValue);
		
		// Place focus at the end of the edit text, rather than the beginning.
		foodInputBox.setSelection(foodInputBox.getText().length());
		valueInputBox.setSelection(valueInputBox.getText().length());
		
		// Determine if there's room at the bottom
		if(hasBottomMenuSpace(popupView)){
			popupWindow.showAsDropDown(sRow, 50, -5);
		}
		else{
			popupWindow.showAsDropDown(sRow, 50, -30 + (popupView.getMeasuredHeight() * -1));
		}
		
		popupWindow.setFocusable(true);
		popupWindow.update();
		
		// When edit button clicked
	    Button btnSubmitEdit = (Button)popupView.findViewById(R.id.h_submit_edit);
	    btnSubmitEdit.setOnClickListener(new View.OnClickListener(){
	    	public void onClick(View v) {
	    		editRow(popupView, popupWindow);
	    		popupWindow.dismiss();
	    	}
	    });
	    
	    // When cancel button clicked
	    Button btnCancelEdit = (Button)popupView.findViewById(R.id.h_cancel_edit);
	    btnCancelEdit.setOnClickListener(new View.OnClickListener(){
	    	public void onClick(View v) {
	    		popupWindow.dismiss();
	    	}
	    });
	}
    
	private boolean hasBottomMenuSpace(View popupView){
		// Returns true if there's enough room to display
		// popupView below the selected row, false otherwise
		
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
		// Edits database, table, and running budget
		
		// Fetch all relevant information
		int oValue = Integer.parseInt(((TextView)sRow.getVirtualChildAt(1)).getText().toString());
		
		EditText foodInputBox = (EditText)popupView.findViewById(R.id.e_food);
		EditText valueInputBox = (EditText)popupView.findViewById(R.id.e_value);

		String nFood = foodInputBox.getText().toString();
		int nValue = Integer.parseInt(valueInputBox.getText().toString());
		int rowID = sRow.getDatabaseID();

		Food food = new Food(nFood, nValue);

		// Update the running budget
		if(displayToday){
			int diff = oValue - nValue;
			SharedPreferences settings = getSharedPreferences(DailyBudgetTracker.PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			int runningBudget = settings.getInt(DailyBudgetTracker.RUNNING_BUDGET, 2000);
			runningBudget = runningBudget + diff;
			editor.putInt(DailyBudgetTracker.RUNNING_BUDGET, runningBudget);
			editor.commit();
		}
	
		// Update the row
		((TextView)sRow.getVirtualChildAt(0)).setText(nFood);
		((TextView)sRow.getVirtualChildAt(1)).setText("" + nValue);

		// Update the database
		db_helper.updateTuple(rowID, food);
	}
	
    public void deleteRow(){
    	// Delete entry from database, table, and running budget
    	
    	TableLayout tbl = (TableLayout) findViewById(R.id.foodtable);
		int id = sRow.getDatabaseID();
		int value = Integer.parseInt(((TextView)sRow.getVirtualChildAt(1)).getText().toString());
		
		// Update running budget
		if(displayToday){
			SharedPreferences settings = getSharedPreferences(DailyBudgetTracker.PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			int runningBudget = settings.getInt(DailyBudgetTracker.RUNNING_BUDGET, 2000);
			runningBudget = runningBudget + value;
			editor.putInt(DailyBudgetTracker.RUNNING_BUDGET, runningBudget);
			editor.commit();
		}
		
        // Remove entry from database
		db_helper.deleteTuple(id);
		
		// Remove row from table
		tbl.removeView(sRow);
		updateColors(tbl);
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

	public void displayToggle(View view){
		// Toggle between viewing yesterday's and today's food. Updates everything
		
		displayToday = !displayToday;
		TableLayout tbl = (TableLayout)findViewById(R.id.foodtable);
        tbl.removeAllViews();
		createTitle(tbl);
        populateTable(tbl);
        setToggleButton();
	}
	
	public void setToggleButton(){
		// Changes the toggle button to either yesterday or today.
		if(displayToday)
			((Button)findViewById(R.id.h_display_toggle)).setText(this.getResources().getString(R.string.history_yesterday));
		else
			((Button)findViewById(R.id.h_display_toggle)).setText(this.getResources().getString(R.string.history_today));
	}
	
	public void goBack(View view) {
		// The Back Button
		finish();
	}
	
}