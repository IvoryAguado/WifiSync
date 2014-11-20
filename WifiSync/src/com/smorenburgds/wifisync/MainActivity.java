package com.smorenburgds.wifisync;

import java.util.List;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

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
	private static final String FILE_WIFI_SUPPLICANT_TEMPLATE = "/system/etc/wifi/wpa_supplicant.conf";
	private static final String DIR_WIFISYNC_TEMP = "/storage/ext_sd/";
	private static final String FILE_WIFI_SUPPLICANT_TEMP = "/storage/ext_sd/wpa_supplicant.conf";

	private ListView wifiListView;

	private ClipboardManager clipboard;

	private DaoMaster daoMaster;
	private DaoSession daoSession;
	private WifiDao wifidao;
	private SQLiteDatabase db;
	private AndroTerm andT;
	private WifiBackupAgent wifiBA = new WifiBackupAgent();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		andT = new AndroTerm(true);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		daoGenStart();
		wifiListView = (ListView) findViewById(R.id.listWifiView);
		clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		registerForContextMenu(wifiListView);
		// wifiListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		populateWifiListView();

	}

	private void populateWifiListView() {
		WifiAdapter adapter = new WifiAdapter(this, R.layout.list_wifi_item,
				wifidao.loadAll());
		wifiListView.setAdapter(adapter);
	}

	private void daoGenStart() {
		DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "wifis-db",
				null);
		db = helper.getWritableDatabase();
		daoMaster = new DaoMaster(db);
		daoSession = daoMaster.newSession();
		wifidao = daoSession.getWifiDao();

	}

	private void readFromPhoneWifis() throws InterruptedException {
		andT.commandExecutor(andT.new Command("cp " + FILE_WIFI_SUPPLICANT
				+ " " + DIR_WIFISYNC_TEMP));
		while (wifidao.loadAll().isEmpty())
			syncWifis();
	}

	private void syncWifis() {
		List<Wifi> listFromFile = wifiBA.parseWpa_supplicantFile(andT
				.convertStreamToString(FILE_WIFI_SUPPLICANT_TEMP));
		if (!listFromFile.isEmpty()) {
			wifidao.deleteAll();
			for (Wifi s : listFromFile) {
				wifidao.insert(s);
			}
			populateWifiListView();
			andT.commandExecutor(andT.new Command("rm  " + DIR_WIFISYNC_TEMP
					+ "wpa_supplicant.conf"));
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
		if (id == R.id.action_settings) {
			try {
				readFromPhoneWifis();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		} else if (id == R.id.action_clear_all) {
			wifidao.deleteAll();
			populateWifiListView();
			return true;
		} else if (id == R.id.action_sync) {
			syncWifis();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
