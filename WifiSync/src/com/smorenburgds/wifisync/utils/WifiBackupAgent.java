package com.smorenburgds.wifisync.utils;

import java.util.List;

import android.util.Log;

import com.smorenburgds.wifisync.dao.Wifi;

public class WifiBackupAgent {

	public WifiBackupAgent() {
		// TODO Auto-generated constructor stub
	}

	public List<Wifi> parseWpa_supplicantFile(String fileContent) {

		String splitedNetworks = fileContent.replaceAll("ssid=", "");
		
		Log.i("WIFI", splitedNetworks);
		
		

		return null;
	}

}
