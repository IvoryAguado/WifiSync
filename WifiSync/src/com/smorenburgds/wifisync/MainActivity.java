package com.smorenburgds.wifisync;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
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

import com.smorenburgds.wifisync.dao.Wifi;
import com.smorenburgds.wifisync.misc.WifiAdapter;
import com.smorenburgds.wifisync.threads.Functions;

public class MainActivity extends Activity {

	

	public static ListView wifiListView;
	public static MainActivity activitymain;

	private ClipboardManager clipboard;
	public static MenuItem sync;

	private Functions Do;

	WifiAdapter adapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setActivitymain(this);
		Do = new Functions();
		wifiListView = (ListView) findViewById(R.id.listWifiView);
		clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		registerForContextMenu(wifiListView);
		populateWifiListView();
	}

	private void populateWifiListView() {
		wifiListView.setAdapter(new WifiAdapter(MainActivity.this, Do
				.getWifidao().loadAll()));
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo pos = (AdapterContextMenuInfo) menuInfo;

		menu.setHeaderTitle("Wifi - Options "
				+ Do.getWifidao().loadByRowId(pos.position + 1).getName());
		getMenuInflater().inflate(R.menu.contextual_menu_main, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo pos = (AdapterContextMenuInfo) item
				.getMenuInfo();

		int id = item.getItemId();
		if (id == R.id.tryconnectwifi) {

			return true;
		} else if (id == R.id.copyfullwifi) {
			ClipData clip = ClipData.newPlainText("label", Do.getWifidao()
					.loadByRowId(pos.position + 1).getRawData());
			clipboard.setPrimaryClip(clip);
			Toast.makeText(this,
					Do.getWifidao().loadByRowId(pos.position + 1).getRawData(),
					Toast.LENGTH_LONG).show();
			return true;
		} else if (id == R.id.copyPass) {

			ClipData clip = ClipData.newPlainText("label", Do.getWifidao()
					.loadByRowId(pos.position + 1).getPassword());
			clipboard.setPrimaryClip(clip);

			Toast.makeText(
					this,
					Do.getWifidao().loadByRowId(pos.position + 1).getPassword(),
					Toast.LENGTH_LONG).show();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.options_menu_main, menu);
		sync = menu.getItem(0);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_readwpa) {
			Do.new readFromPhoneWifis().execute();
			return true;
		} else if (id == R.id.action_clear_all) {
			new AlertDialog.Builder(this)
					.setTitle("Delete All Entrys")
					.setMessage("Are you sure you want to delete all entrys?")
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									Do.getWifidao().deleteAll();
									populateWifiListView();
								}
							})
					.setNegativeButton(android.R.string.no,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).setIcon(android.R.drawable.ic_dialog_alert)
					.show();

			return true;
		} else if (id == R.id.action_sync) {
			sync.setEnabled(false);
			AsyncTask<Void, Integer, List<Wifi>> asd =Do.new syncParseAndDao();
			
			asd.execute();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public ListView getListView() {
		return wifiListView;
	}

	public static MainActivity getActivitymain() {
		return activitymain;
	}

	public static void setActivitymain(MainActivity activitymain) {
		MainActivity.activitymain = activitymain;
	}

}
