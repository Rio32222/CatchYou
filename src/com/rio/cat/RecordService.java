package com.rio.cat;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.View.OnCreateContextMenuListener;
import android.widget.Toast;

public class RecordService extends Service {

	final long DAYTIME = 24*60*60;
	
	private boolean isServiceAlive = false;
	
	ArrayList< PInfo > mRecordAppList = null;
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		//ע�����������
		
		//ע�������������activity�Ķ���
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		
		if( !isServiceAlive ){
			mRecordAppList = intent.getParcelableArrayListExtra("inital_record_apps");
			
			if (mRecordAppList == null){
				LogC.d("mRecordAppList is empty");
			}
			
			Thread t = new Thread(new CheckBackwardRunnable());
			t.start();
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
	
	public boolean checkWhetherRecord(String appName){
		if( mRecordAppList == null ){
			return false;
		}
		
		ArrayList< PInfo > tempAppList = getRecordAppList();
		
		for( PInfo pInfo: tempAppList ){
			if( appName.equals(pInfo.appName) ){
				return true;
			}
		}
		return false;
	}
	
	public void recordChangedTime(String prevApp, String currentApp){
		long time = System.currentTimeMillis()/1000; 
		
		Toast.makeText(this, String.valueOf(time), Toast.LENGTH_LONG).show();
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
				/**��ȡ��ǰ�������е�����ջ�б�Խ�ǿ�����ǰ���е�����ջ�ᱻ���ڵ�һλ**/
				List<RunningTaskInfo> runningTasks = mManager.getRunningTasks(1);
				/**��ȡ��ǰ��˵�����ջ����ǰ̨����ջ**/
				RunningTaskInfo runningTaskInfo = runningTasks.get(0);
				/**��ȡ��ǰ����ջ�����Activity**/
				ComponentName topActivity = runningTaskInfo.topActivity;
				/**��ȡӦ�õİ���**/
				String runningPackageName = topActivity.getPackageName();
				
				if( checkWhetherRecord(runningPackageName) ){
					if( runningPackageName.equals(currentPackageName) ){
						//һֱ��ʹ�ô�app
					}else{
						//�л���app
						recordChangedTime(currentPackageName, runningPackageName);
					}
				}

				currentPackageName = runningPackageName;
				
				//����ÿ��ѭ����ʱ����
				SystemClock.sleep(sleepTime*1000);
			}
			
			isRunning = false;
		}
	}
	
}
