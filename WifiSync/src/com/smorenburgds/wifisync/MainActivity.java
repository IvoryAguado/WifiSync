package com.smorenburgds.wifisync;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.smorenburgds.wifisync.dao.DaoMaster;
import com.smorenburgds.wifisync.dao.DaoMaster.DevOpenHelper;
import com.smorenburgds.wifisync.dao.DaoSession;
import com.smorenburgds.wifisync.dao.Wifi;
import com.smorenburgds.wifisync.dao.WifiDao;
import com.smorenburgds.wifisync.misc.WifiAdapter;
import com.smorenburgds.wifisync.utils.AndroTerm;
import com.smorenburgds.wifisync.utils.WifiBackupAgent;

public class MainActivity extends Activity {

	private static final String FILE_WIFI_SUPPLICANT = "/data/misc/wifi/wpa_supplicant.conf";
//	private static final String FILE_WIFI_SUPPLICANT_TEMPLATE = "/system/etc/wifi/wpa_supplicant.conf";
	private static final String DIR_WIFISYNC_TEMP = "/storage/ext_sd/";
	private static final String FILE_WIFI_SUPPLICANT_TEMP = "/storage/ext_sd/wpa_supplicant.conf";

	private ListView wifiListView;

	private ClipboardManager clipboard;

	private DaoMaster daoMaster;
	private DaoSession daoSession;
	private WifiDao wifidao;

	WifiAdapter adapter = null;
	private SQLiteDatabase db;
	private AndroTerm andT;
	private WifiBackupAgent wifiBA = new WifiBackupAgent();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		andT = new AndroTerm(true);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Parse.initialize(this, "UUsEZlDHou6NijQcIG5BE5RZ7RzPNnOuY9QfHNIo",
				"WXN5pug3ilcxCWJ7Bfj3Kq2lJSvnOyOuk73Iyd9E");
		new UIRefresher().execute(UIRefresher.INITIALITATE_DBS);

