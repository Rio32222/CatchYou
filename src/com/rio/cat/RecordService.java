package com.rio.cat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
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
	
	private synchronized void addAppItem(PInfo info){
		
	}
	
	private synchronized ArrayList< PInfo > delAppItem(PInfo info){
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
	
	@SuppressLint("SimpleDateFormat") 
	private void insertDataToDB(PInfo pInfo, int flag, long sysTime){
		CatchStoreDataDBHelper dbHelper = CatchStoreDataDBHelper.getInstance(this);
		if( dbHelper == null ){
			LogC.e("DB is not created, insert data error");
			return;
		}
		
		ContentValues keyValues = new ContentValues();
		keyValues.put(CatchStoreDataDBHelper.TableKey[0], pInfo.pName);
		keyValues.put(CatchStoreDataDBHelper.TableKey[1], pInfo.appName);
		keyValues.put(CatchStoreDataDBHelper.TableKey[2], flag);
		keyValues.put(CatchStoreDataDBHelper.TableKey[3], sysTime);
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-hh-mm");
		Date curDate = new Date(sysTime*1000);
		String date = formatter.format(curDate);
		
		keyValues.put(CatchStoreDataDBHelper.TableKey[4], date);
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
	
//	public void storeToDB(String name, );
	
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
						recordChangedTime(pInfo, FLAG_CHANGED);
					}
					
					pInfo = checkWhetherInRecordApps(runningPackageName);
					if ( pInfo != null ){
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
