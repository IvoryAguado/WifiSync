package com.smorenburgds.wifisync;

import android.app.Application;
import android.content.Context;

public class WifiSyncApplication extends Application {

	private static Context context;
	 
    public void onCreate(){ 
        super.onCreate(); 
        WifiSyncApplication.context = getApplicationContext();
    } 
 
    public static Context getAppContext() {
        return WifiSyncApplication.context;
    } 
	
}
