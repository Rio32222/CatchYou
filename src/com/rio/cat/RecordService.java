package com.rio.cat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.View.OnCreateContextMenuListener;
import android.widget.Toast;

public class RecordService extends Service {

	public static final int FLAG_START = 1;
	public static final int FLAG_CHANGED = 2;
	
	final long DAYTIME = 24*60*60;
	
	private boolean isServiceAlive = false;	
	ArrayList< PInfo > mRecordAppList = null;
	CheckBackwardRunnable mCheckRunnable = null;
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		//注册监听拨号盘
		
		//注册监听器，监听activity的动作
		MyReceiver myReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Catch.LISTENER_ACTION);
		registerReceiver(myReceiver, filter);
		
		
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		
		if(intent == null){
			return 0;
		}
		
		LogC.d("RecordService start");
		
		if( !isServiceAlive ){
			mRecordAppList = intent.getParcelableArrayListExtra("inital_record_apps");
			
			for(PInfo info: mRecordAppList){  // for debug
				LogC.d(info.pName);
			}
			
			if (mRecordAppList == null){
				LogC.d("mRecordAppList is empty");
			}
			
			mCheckRunnable = new CheckBackwardRunnable();
			if( mCheckRunnable != null){
				new Thread( mCheckRunnable).start();
			}
			
		}
		
		isServiceAlive = true;
		return START_STICKY;
	}
	
	private synchronized ArrayList< PInfo > getRecordAppList(){
		return mRecordAppList;
	}
	
	private synchronized void setRecordAppList(ArrayList< PInfo > apps){
		mRecordAppList = apps;
	}
	
	private synchronized void addAppItem(PInfo pInfo){
		for( PInfo packInfo: mRecordAppList){
			if( packInfo.equals(pInfo) ){
				return;
			}
		}
		
		mRecordAppList.add(pInfo);
	}
	

	private synchronized ArrayList< PInfo > delAppItem(PInfo pInfo){
		for( PInfo packInfo: mRecordAppList){
			if( packInfo.equals(pInfo) ){
				mRecordAppList.remove(packInfo);
				delDataFromDB(pInfo);
				LogC.d(packInfo.pName + " deleted");
				break;
			}
		}
		return null;
	}
	
	public PInfo checkWhetherInRecordApps(String packageName){

		ArrayList< PInfo > tempAppList = getRecordAppList();
		
		if(tempAppList == null){
			return null;
		}
		
		for( PInfo pInfo: tempAppList ){
			if( packageName.equals(pInfo.pName) ){
				return pInfo;
			}
		}
		return null;
	}
	
	//记录切换前后app的时间
	public void recordChangedTime(PInfo pInfo, int flag){
		long sysTime = 0;
		try{
			sysTime = System.currentTimeMillis()/1000; 
		}catch(Exception e){
			e.printStackTrace();
		}
		
		LogC.v(String.valueOf(sysTime) + " " + String.valueOf(flag));
		
		insertDataToDB(pInfo, flag, sysTime);
		
	}
	
	public void delDataFromDB(PInfo pInfo){
		CatchStoreDataDBHelper dbHelper = CatchStoreDataDBHelper.getInstance(this);
		if( dbHelper == null ){
			LogC.e("DB is not created, delete data error");
			return;
		}
		
		if ( !dbHelper.deleteData(pInfo) ){
			LogC.e("From data error: " + pInfo.pName + " " + pInfo.appName);
		}
	}
	
	@SuppressLint("SimpleDateFormat") 
	private void insertDataToDB(PInfo pInfo, int flag, long sysTime){
		CatchStoreDataDBHelper dbHelper = CatchStoreDataDBHelper.getInstance(this);
		if( dbHelper == null ){
			LogC.e("DB is not created, insert data error");
			return;
		}
		
		ContentValues keyValues = new ContentValues();
		keyValues.put(CatchStoreDataDBHelper.DB_PACKAGE_KEY, pInfo.pName);
		keyValues.put(CatchStoreDataDBHelper.DB_APPNAME_KEY, pInfo.appName);
		keyValues.put(CatchStoreDataDBHelper.DB_FLAG_KEY, flag);
		keyValues.put(CatchStoreDataDBHelper.DB_TIMESTAMP_KEY, sysTime);
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-hh-mm");
		Date curDate = new Date(sysTime*1000);
		String date = formatter.format(curDate);
		
		keyValues.put(CatchStoreDataDBHelper.DB_DATE_KEY, date);
		dbHelper.insertData(keyValues);
	}
	
	//根据app名得到PInfo的信息
	private PInfo getPInfoBasePName(String pName){
		ArrayList< PInfo > tempAppList = getRecordAppList();
		
		if(tempAppList == null){
			return null;
		}
		
		for( PInfo pInfo: tempAppList ){
			if( pName.equals(pInfo.pName) ){
				return pInfo;
			}
		}
		return null;
		
	}
	
	public void updateRecordApps(){
		ArrayList< PInfo > recordAppsFromDB = getRecordAppsFromDB();
		ArrayList< PInfo > recordingAppList = getRecordAppList();
		
		for(PInfo pInfoDB: recordAppsFromDB){
			for(PInfo pInfoRecord: recordingAppList){
				if( pInfoRecord.equals(pInfoDB) ){
					recordingAppList.remove(pInfoRecord);
					break;
				}
			}
			
		}
		
	}
	
	private ArrayList< PInfo > getRecordAppsFromDB(){
		ArrayList< PInfo > initApps = new ArrayList< PInfo >();
	
		CatchStoreAppDBHelper storeDBHelper = CatchStoreAppDBHelper.getInstance(this);
		if( storeDBHelper == null){
			return null;
		}
	
		//query for the databases to get the initapps;
		//store first
		Cursor cursor = storeDBHelper.getStoreDBCursor();
		int index = 0;
		if( cursor != null && !cursor.isClosed() ){
			while( cursor.moveToNext() ){
	
				PInfo pInfo = new PInfo();
				pInfo.choosed = true;
				
				index = cursor.getColumnIndex(CatchStoreAppDBHelper.DB_APPNAME_KEY);
				pInfo.appName = cursor.getString(index);
	
				index = cursor.getColumnIndex(CatchStoreAppDBHelper.DB_PACKAGE_KEY);
				pInfo.pName = cursor.getString(index);
	
				index = cursor.getColumnIndex(CatchStoreAppDBHelper.DB_VERSIONNAME_KEY);
				pInfo.versionName = cursor.getString(index);
	
				index = cursor.getColumnIndex(CatchStoreAppDBHelper.DB_VERSIONCODE_KEY);
				pInfo.versionCode = cursor.getInt(index);
					
				initApps.add(pInfo);
			}
			cursor.close();
	}

	return initApps;
	}
	protected class MyReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			boolean action = intent.getBooleanExtra(Catch.ACTION_ADD_DEL_CHECKAPP, false);
			if( action ){
				updateRecordApps();
			}
		}
	}
	
	class CheckBackwardRunnable implements Runnable{

		private boolean isRunning = false;
		private boolean stop = false;
		private long sleepTime = 2;
		private String currentPackageName = "";
		
		ActivityManager mManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE); 
		
		public void setStop(boolean stop){
			this.stop = stop;
		}
		
		public void setSleepTime(long sleepTime){
			this.sleepTime = sleepTime;
		}
		
		public long getSleepTime(){
			return this.sleepTime;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			if(isRunning){
				return;
			}
			
			isRunning = true;
			while(!stop){
				/**获取当前正在运行的任务栈列表，越是靠近当前运行的任务栈会被排在第一位**/
				List<RunningTaskInfo> runningTasks = mManager.getRunningTasks(1);
				/**获取当前最顶端的任务栈，即前台任务栈**/
				RunningTaskInfo runningTaskInfo = runningTasks.get(0);
				/**获取当前任务栈的最顶端Activity**/
				ComponentName topActivity = runningTaskInfo.topActivity;
				/**获取应用的包名**/
				String runningPackageName = topActivity.getPackageName();
				
				if( runningPackageName.equals(currentPackageName) ){
					SystemClock.sleep(sleepTime*1000);
					continue;
				}else{
					//LogC.v(runningPackageName);
					//LogC.v(currentPackageName);
					//如果上次运行的app和这次运行的app时间不一致，则检查两个app是否需要被记录时间
					PInfo pInfo = checkWhetherInRecordApps(currentPackageName);
					if ( pInfo != null ){
						LogC.e("record exit " + pInfo.pName);
						recordChangedTime(pInfo, FLAG_CHANGED);
					}
					
					pInfo = checkWhetherInRecordApps(runningPackageName);
					if ( pInfo != null ){
						LogC.e("record enter " + pInfo.pName);
						recordChangedTime(pInfo, FLAG_START);
					}
				}
				
				currentPackageName = runningPackageName;
				
				//设置每次循环的时间间隔
				SystemClock.sleep(sleepTime*1000);
			}
			
			isRunning = false;
		}
	}
	
}
