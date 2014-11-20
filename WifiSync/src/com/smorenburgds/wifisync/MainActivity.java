package com.smorenburgds.wifisync;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

	private static final String FILE_WIFI_SUPPLICANT = "/data/misc/wifi/wpa_supplicant.conf";
	private static final String FILE_WIFI_SUPPLICANT_TEMPLATE = "/system/etc/wifi/wpa_supplicant.conf";
	private static final String FILE_WIFI_SUPPLICANT_TEMP = "/storage/ext_sd/wpa_supplicant.conf";

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

		registerForContextMenu(wifiListView);
		syncWifis();
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

	private void readFromPhoneWifis() {
		andT.commandExecutor(andT.new Command("cp " + FILE_WIFI_SUPPLICANT
				+ " /storage/ext_sd/"));
		syncWifis();
	}

	private void syncWifis() {
		wifidao.deleteAll();
		for (Wifi s : wifiBA.parseWpa_supplicantFile(andT
				.convertStreamToString(FILE_WIFI_SUPPLICANT_TEMP))) {
			wifidao.insert(s);
		}
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
		} else if (id == R.id.action_clear_all) {
			wifidao.deleteAll();
			populateWifiListView();
			return true;
		} else if (id == R.id.action_sync) {
			 readFromPhoneWifis();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