		wifiListView = (ListView) findViewById(R.id.listWifiView);
		clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		registerForContextMenu(wifiListView);
		// wifiListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		new UIRefresher().execute(UIRefresher.POPULATE_WIFILISTVIEW);

	}

	private class UIRefresher extends AsyncTask<Integer, Void, Integer[]> {

		public static final int POPULATE_WIFILISTVIEW = 234;
		public static final int INITIALITATE_DBS = 236;
		public static final int SYNC_WIFI = 238;
		public static final int READ_WPA_SUPPLICANT = 300;
		public static final int CLEAR_WIFILISTVIEW = 304;

		@Override
		protected Integer[] doInBackground(Integer... options) {
			for (Integer option : options) {
				switch (option) {
				case SYNC_WIFI:
					syncWifis();
					break;

				}
			}
			return options;
		}

		@Override
		protected void onPostExecute(Integer[] result) {
			for (Integer option : result) {
				switch (option) {
				case POPULATE_WIFILISTVIEW:
					populateWifiListView();
					break;
				case INITIALITATE_DBS:
					daoGenStart();
					break;
				case READ_WPA_SUPPLICANT:
					readFromPhoneWifis();
					break;
				case CLEAR_WIFILISTVIEW:
					wifidao.deleteAll();
					populateWifiListView();
					break;
				}
			}
			super.onPostExecute(result);
		}

		private void daoGenStart() {
			DevOpenHelper helper = new DaoMaster.DevOpenHelper(
					MainActivity.this, "wifis-db", null);
			db = helper.getWritableDatabase();
			daoMaster = new DaoMaster(db);
			daoSession = daoMaster.newSession();
			wifidao = daoSession.getWifiDao();

		}

		private void populateWifiListView() {
			
			adapter = new WifiAdapter(MainActivity.this,
					R.layout.list_wifi_item, wifidao.loadAll());
			
			wifiListView.setAdapter(adapter);
		}

		private void syncWifis() {

			List<ParseObject> toStoreInB = new ArrayList<ParseObject>();
			ParseObject wifiEntry = null;
			List<Wifi> tmpDList = wifidao.loadAll();
			try {

				for (Wifi tmpWifi : tmpDList) {
					// Log.i("DELETE", wifi.getName());
					wifiEntry = new ParseObject("Wifis");
					wifiEntry.put("SSID", tmpWifi.getName());
					wifiEntry.put("Password", tmpWifi.getPassword());
					wifiEntry.put("rawData", tmpWifi.getRawData());
					toStoreInB.add(wifiEntry);
				}
				ParseObject.saveAll(toStoreInB);
				ParseLoadAll();

			} catch (Exception e) {

			} finally {

			}
		}

		private List<Wifi> ParseLoadAll() {

			List<Wifi> wifiList = new LinkedList<Wifi>();
			ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Wifis");

			try {
				for (ParseObject parseObject : query.find()) {
					wifidao.insert(new Wifi(null,
							parseObject.getString("SSID"), parseObject
									.getString("Password"), parseObject
									.getString("rawData")));
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				
			}

			return wifiList;
		}

		private void readFromPhoneWifis() {
			andT.commandExecutor(andT.new Command("cp " + FILE_WIFI_SUPPLICANT
					+ " " + DIR_WIFISYNC_TEMP));
			List<Wifi> listFromFile = wifiBA.parseWpa_supplicantFile(andT
					.convertStreamToString(FILE_WIFI_SUPPLICANT_TEMP));
			if (!listFromFile.isEmpty()) {
				wifidao.deleteAll();
				for (Wifi s : listFromFile) {
					wifidao.insert(s);
				}
				populateWifiListView();
				andT.commandExecutor(andT.new Command("rm  "
						+ DIR_WIFISYNC_TEMP + "wpa_supplicant.conf"));
			}
		}

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo pos = (AdapterContextMenuInfo) menuInfo;

		menu.setHeaderTitle("Wifi - Options "
				+ wifidao.loadByRowId(pos.position + 1).getName());
		getMenuInflater().inflate(R.menu.contextual_menu_main, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo pos = (AdapterContextMenuInfo) item
				.getMenuInfo();

		int id = item.getItemId();
		if (id == R.id.tryconnectwifi) {
			WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

			List<WifiConfiguration> configs = wifiManager
					.getConfiguredNetworks();

			for (WifiConfiguration wifiConfiguration : configs) {

				Toast.makeText(
						this,
						"Trying to connect "
								+ wifiConfiguration.wepKeys.toString(),
						Toast.LENGTH_SHORT).show();
				try {
					// if (wifiConfiguration.SSID.equals("\"" + "Sm" + "\"")) {
					// wc = wifiConfiguration;
					// break;
					// }
				} catch (Exception e) {
				}
			}
			return true;
		} else if (id == R.id.copyfullwifi) {
			ClipData clip = ClipData.newPlainText("label",
					wifidao.loadByRowId(pos.position + 1).getRawData());
			clipboard.setPrimaryClip(clip);
			Toast.makeText(this,
					wifidao.loadByRowId(pos.position + 1).getRawData(),
					Toast.LENGTH_LONG).show();
			return true;
		} else if (id == R.id.copyPass) {

			ClipData clip = ClipData.newPlainText("label",
					wifidao.loadByRowId(pos.position + 1).getPassword());
			clipboard.setPrimaryClip(clip);

			Toast.makeText(this,
					wifidao.loadByRowId(pos.position + 1).getPassword(),
					Toast.LENGTH_LONG).show();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onContextMenuClosed(Menu menu) {
		super.onContextMenuClosed(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.options_menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_readwpa) {
			new UIRefresher().execute(UIRefresher.READ_WPA_SUPPLICANT);
			new UIRefresher().execute(UIRefresher.POPULATE_WIFILISTVIEW);
			return true;
		} else if (id == R.id.action_clear_all) {
			new UIRefresher().execute(UIRefresher.CLEAR_WIFILISTVIEW);
			return true;
		} else if (id == R.id.action_sync) {
			new UIRefresher().execute(UIRefresher.SYNC_WIFI);
			new UIRefresher().execute(UIRefresher.POPULATE_WIFILISTVIEW);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
