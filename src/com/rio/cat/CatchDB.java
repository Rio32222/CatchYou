package com.rio.cat;

import android.database.sqlite.SQLiteDatabase;

public class CatchDB {
	final String PATH = "/mnt/db/temp.db3";
	
	private static CatchDB instance = null;
	
	SQLiteDatabase mDataBase = null;
	
	public static CatchDB getInstatnce(){
		if( instance == null){
			instance = new CatchDB();
		}
		return instance;
	}
	
	public boolean open(){
		mDataBase = SQLiteDatabase.openOrCreateDatabase(PATH, null);
		if ( mDataBase == null ){
			return false;
		}
		return true;
	}
	
	
	private CatchDB(){};
}
