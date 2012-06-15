package com.michael.android.budget;

import java.util.concurrent.atomic.AtomicReference;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.Toast;

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
	boolean mGender;		//true=female, false=male
	float mAge;
	float mHeight;
	float mWeight;
	int weeklyIndex;
	int activiIndex;
	
	boolean spinnerMutex; 	//prevents the spinners onItemSelected from being called too early
	boolean editTextMutex;	//prevents edittext listener from being called at inoppurtune times
	
	float weightLbs;
	float weightKgs;
	
	float heightIns;
	float heightCms;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		/**the spinner listeners will fire when the spinners gets called
		 * as it considers it an instance of selecting an item. The mutex 
		 * makes the listener code only fire upon a user powered selection
		 */
		spinnerMutex=false;
		setContentView(R.layout.profile);
		
	}
	
	/**reinitializes old profile information and sets the spinner listeners**/
	@Override
	public void onResume(){
		super.onResume();
		
		//activity level spinner listener
		((Spinner)findViewById(R.id.p_activity_spinner)).setOnItemSelectedListener(new OnItemSelectedListener() {
		    
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		        if(spinnerMutex){
		        	activiIndex = position;
		        	updateRecommendation();
		        }
		    }
		    
		    public void onNothingSelected(AdapterView<?> parentView) {
		        return;
		    }
		});
		
		//weekly weight loss spinner listener
		((Spinner)findViewById(R.id.p_weekly_spinner)).setOnItemSelectedListener(new OnItemSelectedListener() {
		    
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		        if(spinnerMutex){
		        	weeklyIndex = position;
		        	updateRecommendation();
		        }
		    }
		    
		    public void onNothingSelected(AdapterView<?> parentView) {
		        return;
		    }
		});
		
		restoreButtonInfo();
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
	
	private void restoreButtonInfo(){
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
	
	private void saveButtonInfo(){
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

	/**launches the gender window**/
	public void launchSexWindow(View view){
		
		AtomicReference<View> ref = new AtomicReference<View>();
		final PopupWindow popupWindow = displayPopUpWindow(R.layout.pro_gender_popup, ref);
		View popupView = ref.get();
		
		//set gender to female, close window
	    Button btnFemale = (Button)popupView.findViewById(R.id.p_female_select);
	    btnFemale.setOnClickListener(new View.OnClickListener(){
	    	public void onClick(View v) {
	    		mGender = true;
	    		setSexText(mGender);
	    		updateRecommendation();
	    		popupWindow.dismiss();
	    	}
	    });
	    
	    //set gender to male, close window
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
		
		AtomicReference<View> ref = new AtomicReference<View>();
		final PopupWindow popupWindow = displayPopUpWindow(R.layout.pro_age_popup, ref);
		View popupView = ref.get();
		
		//initialize the Editbox to the current age
	    final EditText ageInputBox = (EditText)popupView.findViewById(R.id.p_age_input_box);
	    ageInputBox.setText(Integer.toString((int)mAge));
	    //place focus at the end of the edit text, rather than the beginning.
	 	ageInputBox.setSelection(ageInputBox.getText().length());	
	 	
	 	//when submit button clicked, save age, update big number, close window
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
	    
	    //when cancel button clicked, close the window
	    Button btnCancelAge = (Button)popupView.findViewById(R.id.p_cancel_age);
	    btnCancelAge.setOnClickListener(new View.OnClickListener(){
	    	public void onClick(View v) {
	    		popupWindow.dismiss();
	    	}
	    });
	}
	
	public void launchWeightWindow(View view){
		
		AtomicReference<View> ref = new AtomicReference<View>();
		final PopupWindow popupWindow = displayPopUpWindow(R.layout.pro_weight_popup, ref);
		View popupView = ref.get();
		
		//put current weight in edit box
		final EditText weightInputBox = (EditText)popupView.findViewById(R.id.p_weight_input_box);
		weightInputBox.setText(Integer.toString((int)mWeight));
		//place focus at the end of the edit text, rather than the beginning.
		weightInputBox.setSelection(weightInputBox.getText().length());
		 

		//set the unit to the correct one
		final TextView weightCaption = (TextView)popupView.findViewById(R.id.p_weight_caption);
		if(unitPounds){
		   weightCaption.setText(this.getResources().getString(R.string.p_weight_unit_im));
		   weightLbs=mWeight;
		   weightKgs=mWeight/2.2f;
		}
		else{
		   weightCaption.setText(this.getResources().getString(R.string.p_weight_unit_si));
		   weightKgs=mWeight;
		   weightLbs=mWeight*2.2f;
		}
		
		weightInputBox.addTextChangedListener(new TextWatcher() {
			
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				return;
				
			}
			
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				return;
				
			}
			
			public void afterTextChanged(Editable s) {
				if(editTextMutex){
					if(unitPounds){
						if(s.length()!=0)
							weightLbs = (float)Integer.parseInt(s.toString());
						else
							weightLbs = 0;
						weightKgs = weightLbs/2.2f;
					}
					else{
						if(s.length()!=0)
							weightKgs = (float)Integer.parseInt(s.toString());
						else
							weightKgs = 0;
						weightLbs = weightKgs*2.2f;
					}
				}
			}
		});
		
		//when toggle button clicked, toggle user input and saved value
		Button btnToggleUnit = (Button)popupView.findViewById(R.id.p_weight_toggle);
		btnToggleUnit.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				editTextMutex = false;
				unitPounds = !unitPounds;
				
				//convert the user input to the right units
			    if(unitPounds){
			    	weightCaption.setText(v.getResources().getString(R.string.p_weight_unit_im));
			    	mWeight = mWeight * 2.2f;
			    	weightInputBox.setText(Integer.toString((int)weightLbs));
			    }
			    else{
			    	weightCaption.setText(v.getResources().getString(R.string.p_weight_unit_si));
			    	mWeight = mWeight / 2.2f;
			    	weightInputBox.setText(Integer.toString((int)weightKgs));
			    }
		    	// Place focus at the end of the edit text, rather than the beginning.
			 	weightInputBox.setSelection(weightInputBox.getText().length());
			 	
			 	//convert the current saved weight to the right units
		    	setWeightText((int)mWeight);
		    	editTextMutex = true;
			}
		});
		
		//when submit button clicked, save user input, update recommendation, close window
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
	    
	    //when cancel button clicked, close window
	    Button btnCancelWeight = (Button)popupView.findViewById(R.id.p_cancel_weight);
	    btnCancelWeight.setOnClickListener(new View.OnClickListener(){
	    	public void onClick(View v) {
	    		popupWindow.dismiss();
	    	}
	    });
	}
	
	public void launchHeightWindow(View view){

		AtomicReference<View> ref = new AtomicReference<View>();
		final PopupWindow popupWindow = displayPopUpWindow(R.layout.pro_height_popup, ref);
		View popupView = ref.get();
	    
	    final EditText firstInputBox = (EditText)popupView.findViewById(R.id.p_first_input_box);
	    final EditText secondInputBox = (EditText)popupView.findViewById(R.id.p_second_input_box);
	    
	    
	    final TextView firstCaption = (TextView)popupView.findViewById(R.id.p_first_caption);
	    final TextView secondCaption = (TextView)popupView.findViewById(R.id.p_second_caption);
	    
	    //set the captions and input boxes to the saved values and units
	    if(unitInches){
	    	firstCaption.setText(this.getResources().getString(R.string.p_height1_unit_im));
	    	secondCaption.setText(this.getResources().getString(R.string.p_height2_unit_im));
	    	firstInputBox.setText(Integer.toString((int)mHeight/12));
	    	secondInputBox.setText(Integer.toString((int)mHeight%12));
	    	heightIns = mHeight;
	    	heightCms = mHeight * 2.54f;
	    }
	    else{
	    	firstCaption.setText(this.getResources().getString(R.string.p_height1_unit_si));
	    	secondCaption.setText(this.getResources().getString(R.string.p_height2_unit_si));
	    	firstInputBox.setText(Integer.toString((int)mHeight/100));
	    	secondInputBox.setText(Integer.toString((int)mHeight%100));
	    	heightCms = mHeight;
	    	heightIns = mHeight / 2.54f;
	    }
	    //place focus at the end of the edit text, rather than the beginning.
	 	firstInputBox.setSelection(firstInputBox.getText().length());
	 	secondInputBox.setSelection(secondInputBox.getText().length());
		
	 	TextWatcher textWatcher = new TextWatcher() {
			
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				return;
				
			}
			
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				return;
				
			}
			
			public void afterTextChanged(Editable s) {
				if(editTextMutex){
					if(unitInches){
						heightIns = 0.0f;
						if(firstInputBox.length()!=0){
							heightIns = heightIns + (Integer.parseInt(firstInputBox.getText().toString())*12);
						}
						if(secondInputBox.length()!=0){
							heightIns = heightIns + Integer.parseInt(secondInputBox.getText().toString());
						}
						heightCms = heightIns * 2.54f;
					}
					else{
						StringBuffer buffer = new StringBuffer();
						if(firstInputBox.length()!=0){
							buffer.append(firstInputBox.getText());
						}
						else{
							buffer.append(0);
						}
						buffer.append('.');
						if(secondInputBox.length()!=0){
							buffer.append(secondInputBox.getText());
						}
						else{
							buffer.append(0);
						}
						heightCms = Float.parseFloat(buffer.toString());
						heightCms = heightCms * 100f;
						heightIns = heightCms / 2.54f;
					}
				}	
			}
		};
	 	
		firstInputBox.addTextChangedListener(textWatcher);
		secondInputBox.addTextChangedListener(textWatcher);
		
	 	//when toggle button clicked, toggle user input and saved value
		Button btnToggleUnit = (Button)popupView.findViewById(R.id.p_height_toggle);
		btnToggleUnit.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				editTextMutex = false;
				unitInches = !unitInches;
				
				//if changing to inches
			    if(unitInches){
			    	//set unit text
			    	firstCaption.setText(v.getResources().getString(R.string.p_height1_unit_im));
			    	secondCaption.setText(v.getResources().getString(R.string.p_height2_unit_im));
			    	
			    	//convert saved height
			    	mHeight = mHeight / 2.54f;
			    	
			    	firstInputBox.setText(Integer.toString((int)heightIns/12));
			    	secondInputBox.setText(Integer.toString((int)heightIns%12));
			    }
			    //if changing to meters
			    else{
			    	firstCaption.setText(v.getResources().getString(R.string.p_height1_unit_si));
			    	secondCaption.setText(v.getResources().getString(R.string.p_height2_unit_si));
			    	
			    	//convert saved height
			    	mHeight = mHeight * 2.54f;
			    	
			    	String cmString = Integer.toString((int)heightCms);
			    	
			    	firstInputBox.setText(Integer.toString((int)heightCms/100));
			    	secondInputBox.setText(cmString.substring(cmString.length()-2));
			    }
				// Place focus at the end of the edit text, rather than the beginning.
			 	firstInputBox.setSelection(firstInputBox.getText().length());
			 	secondInputBox.setSelection(secondInputBox.getText().length());
			 	
			 	//update saved data display
			    setHeightText((int)mHeight);
			    editTextMutex = true;
			}
		});	
		
		//when submit button clicked, save user input, update recommendation, and close window
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
	    
	    // When cancel button clicked, close window
	    Button btnCancelHeight = (Button)popupView.findViewById(R.id.p_cancel_height);
	    btnCancelHeight.setOnClickListener(new View.OnClickListener(){
	    	public void onClick(View v) {
	    		popupWindow.dismiss();
	    	}
	    });
	}

	/**display the inputed layout as a PopupWindow and return it**/
	private PopupWindow displayPopUpWindow(int id, AtomicReference<View> ref){
		LayoutInflater layoutInflater  =
				(LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
	    View popupView = 
	    		layoutInflater.inflate(id, null);  
	    PopupWindow popupWindow = new PopupWindow(popupView, 
	            										LayoutParams.WRAP_CONTENT,  
	            										LayoutParams.WRAP_CONTENT);
	    
	    centerPopupWindow(popupView, popupWindow);
	    
		popupWindow.setFocusable(true);
		popupWindow.update();
		
		ref.set(popupView);
		return popupWindow;
	}
	
	/**center the popup window to the top of the screen**/
	private void centerPopupWindow(View popupView, PopupWindow popupWindow){
		popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		View parent = findViewById(R.id.p_activity_spinner).getRootView();
		
		popupWindow.showAtLocation(parent, Gravity.TOP, 0, 60);
	}
	
	/**cause buttons/spinners to disply user's profile data**/
	private void setSexText(Boolean sex){
		Button button = (Button)findViewById(R.id.p_sex_button);
		if(sex)
			button.setText("Female");
		else
			button.setText("Male");
	}
	
	private void setAgeText(int age){
		Button button = (Button)findViewById(R.id.p_age_button);
		button.setText(Integer.toString(age) + "yrs");
	}
	
	private void setHeightText(int height){
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
	
	private void setWeightText(int weight){
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
	
	private void setWeeklyItem(int index){
		Spinner spinner = (Spinner)findViewById(R.id.p_weekly_spinner);
		spinner.setSelection(index);
	}
	
	private void setActivityItem(int index){
		Spinner spinner = (Spinner)findViewById(R.id.p_activity_spinner);
		spinner.setSelection(index);
	}
	
	/**change the big number to display the recommended budget**/
	public void updateRecommendation(){
		((TextView)findViewById(R.id.p_budget_goal)).setText(Integer.toString(calculateRecommendation()));
	}
	
	/**calculate the recommended budget based on profile data**/
	private int calculateRecommendation(){
		float height = mHeight;
		//convert to cm
		if(unitInches){
			height = height * 2.45f;
		}
		
		float weight = mWeight;
		//convert to kg
		if(unitPounds){
			weight = weight / 2.2f;
		}
		
		float intake;
		//females
		if(mGender){
			intake = (9.36f*weight + 7.26f*height);
			//multiply by activity multiplier
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
		//males
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
		
		//account for how much weight they wanna gain/lose
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
	
	/**sets the master budget to the current recommended budget**/
	public void updateMasterBudget(View view){
        SharedPreferences settings = getSharedPreferences(DailyBudgetTracker.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        TextView mUpdateBox = (TextView)findViewById(R.id.p_budget_goal);

    	int value = Integer.parseInt(mUpdateBox.getText().toString());
    	
    	//update the runningBudget to be in line with the master
        int budget = settings.getInt(DailyBudgetTracker.BUDGET, 2000);
        int runningBudget = settings.getInt(DailyBudgetTracker.RUNNING_BUDGET, 2000);
    	int diff = value - budget;
    	budget = value;
    	runningBudget = runningBudget + diff;   
		
        editor.putInt(DailyBudgetTracker.RUNNING_BUDGET, runningBudget);
        editor.putInt(DailyBudgetTracker.BUDGET, budget);
        editor.commit();
        
        //display toast to confirm the update
		CharSequence text = "Your budget has been updated!";
		Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
		toast.show();
	}
	
    public void openSettings(View view) {
    	Intent intent = new Intent(this, Settings.class);
    	startActivity(intent);
    }
	
	public void goBack(View view){
		finish();
	}
}
