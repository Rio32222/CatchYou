package com.rio.cat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class Catch extends Activity {

	public final String[] DefaultApps= {"Calendar", "Dialer"};
	
	List<Map< String, Object > > mListItems = null;
	List< PInfo > mInstalledApps = null;
	Map< String, Object > item;
	BrowseAppInfoAdapter mListAdapter = null;
	
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
        
        ArrayList< PInfo > initRecordApps = new ArrayList<PInfo>();
        
        //This is for a test --start
        initRecordApps.add(mInstalledApps.get(0));
        initRecordApps.add(mInstalledApps.get(1));
        //This is for a test --end
        
        
        Intent intent = new Intent(Catch.this, RecordService.class);
        intent.putParcelableArrayListExtra("inital_record_apps", initRecordApps);
        startService(intent);
    }
    
    private void init(){
    	
    	//should add at the first time when the user install this app
    	 mInstalledApps = getInstalledApps(false);
         mListItems = new ArrayList< Map< String, Object > >();
                
         mListAdapter = new BrowseAppInfoAdapter(this, mInstalledApps.subList(0, 5));
         
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
    		
    		info.pName = pack.packageName;
    		info.icon = pack.applicationInfo.loadIcon(getPackageManager());
    		
    		//LogC.d(info.icon.toString());
    		res.add(info);
    		
    	}
    	return res;
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
	public String versionCode="";
	public Drawable icon;
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
		dest.writeValue(icon);
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
	
	private PInfo(Parcel in){
		appName = in.readString();
		pName = in.readString();
		icon =  (Drawable) in.readValue(PInfo.class.getClassLoader());
	}
	
	public PInfo(){}
	
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
		holder.appIcon.setImageDrawable(info.icon);
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
