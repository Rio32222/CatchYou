package com.rio.cat;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Catch extends Activity {

	public static final int HIDE_DUARATION = 600;
	public final String[] DefaultApps= {"Calendar", "Dialer"};
	
	ArrayList< PInfo > mInstalledApps = null;
	Map< String, Object > item;
	BrowseAppInfoAdapter mListAdapter = null;
	Button mAddButton = null;
	Button mOkButton = null;
	Boolean mLoadFinished = false;
	
	ListView mRootList = null;
	ListView mInstalledList = null;
	LinearLayout mChoosePage = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catch);
        
        LoadInstalledApps loadApps = new LoadInstalledApps();
        loadApps.execute("xiong");
        
        //should add initial code
        init();
        
        mRootList = (ListView) findViewById(R.id.root_list);
        mRootList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				
			}
        	
		});
        
        mRootList.setAdapter(mListAdapter);
        
        mChoosePage = (LinearLayout) findViewById(R.id.install_choose_page);
        mInstalledList = (ListView) findViewById(R.id.install_to_choose_list);
        
        mAddButton = (Button)findViewById(R.id.add_button);
        mAddButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//判断是否已经加载完
				if(mLoadFinished){
					showChoosePage();
				}
			}
		});
     
        mOkButton= (Button)findViewById(R.id.choose_ok);
        
        mOkButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showCheckPage();
			}
		});
        
        LogC.d("activity load finished");
        
    }
    
    private Point getWidthHeight(){
    	Point point = new Point();
    	
    	DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        
        point.x = dm.widthPixels;
        point.y = dm.heightPixels;
        
        LogC.d(point.toString());
    	return point;
    }
    
    private void init(){
    	
    	//should add at the first time when the user install this app
    	 mInstalledApps = getInstalledApps();
         

         ArrayList< PInfo > initRecordApps = new ArrayList<PInfo>();
         
         //This is for a test --start
         //initRecordApps.add(mInstalledApps.get(3));
         //initRecordApps.add(mInstalledApps.get(4));
         //This is for a test --end
         
         initRecordApps = getInitRecordApps();
         
        // LogC.d(mInstalledApps.get(3).pName);
         
         mListAdapter = new BrowseAppInfoAdapter(this, initRecordApps);
         
         Intent intent = new Intent(Catch.this, RecordService.class);
         intent.putParcelableArrayListExtra("inital_record_apps", initRecordApps);
         if( intent != null){
         	startService(intent);
         }
    }
        
    public void insertData(PInfo pInfo){
    	CatchStoreAppDBHelper storeDBHelper = CatchStoreAppDBHelper.getInstance(this);
    	if( storeDBHelper == null){
    		return;
    	}
    	
    	ContentValues keyValues = new ContentValues();
    	keyValues.put(CatchStoreAppDBHelper.DB_PACKAGE_KEY, pInfo.pName);
    	keyValues.put(CatchStoreAppDBHelper.DB_APPNAME_KEY, pInfo.appName);
    	keyValues.put(CatchStoreAppDBHelper.DB_VERSIONNAME_KEY, pInfo.versionName);
    	keyValues.put(CatchStoreAppDBHelper.DB_VERSIONCODE_KEY, pInfo.versionCode);
    	
    	if(pInfo.getBitmap() != null){
    		ByteArrayOutputStream os = new ByteArrayOutputStream();
    		if( pInfo.getBitmap().compress(CompressFormat.PNG, 100, os) ){
    			keyValues.put(CatchStoreAppDBHelper.DB_ICON_KEY, os.toByteArray());
    		}
    	}else{
    		LogC.e(pInfo.appName +"'s icon is null");
    	}
    	
    	storeDBHelper.insertData(keyValues);
    	
    }
    
    
    public ArrayList< PInfo > getInitRecordApps(){
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
    			index = cursor.getColumnIndex(CatchStoreAppDBHelper.DB_APPNAME_KEY);
    			pInfo.appName = cursor.getString(index);

    			index = cursor.getColumnIndex(CatchStoreAppDBHelper.DB_PACKAGE_KEY);
    			pInfo.pName = cursor.getString(index);
    			
    			index = cursor.getColumnIndex(CatchStoreAppDBHelper.DB_VERSIONNAME_KEY);
    			pInfo.versionName = cursor.getString(index);
    			
    			index = cursor.getColumnIndex(CatchStoreAppDBHelper.DB_VERSIONCODE_KEY);
    			pInfo.versionCode = cursor.getInt(index);
    			
    			index = cursor.getColumnIndex(CatchStoreAppDBHelper.DB_ICON_KEY);
    			byte [] iconByte = cursor.getBlob(index);
    			
    			pInfo.setBitmap( BitmapFactory.decodeByteArray(iconByte, 0, iconByte.length));
    			
    			initApps.add(pInfo);
    			
    			
    		}
    		cursor.close();
    	}
    	
    
    	return initApps;
    }
    
    private ArrayList< PInfo > getInstalledApps(){
    	int wait = 3;
    	while( !mLoadFinished && wait > 0){
    		try {
				Thread.sleep(100);
				wait = wait-1;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	if( mLoadFinished ){
    		return mInstalledApps;
    	}else{
    		return null;
    	}
    }
    
    private void showChoosePage(){
    	
    	ArrayList< PInfo > installedApps = getInstalledApps();
    	if( installedApps == null || installedApps.size() == 0 ){
    		return;
    	}
    	
    	ChooseAppAdapter chooseAdapter = new ChooseAppAdapter(this, installedApps);
    	mInstalledList.setAdapter(chooseAdapter);
    	
    	
    	Point point = getWidthHeight();
    	
    	TranslateAnimation transAni = new TranslateAnimation(0, 0, point.y, 0);//point.y/4);
    	transAni.setFillAfter(true);
    	transAni.setDuration(HIDE_DUARATION);
    	
    	mChoosePage.setVisibility(View.VISIBLE);
    	
    	mAddButton.setVisibility(View.GONE);
    	mAddButton.setEnabled(false);
    	
    	mChoosePage.startAnimation(transAni);
    	
    	mOkButton.setVisibility(View.VISIBLE);
    	mOkButton.setEnabled(true);
    }
    
    private void showCheckPage(){
    	mOkButton.setVisibility(View.GONE);
    	mOkButton.setEnabled(false);
    	
    	Point point = getWidthHeight();
    	TranslateAnimation transAni = new TranslateAnimation(0, 0,  0, point.y);
    	transAni.setFillAfter(true);
    	transAni.setDuration(HIDE_DUARATION);
    	
    	mChoosePage.startAnimation(transAni);
    	
    	mChoosePage.setVisibility(View.GONE);
    	
    	mAddButton.setVisibility(View.VISIBLE);
    	mAddButton.setEnabled(true);
    }
    
    public void addRecordApp(PInfo pInfo){
    	
    }
    
    
    private boolean checkWhetherInDefaultApps(String name){
    	for( String appname:DefaultApps){
    		if(appname.contains(name)){
    			return true;
    		}
    	}
    	return false;
    }
    
    private class LoadInstalledApps extends AsyncTask<String, Integer, ArrayList< PInfo >>{

		@Override
		protected ArrayList<PInfo> doInBackground(String... params) {
			// TODO Auto-generated method stub
			mInstalledApps = getInstalledApps(false);
			mLoadFinished = true;
			return mInstalledApps;
		}
    	
		@Override
		protected void onPostExecute(ArrayList< PInfo > installedApps){
			
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}
		
		private ArrayList< PInfo > getInstalledApps(boolean getSysPackages){
	    	ArrayList<PInfo> res = new ArrayList<PInfo>();
	    	
	    	List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
	    	
	    	for(PackageInfo pack:packages){
	    		if( !(getSysPackages) && pack.versionName == null){
	    			continue;
	    		}
	    		
	    		PInfo info = new PInfo();
	    		info.appName = (String) pack.applicationInfo.loadLabel(getPackageManager());
	    		//LogC.d(info.appName);
	    		info.versionName = pack.versionName;
	    		info.versionCode = pack.versionCode;
	    		info.pName = pack.packageName;
	    		Drawable icon = pack.applicationInfo.loadIcon(getPackageManager());
	    		info.setBitmap( ((BitmapDrawable)icon).getBitmap() );
	    		
	    		res.add(info);
	    		
	    	}
	    	return res;
	    }
    }
}

class PInfo implements Parcelable{
	public String appName="";
	public String pName="";
	public String versionName="";
	public int versionCode= -1;
	private Bitmap bitmapIcon;
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(appName);
		dest.writeString(pName);
		dest.writeString(versionName);
		dest.writeInt(versionCode);
		//dest.writeParcelable(bitmapIcon, PARCELABLE_WRITE_RETURN_VALUE);
		//dest.writeValue(icon);
	}
	
	public static final Parcelable.Creator< PInfo > CREATOR = new Parcelable.Creator< PInfo >(){
		
		public PInfo[] newArray(int size){
			return new PInfo[size];
		}

		@Override
		public PInfo createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new PInfo(source);
		}
	};
	
	public PInfo(Parcel in){
		appName = in.readString();
		pName = in.readString();
		versionName = in.readString();
		versionCode = in.readInt();
		//bitmapIcon = in.readParcelable( Bitmap.class.getClassLoader() );
	}
	
	public PInfo(String pName, String appName, String versionName, int versionCode, byte[] iconStream){
		this.pName = pName;
		this.appName = appName;
		this.versionName = versionName;
		this.versionCode = versionCode;
		
		this.bitmapIcon = BitmapFactory.decodeByteArray(iconStream, 0, iconStream.length);
	}
	
	public PInfo(){}
	
	//
	public Bitmap setBitmapIconFromDB(SQLiteDatabase db){
		
		String queryCmd = new String();
		try{
			db.execSQL(queryCmd);
		}catch(SQLiteException e){
			e.printStackTrace();
		}
			
		return this.bitmapIcon;
	}

	public Bitmap getBitmap(){
		return bitmapIcon;
	}
	
	public void setBitmap(Bitmap bitmap){
		bitmapIcon = bitmap;
	}
}

