package com.rio.cat;

import android.util.*;

public class LogC {
	static final String Tag = "Catch";
	
	public static void d(String msg){
		Log.d(Tag, msg);
	}
	
	public static void e(String msg){
		Log.e(Tag, msg);
	}
	
	public static void i(String msg){
		Log.i(Tag, msg);
	}
	
	public static void w(String msg){
		Log.w(Tag, msg);
	}
	
	public static void v(String msg){
		Log.v(Tag, msg);
	}
}
