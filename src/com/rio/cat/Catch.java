package com.rio.cat;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.DownloadManager.Query;
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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract.PinnedPositions;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class Catch extends Activity {

	public final String[] DefaultApps= {"Calendar", "Dialer"};
	
	List< PInfo > mInstalledApps = null;
	Map< String, Object > item;
	BrowseAppInfoAdapter mListAdapter = null;
	Button addButton = null;
	Boolean mLoadFinished = false;
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catch);
        
        //should add initial code
        init();
        
        ListView rootList = (ListView) findViewById(R.id.root_list);
        rootList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				
			}
        	
		});
        
        rootList.setAdapter(mListAdapter);
        
        addButton = (Button)findViewById(R.id.add_button);
        addButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//判断是否已经加载完
				if(mLoadFinished){
					
				}
			}
		});
        
        
    }
    
    private void init(){
    	
    	//should add at the first time when the user install this app
    	 mInstalledApps = getInstalledApps(false);
         

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
			view = inflater.inflate(R.layout.list_item, null);
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
