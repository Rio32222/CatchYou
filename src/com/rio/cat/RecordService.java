package com.rio.cat;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.View.OnCreateContextMenuListener;

public class RecordService extends Service {

	final long DAYTIME = 24*60*60;
	
	List< PInfo > mRecordAppList = null;
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		//注册监听拨号盘
		
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean checkWhetherRecord(String appName){
		if( mRecordAppList == null ){
			return false;
		}
		
		for( PInfo pInfo: mRecordAppList ){
			if( appName.equals(pInfo.appName) ){
				return true;
			}
		}
		return false;
	}
	
	public void recordChangedTime(String prevApp, String currentApp){
		long time = System.currentTimeMillis()/1000; 
		
	}
	
	public void storeToDB(String name, );
	
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
				
				if( checkWhetherRecord(runningPackageName) ){
					if( runningPackageName.equals(currentPackageName) ){
						//一直在使用此app
					}else{
						//切换了app
						recordChangedTime(currentPackageName, runningPackageName);
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