class BrowseAppInfoAdapter extends BaseAdapter{
	
	private List< PInfo > mListAppInfo = null;
	
	LayoutInflater inflater = null;
	
	public BrowseAppInfoAdapter(Context context, List< PInfo > apps){
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if( apps == null){
			mListAppInfo = new ArrayList<PInfo>();
		}else{
			mListAppInfo = apps;
		}
	}
	
	public void addItem(Object object){
		mListAppInfo.add((PInfo) object);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mListAppInfo.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mListAppInfo.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = null;
		ViewHolder holder = null;
		if ( convertView == null || convertView.getTag() == null){
			view = inflater.inflate(R.layout.choose_list_item, null);
			holder = new ViewHolder(view);
			view.setTag(holder);
		}else{
			view = convertView;
			holder = (ViewHolder) convertView.getTag();
		}
		
		PInfo info = (PInfo) getItem(position);
		if (holder == null){
			return null;
		}
		
		
		holder.appIcon.setImageBitmap(info.getBitmap());
		holder.appName.setText(info.appName);
		holder.appHint.setText(R.id.app_hint);
		holder.appGraph.setImageResource(R.drawable.graph);
		
		return view;
	}
	
	class ViewHolder{
		ImageView appIcon;
		TextView appName;
		TextView appHint;
		ImageView appGraph;
		
