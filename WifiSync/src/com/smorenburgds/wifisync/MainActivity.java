package com.smorenburgds.wifisync;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import com.smorenburgds.wifisync.versioning.AutoUpdateApk;

public class MainActivity extends Activity {

	public static ListView wifiListView;
	public static MainActivity activitymain;

	public static final boolean DEMO_MODE = false;

	private ClipboardManager clipboard;
	public static MenuItem sync;

	private Functions Do;
	private AutoUpdateApk wifiUpdateChecker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setActivitymain(this);
		wifiUpdateChecker = new AutoUpdateApk();
		update();
		Do = new Functions();
		wifiListView = (ListView) findViewById(R.id.listWifiView);
		clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		registerForContextMenu(wifiListView);
		populateWifiListView();
	}

	private void update() {

		new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				wifiUpdateChecker.getData(MainActivity.this);
				if (wifiUpdateChecker.isNewVersionAvailable()) {
					return true;
				}
				return false;
			}

			protected void onPostExecute(Boolean result) {

				if (result) {
					Toast.makeText(MainActivity.this,
							"Nueva Version Disponible! =)", Toast.LENGTH_LONG)
							.show();
					Builder downloadAsk = new AlertDialog.Builder(
							MainActivity.this)
							.setTitle(
									"WifiSync - Nueva Version "
											+ wifiUpdateChecker
													.getLatestVersionCode()
											+ "."
											+ wifiUpdateChecker
													.getLatestVersionName())
							.setMessage("¿Descargar nueva version?")
							.setCancelable(false)
							.setPositiveButton("Más tarde si eso...",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											finish();
										}

									})
							.setNegativeButton("Actualizar!",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											startActivity(new Intent(
													"android.intent.action.VIEW",
													Uri.parse(wifiUpdateChecker
															.getDownloadURL())));
											finish();
										}

									}).setIcon(android.R.drawable.ic_popup_sync);
					downloadAsk.show();
				} else {
					Toast.makeText(MainActivity.this, "No Hay Nueva Version",
							Toast.LENGTH_LONG).show();
				}
			};
		}.execute();

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
			Do.new tryToConnectToThisWiFi().execute(Do.getWifidao()
					.loadByRowId(pos.position + 1));
			return true;
		} else if (id == R.id.copyfullwifi) {

			if (DEMO_MODE) {
				Toast.makeText(this, "Funcion deshabilitada", Toast.LENGTH_LONG)
						.show();
			} else {
				ClipData clip = ClipData.newPlainText("label", Do.getWifidao()
						.loadByRowId(pos.position + 1).getRawData());
				clipboard.setPrimaryClip(clip);
				Toast.makeText(
						this,
						Do.getWifidao().loadByRowId(pos.position + 1)
								.getRawData(), Toast.LENGTH_LONG).show();
			}
			return true;
		} else if (id == R.id.copyPass) {

			if (DEMO_MODE) {
				Toast.makeText(this, "Funcion deshabilitada", Toast.LENGTH_LONG)
						.show();
			} else {
				ClipData clip = ClipData.newPlainText("label", Do.getWifidao()
						.loadByRowId(pos.position + 1).getPassword());
				clipboard.setPrimaryClip(clip);
				Toast.makeText(
						this,
						Do.getWifidao().loadByRowId(pos.position + 1)
								.getPassword(), Toast.LENGTH_LONG).show();
			}
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
					.setTitle("Borrar todos los WiFis")
					.setMessage("Estas segur@ de que quieres borrar todo?")
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
			AsyncTask<Void, Integer, List<Wifi>> asd = Do.new syncParseAndDao();

			asd.execute();
			return true;
		} else if (id == R.id.check_updates) {
			update();
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
