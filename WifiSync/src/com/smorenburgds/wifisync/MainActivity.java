package com.smorenburgds.wifisync;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;

import com.smorenburgds.wifisync.dao.DaoMaster;
import com.smorenburgds.wifisync.dao.DaoMaster.DevOpenHelper;
import com.smorenburgds.wifisync.dao.DaoSession;
import com.smorenburgds.wifisync.dao.Wifi;
import com.smorenburgds.wifisync.dao.WifiDao;
import com.smorenburgds.wifisync.misc.WifiAdapter;
import com.smorenburgds.wifisync.utils.AndroTerm;
import com.smorenburgds.wifisync.utils.WifiBackupAgent;

public class MainActivity extends Activity {

	public static final String FILE_WIFI_SUPPLICANT = "/data/misc/wifi/wpa_supplicant.conf";
	private static final String FILE_WIFI_SUPPLICANT_TEMPLATE = "/system/etc/wifi/wpa_supplicant.conf";

	private ListView wifiListView;

	private DaoMaster daoMaster;
	private DaoSession daoSession;
	private WifiDao wifidao;
	private SQLiteDatabase db;
	private AndroTerm andT = new AndroTerm(true);
	private WifiBackupAgent wifiBA = new WifiBackupAgent();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		daoGenStart();
		wifiListView = (ListView) this.findViewById(R.id.listWifiView);

		andT.commandExecutor(andT.new Command("cp " + FILE_WIFI_SUPPLICANT
				+ " /storage/ext_sd/"));

		registerForContextMenu(wifiListView);

		wifiBA.parseWpa_supplicantFile(andT
				.convertStreamToString("/storage/ext_sd/wpa_supplicant.conf"));

		// wifidao.deleteAll();

		// wifiListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

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

	private void syncWifis() {
		wifidao.insert(new Wifi(null, "SmorenburgS", ("1234"), "raw"));
		wifidao.insert(new Wifi(null, "SmorenburgS2", ("1234"), "raw"));
		wifidao.insert(new Wifi(null, "SmorenburgS3", ("1234"), "raw"));
		wifidao.insert(new Wifi(null, "SmorenburgS4", ("1234"), "raw"));
		populateWifiListView();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.setHeaderTitle("Wifi - Options");
		getMenuInflater().inflate(R.menu.contextual_menu_main, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.tryconnectwifi) {
			return true;
		} else if (id == R.id.copyfullwifi) {
			return true;
		} else if (id == R.id.copyPass) {
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onContextMenuClosed(Menu menu) {
		wifidao.deleteAll();
//		populateWifiListView();
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
			return true;
		} else if (id == R.id.action_sync) {
			syncWifis();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
