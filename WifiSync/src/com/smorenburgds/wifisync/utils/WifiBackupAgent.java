package com.smorenburgds.wifisync.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

import com.smorenburgds.wifisync.dao.Wifi;

public class WifiBackupAgent {
	private final static String WIFI_BLOCK_START = "network={";
	private final static String WIFI_BLOCK_END = "}";

	public WifiBackupAgent() {
	}

	public List<Wifi> parseWpa_supplicantFile(String fileContent) {

		// Log.i(getClass().getName(), fileContent);

		String[] splitedNetworks = fileContent.split("\n\n"), splitedNetworksRaw = fileContent
				.split("network=");

		List<Wifi> wifilist = new LinkedList<Wifi>();
		// string.replaceAll("psk=", "").replaceAll("[\"]"

		String actualPassword = "";
		String actualSSID = "";
		Log.i("OLE", Arrays.toString(splitedNetworksRaw));

		for (int i = 0; i < splitedNetworks.length; i++) {
			String block = splitedNetworks[i].trim();

			if (block.startsWith(WIFI_BLOCK_START)
					&& block.endsWith(WIFI_BLOCK_END)) {

				actualSSID = "";

				String blockLines[] = block.split("\n");

				for (int j = 0; j < blockLines.length; j++) {
					String line = blockLines[j].trim();

					if (line.startsWith("ssid=")) {
						actualSSID = line.replace("ssid=", "").replaceAll("\"",
								"");

					} else if (line.startsWith("key_mgmt=NONE")
							&& !block.contains("psk=")) {

						actualPassword = "Open Wifi Network =)";

					} else if (line.startsWith("psk=")) {

						actualPassword = line.replace("psk=", "").replaceAll(
								"\"", "");

					} else if (line.startsWith("wep_key0=")) {

						actualPassword = line.replace("psk=", "").replaceAll(
								"\"", "");

					} else if (line.startsWith("wep_key1=")) {

					} else if (line.startsWith("wep_key2=")) {

					} else if (line.startsWith("wep_key3=")) {

					}else if (line.startsWith("identity=")) {

						actualPassword = "User: "+line.replace("identity=", "")
								.replaceAll("\"", "");

					}  else if (line.startsWith("password=")) {

						actualPassword += "\nPassword: "+line.replace("password=", "")
								.replaceAll("\"", "");

					} else if (actualSSID != "") {
						wifilist.add(new Wifi(null, actualSSID, actualPassword,
								block.replaceAll("network=", "")));
					}

				}
			}

			// if (string.contains("ssid=")) {
			// actualSSID = string.replaceAll("ssid=", "")
			// .replaceAll("\"", "").trim();
			// // Log.i(getClass().getName(), actualSSID);
			// }
			// if (string.contains("key_mgmt=NONE")) {
			// if (string.contains("psk=")) {
			// actualPassword = string.replaceAll("psk=", "")
			// .replaceAll("\"", "").trim();
			// } else {
			// actualPassword = "Open WiFi Network";
			// }
			// }
			// if (string.contains("psk=")) {
			// actualPassword = string.replaceAll("psk=", "")
			// .replaceAll("\"", "").trim();
			// } else if (string.contains("key_mgmt=WPA-EAP")) {
			// actualPassword = "Radius Auth";
			// }

			// if (wifiToAdd.getPassword().isEmpty()) {
			// wifilist.add(wifiToAdd);
			// }
		}

		return wifilist;
	}
}
