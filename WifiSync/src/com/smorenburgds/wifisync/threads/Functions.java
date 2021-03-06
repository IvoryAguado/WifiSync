package com.smorenburgds.wifisync.threads;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.gesture.GestureLibraries;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.smorenburgds.wifisync.MainActivity;
import com.smorenburgds.wifisync.WifiSyncApplication;
import com.smorenburgds.wifisync.dao.DaoMaster;
import com.smorenburgds.wifisync.dao.DaoMaster.DevOpenHelper;
import com.smorenburgds.wifisync.dao.DaoSession;
import com.smorenburgds.wifisync.dao.Wifi;
import com.smorenburgds.wifisync.dao.WifiDao;
import com.smorenburgds.wifisync.misc.WifiAdapter;
import com.smorenburgds.wifisync.utils.WifiBackupAgent;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;

public class Functions {

	private static final String FILE_WIFI_SUPPLICANT = "/data/misc/wifi/wpa_supplicant.conf";
	// private static final String FILE_WIFI_SUPPLICANT_TEMPLATE =
	// "/system/etc/wifi/wpa_supplicant.conf";
	public static final String DIR_WIFISYNC_TEMP = "/storage/ext_sd/";
	public static final String FILE_WIFI_SUPPLICANT_TEMP = "/storage/ext_sd/wpa_supplicant.conf";

	// GreenDao DB
	private DaoMaster daoMaster;
	private DaoSession daoSession;
	private WifiDao wifidao;
	private SQLiteDatabase db;
	private boolean isDaoDbOn = false;
	public  List<Wifi> listFromFile = new LinkedList<Wifi>();

	public Functions() {
		Parse.initialize(WifiSyncApplication.getAppContext(),
				"UUsEZlDHou6NijQcIG5BE5RZ7RzPNnOuY9QfHNIo",
				"WXN5pug3ilcxCWJ7Bfj3Kq2lJSvnOyOuk73Iyd9E");
		daoGenStart();
	}

	private String createWPAWiFi(String SSID, String password) {
		return "{ \n	ssid=\"" + SSID + "\"\n psk=\"" + password
				+ "\"\n  key_mgmt=WPA-PSK\n }";
	}

	private String createWEPWiFi(String SSID, String password) {
		return "{ \n	ssid=\"" + SSID + "\"\n psk=\"" + password
				+ "\"\n  key_mgmt=WPA-PSK\n }";
	}

	// Starting GreenDaoDB
	private void daoGenStart() {

		if (!this.isDaoDbOn) {
			DevOpenHelper helper = new DaoMaster.DevOpenHelper(
					WifiSyncApplication.getAppContext(), "wifis-db", null);
			db = helper.getWritableDatabase();
			daoMaster = new DaoMaster(db);
			daoSession = daoMaster.newSession();
			setWifidao(daoSession.getWifiDao());
			isDaoDbOn = true;
		}
	}

	

	private void populateWifiListView() {
		MainActivity.wifiListView.setAdapter(new WifiAdapter(MainActivity
				.getActivitymain(), getWifidao().loadAll()));
	}

	public class syncParseAndDao extends AsyncTask<Void, Integer, List<Wifi>> {

		private List<Wifi> getAllFromParse() {
			List<Wifi> wifiList = new LinkedList<Wifi>();
			ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Wifis");

			try {
				for (ParseObject parseObject : query.find()) {
					wifiList.add(new Wifi(parseObject.getString("SSID"),
							parseObject.getString("Password"), parseObject
									.getString("rawData")));
				}
			} catch (ParseException e) {
				return wifiList;
			}
			return wifiList;
		}

		private List<Wifi> setAllToParse(List<Wifi> list) {
			List<ParseObject> toStoreInB = new LinkedList<ParseObject>();
			List<ParseObject> toStoreInBBac = new LinkedList<ParseObject>();
			ParseObject wifiEntry = null;
			ParseObject wifiEntryBackup = null;

			for (Wifi wifi : list) {
				wifiEntryBackup = new ParseObject("WifiBackUp");
				wifiEntry = new ParseObject("Wifis");
				wifiEntryBackup.put("SSID", wifi.getName());
				wifiEntryBackup.put("Password", wifi.getPassword());
				wifiEntryBackup.put("rawData", wifi.getRawData());
				wifiEntry.put("SSID", wifi.getName());
				wifiEntry.put("Password", wifi.getPassword());
				wifiEntry.put("rawData", wifi.getRawData());
				toStoreInBBac.add(wifiEntryBackup);
				toStoreInB.add(wifiEntry);
			}
			try {
				ParseObject.saveAll(toStoreInB);
				ParseObject.saveAll(toStoreInBBac);
				return getAllFromParse();
			} catch (ParseException e) {
			}
			return new LinkedList<Wifi>();
		}