		public ViewHolder(View view){
			appIcon = (ImageView) view.findViewById(R.id.app_icon);
			appName = (TextView) view.findViewById(R.id.app_name);
			appHint = (TextView) view.findViewById(R.id.app_hint);
			appGraph = (ImageView) view.findViewById(R.id.app_graph);
		}
	}
}


class ChooseAppAdapter extends BaseAdapter{
	private List< PInfo > mListAppInfo = null;
	
	LayoutInflater inflater = null;
	
	public ChooseAppAdapter(Context context, List< PInfo > apps){
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if( apps == null ){
			mListAppInfo = new ArrayList<PInfo>();
		}else{
			mListAppInfo = apps;
		}
	}
	
	public void addItem(Object object){
		mListAppInfo.add( (PInfo)object );
	}

	public void deleteItem(int location){
		mListAppInfo.remove(location);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mListAppInfo.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mListAppInfo.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@SuppressLint("InflateParams") @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = null;
		ViewHolder holder = null;
		if( convertView == null || convertView.getTag() == null ){
			view = inflater.inflate(R.layout.installed_list_item, null);
			holder = new ViewHolder(view);
			view.setTag(holder);
		}else{
			view = convertView;
			holder = (ViewHolder)view.getTag();
		}
		
		if( holder == null ){
			return null;
		}
		
		PInfo info = (PInfo)getItem(position);
		
		if ( info != null ){
			holder.Icon.setImageBitmap(info.getBitmap());
			holder.Name.setText(info.appName);
			holder.Checked.setImageResource(R.drawable.choosed_icon);
		}
		
		return view;
	}
	
	class ViewHolder{
		
		ImageView Icon;
		TextView Name;
		ImageView Checked;
		
		public ViewHolder(View view){
			Icon = (ImageView)view.findViewById(R.id.install_icon);
			Name = (TextView)view.findViewById(R.id.install_name);
			Checked = (ImageView)view.findViewById(R.id.install_choose);
		}
	}
}