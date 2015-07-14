package com.rio.cat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

class CatchStoreDataDBHelper extends SQLiteOpenHelper{

	private static CatchStoreDataDBHelper instance = null;
	public static final int DB_VERSION = 1;
	
	public static final String DB_PACKAGE_KEY = "Package";
	public static final String DB_APPNAME_KEY = "App";
	public static final String DB_FLAG_KEY = "Flag";
	public static final String DB_TIMESTAMP_KEY = "TimeStamp";
	public static final String DB_DATE_KEY = "Date";
	
	
	public static CatchStoreDataDBHelper getInstance(Context context){
		if( instance == null){
			instance = new CatchStoreDataDBHelper(context, "xiong.db3", DB_VERSION);
		}
		
		return instance;
	}
	
	final String TABLE = "AppRecord";
	
	final String CREATE_TABLE_SQL = 
			"create table "+
			TABLE +
			"(Package varchar," +
			"App varchar," +
			"Flag integer," +
			"TimeStamp integer," +
			"Date varchar" +
			")";
	
	private CatchStoreDataDBHelper(Context context, String name, int version){
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

class CatchStoreAppDBHelper extends SQLiteOpenHelper{

	private static CatchStoreAppDBHelper instance = null;
	public static final int DB_VERSION = 1;
	
	public static final int DB_PACKAGE_COLUMN = 0;
	public static final int DB_APPNAME_COLUMN = 1;
	public static final int DB_VERSIONNAME_COLUMN = 2;
	public static final int DB_VERSIONCODE_COLUMN = 3;
	public static final int DB_ICON_COLUMN = 4;
	
	public static final String DB_PACKAGE_KEY = "Package";
	public static final String DB_APPNAME_KEY = "AppName";
	public static final String DB_VERSIONNAME_KEY = "VersionName";
	public static final String DB_VERSIONCODE_KEY = "VersionCode";
	public static final String DB_ICON_KEY = "Icon";
	
	public static CatchStoreAppDBHelper getInstance(Context context){
		if( instance == null){
			instance = new CatchStoreAppDBHelper(context, "xiong.db1", DB_VERSION);
		}
		
		return instance;
	}
	
	public final String TABLE = "AppSelected";
	public final String CREATE_TABLE_SQL = "create table " +
			TABLE+
			"(Package varchar," +
			"AppName varchar," +
			"VersionName varchar," +
			"VersionCode integer," +
			"Icon blob" +
			")";
	
	
	public CatchStoreAppDBHelper(Context context, String name, int version){
		super(context, name, null, version);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		try{
			db.execSQL(CREATE_TABLE_SQL);
		}catch(SQLException e){
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
	public void insertData(ContentValues keyValues){
		SQLiteDatabase db = getWritableDatabase();
		
		try{
			db.insert(TABLE, null, keyValues);
		}catch(SQLException e){
			e.printStackTrace();
		}
		
	}
	
	public Cursor getStoreDBCursor(){
		SQLiteDatabase db = getReadableDatabase();
		
		String sqlQuery = new String("select * from " + TABLE);
		try{
			Cursor cursor = db.rawQuery(sqlQuery, null);
			return cursor;
		}catch(SQLException e){
			e.printStackTrace();
			return null;
		}
	}
}