		@Override
		protected List<Wifi> doInBackground(Void... params) {

			List<Wifi> parseDb = getAllFromParse();
			ArrayList<Wifi> daoDb = new ArrayList<Wifi>(getWifidao().loadAll());

			while (daoDb.size() != wifidao.loadAll().size()) {
			}

			while (!wifidao.loadAll().isEmpty()) {
				wifidao.deleteAll();
			}

			Map<String, Wifi> newSyncedList = new HashMap<String, Wifi>();

			for (Wifi wifiDao : daoDb) {
				newSyncedList.put(wifiDao.getRawData(), wifiDao);
			}
			for (Wifi wifiParse : parseDb) {
				newSyncedList.put(wifiParse.getRawData(), wifiParse);
			}

			Log.i("SYNC FINAL LIST", newSyncedList.toString());

			Iterator<Entry<String, Wifi>> newSyncedListIterable = newSyncedList
					.entrySet().iterator();

			while (newSyncedListIterable.hasNext()) {
				wifidao.insert(newSyncedListIterable.next().getValue());
			}

			// daoDb.removeAll(parseDb);
			// List<Integer> indexToDelete = new LinkedList<Integer>();
			//
			// for (int i = 0; i < daoDb.size(); i++) {
			// for (int j = 0; j < parseDb.size(); j++) {
			// if (daoDb.get(i).getRawData()
			// .equalsIgnoreCase(parseDb.get(j).getRawData())) {
			// indexToDelete.add(i);
			//
			// }
			// }
			// }
			// int sizetoloop = indexToDelete.size();
			// for (int i = 0; i < sizetoloop+1; i++) {
			// daoDb.remove(indexToDelete.get(i).intValue());
			// sizetoloop--;
			// }

			return setAllToParse(wifidao.loadAll());
		}

		@Override
		protected void onPostExecute(List<Wifi> result) {
			populateWifiListView();
			MainActivity.sync.setEnabled(true);
			MainActivity.sync.setIcon(android.R.drawable.stat_notify_sync);
			super.onPostExecute(result);
		}
	}

	public class readFromPhoneWifi extends AsyncTask<Void, Integer, Void> {
		private List<Wifi> readFromPhoneWifis() {
			Command command = new Command(0, "cat " + FILE_WIFI_SUPPLICANT) {

				private WifiBackupAgent wifiBA = new WifiBackupAgent();
				private String wpa_supplicantString;

				@Override
				public void commandCompleted(int arg0, int arg1) {

					listFromFile = wifiBA.parseWpa_supplicantFile(wpa_supplicantString);

//					for (Wifi wifi : listFromFile) {
//						getWifidao().insert(wifi);
//					}
					Log.i("LOGREADWPA", wpa_supplicantString);
					Log.i("LOGREADWPA", listFromFile.toString());

				}

				@Override
				public void commandOutput(int arg0, String arg1) {
					wpa_supplicantString += arg1 + "\n";

				}

				@Override
				public void commandTerminated(int arg0, String arg1) {
				}

			};
			try {
				while (RootTools.getShell(true).add(command).isExecuting())
					;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RootDeniedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				

			}

			return listFromFile;
		}
		@Override
		protected Void doInBackground(Void... params) {

			Map<String, Wifi> newSyncedList = new HashMap<String, Wifi>();

			for (Wifi wifi : wifidao.loadAll()) {
				newSyncedList.put(wifi.getRawData(), wifi);
			}
			while (!wifidao.loadAll().isEmpty()) {
				wifidao.deleteAll();
			}
			for (Wifi wifiFromWpa : readFromPhoneWifis()) {
				newSyncedList.put(wifiFromWpa.getRawData(), wifiFromWpa);
			}

			Iterator<Entry<String, Wifi>> newSyncedListIterable = newSyncedList
					.entrySet().iterator();

			while (newSyncedListIterable.hasNext()) {
				wifidao.insert(newSyncedListIterable.next().getValue());
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			MainActivity.populateWifiListView();
			super.onPostExecute(result);
		}
	}

	public class tryToConnectToThisWiFi extends AsyncTask<Wifi, Void, Void> {
		public void tryToConnect(Wifi wifi) {

			String networkSSID = wifi.getName();
			String networkPass = wifi.getPassword();

			WifiConfiguration conf = new WifiConfiguration();
			conf.SSID = "\"" + networkSSID + "\"";

			// conf.wepKeys[0] = "\"" + networkPass + "\"";
			// conf.wepTxKeyIndex = 0;
			// conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			// conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);

			conf.preSharedKey = "\"" + networkPass + "\"";

			conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

			WifiManager wifiManager = (WifiManager) WifiSyncApplication
					.getAppContext().getSystemService(Context.WIFI_SERVICE);
			wifiManager.addNetwork(conf);

			List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
			for (WifiConfiguration i : list) {
				if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
					wifiManager.disconnect();
					wifiManager.enableNetwork(i.networkId, true);
					wifiManager.reconnect();
					break;
				}
			}
			// UPD: In case of WEP, if your password is in hex, you do not need
			// to
			// surround it with quotes.

		}

		@Override
		protected void onPreExecute() {
			Toast.makeText(WifiSyncApplication.getAppContext(),
					"Trying to connect...", Toast.LENGTH_LONG).show();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Wifi... params) {

			for (Wifi wifi : params) {
				tryToConnect(wifi);
			}

			return null;
		}

	}

	public WifiDao getWifidao() {
		return wifidao;
	}

	public void setWifidao(WifiDao wifidao) {
		this.wifidao = wifidao;
	}
}
