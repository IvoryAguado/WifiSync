package com.smorenburgds.wifisync.misc;

import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.smorenburgds.wifisync.MainActivity;
import com.smorenburgds.wifisync.R;
import com.smorenburgds.wifisync.dao.Wifi;

public class WifiAdapter extends ArrayAdapter<Wifi> {

	private Activity context;
	private List<Wifi> wifiList;

	public WifiAdapter(Activity context, List<Wifi> wifiList) {
		super(context, R.layout.list_wifi_item, wifiList);
		// Collections.sort(wifiList, new Comparator<Wifi>() {
		//
		// @Override
		// public int compare(Wifi lhs, Wifi rhs) {
		// return lhs.getName().compareTo(rhs.getName());
		// }
		// });
		this.wifiList = wifiList;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = context.getLayoutInflater();

		convertView = inflater.inflate(R.layout.list_wifi_item, null);

		// Si optamos por eliminar del constructor del Adaptador el
		// parámetro del layout que utilizamos como molde, podríamos
		// hacerlo de la siguiente manera:
		// convertView = inflater.inflate(R.layout.list_item_fruta, null);

		Wifi wifiActual = wifiList.get(position);
		TextView wifiEssid = (TextView) convertView
				.findViewById(R.id.textItemWifi);
		TextView wifiPass = (TextView) convertView
				.findViewById(R.id.passwordSsid);
		TextView mgmtKey = (TextView) convertView.findViewById(R.id.mgmtKey);

		// Rescato los elementos del molde para modificarlos con el nombre y el
		// icono de la fruta actual

		wifiEssid.setText(wifiActual.getName());
		
		
		if (wifiActual.getRawData().contains("key_mgmt=WPA-PSK")) {
			mgmtKey.setText("WPA-PSK");
		}else if(wifiActual.getRawData().contains("key_mgmt=NONE") && !wifiActual.getRawData().contains("psk=")){
			mgmtKey.setText("OPEN");
		}if(wifiActual.getRawData().contains("key_mgmt=WPA-EAP")){
			mgmtKey.setText("WPA-EAP");
		}
		if (MainActivity.DEMO_MODE) {
			wifiPass.setText("");
		} else {
			wifiPass.setText(wifiActual.getPassword());
		}
		mgmtKey.setTextColor(Color.parseColor("#000000"));
		wifiPass.setTextColor(Color.parseColor("#628DBA"));
		convertView.setBackgroundColor(Color.WHITE);

		if (position % 2 == 0) {
			mgmtKey.setTextColor(Color.parseColor("#ffffff"));
			convertView.setBackgroundColor(Color.parseColor("#87C1FF"));
			wifiPass.setTextColor(Color.WHITE);
		}
		return convertView;
	}

}
