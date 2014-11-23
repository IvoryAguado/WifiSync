package com.smorenburgds.wifisync.threads;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

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
import com.smorenburgds.wifisync.utils.AndroTerm;
import com.smorenburgds.wifisync.utils.WifiBackupAgent;

public class Functions {

	private static final String FILE_WIFI_SUPPLICANT = "/data/misc/wifi/wpa_supplicant.conf";
	private static final String FILE_WIFI_SUPPLICANT_TEMPLATE = "/system/etc/wifi/wpa_supplicant.conf";
	private static final String DIR_WIFISYNC_TEMP = "/storage/ext_sd/";
	private static final String FILE_WIFI_SUPPLICANT_TEMP = "/storage/ext_sd/wpa_supplicant.conf";

	// GreenDao DB
	private DaoMaster daoMaster;
	private DaoSession daoSession;
	private WifiDao wifidao;
	private SQLiteDatabase db;
	private boolean isDaoDbOn = false;
	private AndroTerm andT;
	private WifiBackupAgent wifiBA = new WifiBackupAgent();

	public Functions() {
		andT = new AndroTerm(true);
		// Parse DB initialitation
		Parse.initialize(WifiSyncApplication.getAppContext(),
				"UUsEZlDHou6NijQcIG5BE5RZ7RzPNnOuY9QfHNIo",
				"WXN5pug3ilcxCWJ7Bfj3Kq2lJSvnOyOuk73Iyd9E");
		daoGenStart();
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

	private List<Wifi> readFromPhoneWifis() {
		andT.commandExecutor(andT.new Command("cp " + FILE_WIFI_SUPPLICANT
				+ " " + DIR_WIFISYNC_TEMP));

		List<Wifi> listFromFile = wifiBA.parseWpa_supplicantFile(andT
				.convertStreamToString(FILE_WIFI_SUPPLICANT_TEMP));

		andT.commandExecutor(andT.new Command("sleep 5 && rm  "
				+ DIR_WIFISYNC_TEMP + "wpa_supplicant.conf"));
//		Log.i("LOGREADWPA", listFromFile.toString());
		return listFromFile;
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
					wifiList.add(new Wifi(null, parseObject.getString("SSID"),
							parseObject.getString("Password"), parseObject
									.getString("rawData")));
				}
			} catch (ParseException e) {
				return wifiList;
			}
			return wifiList;
		}

		private void setAllToParse(List<Wifi> list) {
			List<ParseObject> toStoreInB = new LinkedList<ParseObject>();
			ParseObject wifiEntry = null;
			for (Wifi wifi : list) {
				wifiEntry = new ParseObject("Wifis");
				wifiEntry.put("SSID", wifi.getName());
				wifiEntry.put("Password", wifi.getPassword());
				wifiEntry.put("rawData", wifi.getRawData());
				toStoreInB.add(wifiEntry);
			}
			try {
				ParseObject.saveAll(toStoreInB);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		protected List<Wifi> doInBackground(Void... params) {

			List<Wifi> parseDb = getAllFromParse();
			List<Wifi> daoDb = getWifidao().loadAll();

			HashMap<String, Wifi> newSyncedList = new HashMap<String, Wifi>();

			for (Wifi wifiDao : daoDb) {
				newSyncedList.put(wifiDao.getName(), wifiDao);
			}
			for (Wifi wifiParse : parseDb) {
				newSyncedList.put(wifiParse.getName(), wifiParse);
			}
			wifidao.deleteAll();
			Iterator<Entry<String, Wifi>> newSyncedListIterable = newSyncedList
					.entrySet().iterator();
			
			while (newSyncedListIterable.hasNext()) {
				wifidao.insert(newSyncedListIterable.next().getValue());
			}
			
			daoDb.removeAll(parseDb);
			setAllToParse(daoDb);
			return wifidao.loadAll();
		}

		@Override
		protected void onPostExecute(List<Wifi> result) {
			populateWifiListView();
			MainActivity.sync.setEnabled(true);
			super.onPostExecute(result);
		}
	}

	public class readFromPhoneWifis extends AsyncTask<Void, Integer, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			HashMap<String, Wifi> newSyncedList = new HashMap<String, Wifi>();
		
			for (Wifi wifi : wifidao.loadAll()) {
				newSyncedList.put(wifi.getName(),wifi);
			}
			for (Wifi wifi : readFromPhoneWifis()) {
				newSyncedList.put(wifi.getName(),wifi);
			}
			Iterator<Entry<String, Wifi>> newSyncedListIterable = newSyncedList
					.entrySet().iterator();
			wifidao.deleteAll();
			while (newSyncedListIterable.hasNext()) {
				wifidao.insert(newSyncedListIterable.next().getValue());
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			populateWifiListView();
			super.onPostExecute(result);
		}
	}

	public WifiDao getWifidao() {
		return wifidao;
	}

	public void setWifidao(WifiDao wifidao) {
		this.wifidao = wifidao;
	}
}
