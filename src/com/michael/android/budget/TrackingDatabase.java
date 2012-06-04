package com.michael.android.budget;

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TrackingDatabase extends SQLiteOpenHelper {
	private static final String DB_NAME = "Track";
	private static final int DB_VERSION = 1;
	private static final String DATE_TIME = "DateTime";
	private static final String FOOD_ID = "FoodId";
	private static final String FOOD_NAME = "FoodName";
	private static final String FOOD_CAL = "FoodCal";
	private static final String DB_CREATE = "CREATE TABLE "	+ DB_NAME
					  		 + "( " + FOOD_ID + " INTEGER PRIMARY KEY, "
							 + FOOD_NAME + " TEXT, "
							 + FOOD_CAL + " INTEGER NOT NULL, "
							 + DATE_TIME + " INTEGER);";
	
	public TrackingDatabase (Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DB_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + DB_NAME);
		onCreate(db);
	}

	public void insertTuple (Food fd) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(FOOD_NAME, fd.getName());
		cv.put(FOOD_CAL, fd.getValue());
		cv.put(DATE_TIME, System.currentTimeMillis());
		db.insert(DB_NAME, null, cv);
		Log.i("insert", "name: " + fd.getName() + " cal: " + fd.getValue()); //TODO
		db.close();
	}
	
	public void updateTuple (int id, Food newFd) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(FOOD_NAME, newFd.getName());
		cv.put(FOOD_CAL, newFd.getValue());
		db.update(DB_NAME, cv, FOOD_ID+"=?", new String []{""+id});
		db.close();
	}
	
	public void deleteTuple (int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(DB_NAME, FOOD_ID+"=?", new String [] {""+id});
		Log.i("delete", "id: " + id); //TODO
		db.close();
	}
	
	public void clearDatabase () {
		long time = System.currentTimeMillis();
		Calendar yesterday = Calendar.getInstance();
		yesterday.setTimeInMillis(time - 86400000);
		yesterday.set(Calendar.HOUR_OF_DAY, 0);
		yesterday.set(Calendar.MINUTE, 0);
		yesterday.set(Calendar.SECOND,0);
		yesterday.set(Calendar.MILLISECOND,0);
		time = yesterday.getTimeInMillis();
		
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(DB_NAME, DATE_TIME+"<?", new String [] {""+time});
        db.close();
		
		/*db.execSQL("DROP TABLE IF EXISTS " + DB_NAME);
		onCreate(db);
		db.close();*/
	}
}