package com.michael.android.budget;


public class Food {
	private String myName;
	private int myValue;
	
	public Food (int value) {
		this (null, value);
	}
	
	public Food (String name, int value) {
		myName = name;
		myValue = value;
	}
	
	public String getName() {
		return myName;
	}
	
	public int getValue() {
		return myValue;
	}
	
	public void setName(String name) {
		myName = name;
	}
	
	public void setValue(int val) {
		myValue = val;
	}
}
