package com.rio.cat;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class CatchDBHelper extends SQLiteOpenHelper{

	private static CatchDBHelper instance = null;
	public static final int DB_VERSION = 1;
	
	public static CatchDBHelper getInstance(Context context){
		if( instance == null){
			instance = new CatchDBHelper(context, "xiong.db3", DB_VERSION);
		}
		
		return instance;
	}
	
	final String TABLE = "AppRecord";
	
	/*
	final String CREATE_TABLE_SQL = 
			"create table "+
			TABLE +
			"(_id integer primary," +
			"Package char," +
			"App char(255)," +
			"Flag integer(1) NOT NULL," +
			"Time int," +
			"OrderDate date DEFAULT GETDATE()" +
			")";
	*/
	
	final String CREATE_TABLE_SQL = 
			"create table AppRecord(Package varchar," +
			"App varchar," +
			"Flag int," +
			"Time int" +
			")";
	
	
	public static final String[] TableKey = {
			"Package",
			"App",
			"Flag",
			"Time"
	};
	
	private CatchDBHelper(Context context, String name, int version){
		super(context, name, null, version);
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		LogC.d(db.getPath());
		try{
			db.execSQL(CREATE_TABLE_SQL);
		}catch(SQLException e){
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	};
	
	//插入每行的数据
	public boolean insertData(ContentValues keyValues){
		SQLiteDatabase db = getWritableDatabase();
		
		try{
			db.insert(TABLE, null, keyValues);
		}catch(SQLiteException e){
			LogC.e("insert data error " + keyValues.toString());
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	
}
