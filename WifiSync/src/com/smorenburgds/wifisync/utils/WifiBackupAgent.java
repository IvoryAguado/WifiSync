package com.smorenburgds.wifisync.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

import com.smorenburgds.wifisync.dao.Wifi;

public class WifiBackupAgent {

	public WifiBackupAgent() {
		// TODO Auto-generated constructor stub
	}

	public List<Wifi> parseWpa_supplicantFile(String fileContent) {

		// Log.i(getClass().getName(), fileContent);

		String[] splitedNetworks = fileContent.split("\n"), splitedNetworksRaw = fileContent
				.split("\nnetwork=");

		List<Wifi> wifilist = new LinkedList<Wifi>();
		// string.replaceAll("psk=", "").replaceAll("[\"]"

		String actualPassword = "";
		String actualSSID = "";
		Log.i("OLE", Arrays.toString(splitedNetworksRaw));

		int i = 0;

		for (String string : splitedNetworks) {

			if (string.contains("ssid=")) {
				actualSSID = string.replaceAll("ssid=", "")
						.replaceAll("\"", "").trim();
				// Log.i(getClass().getName(), actualSSID);
			}
			if (string.contains("psk=")) {
				actualPassword = string.replaceAll("psk=", "")
						.replaceAll("\"", "").trim();
				// Log.i(getClass().getName(), actualPassword);

			}
			if (!actualSSID.isEmpty() && !actualPassword.isEmpty()) {
				wifilist.add(new Wifi(null, actualSSID, actualPassword,
						splitedNetworksRaw[i-1]));
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
