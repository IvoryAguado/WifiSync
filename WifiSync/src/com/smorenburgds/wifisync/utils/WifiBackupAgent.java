package com.smorenburgds.wifisync.utils;

import java.util.LinkedList;
import java.util.List;

import com.smorenburgds.wifisync.dao.Wifi;

import android.util.Log;

public class WifiBackupAgent {

	public WifiBackupAgent() {
		// TODO Auto-generated constructor stub
	}

	public List<Wifi> parseWpa_supplicantFile(String fileContent) {

		// Log.i(getClass().getName(), fileContent);

		String[] splitedNetworks = fileContent.split("\n"), splitedNetworksRaw = fileContent.split("\n");

		List<Wifi> wifilist = new LinkedList<Wifi>();
		// string.replaceAll("psk=", "").replaceAll("[\"]"

		String actualPassword = "";
		String actualSSID = "";

		int i=0;
		
		for (String string : splitedNetworks) {

			if (string.contains("ssid=")) {
				actualSSID = string.replaceAll("ssid=", "")
						.replaceAll("\"", "");
				Log.i(getClass().getName(), actualSSID);
			}
			if (string.contains("psk=")) {
				actualPassword = string.replaceAll("psk=", "").replaceAll("\"",
						"");
				Log.i(getClass().getName(), actualPassword);

			}
			if (!actualSSID.isEmpty() && !actualPassword.isEmpty()) {
				wifilist.add(new Wifi(null, actualSSID, actualPassword, splitedNetworksRaw[i]));
				i++;
				actualPassword = "";
				actualSSID = "";
			}
			// if (wifiToAdd.getPassword().isEmpty()) {
			// wifilist.add(wifiToAdd);
			// }

		}

		return wifilist;
	}

}
