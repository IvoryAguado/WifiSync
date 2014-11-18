package com.smorenburgds.wifisync.misc;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.smorenburgds.wifisync.R;
import com.smorenburgds.wifisync.dao.Wifi;

public class WifiAdapter extends ArrayAdapter<Wifi> {

	private Activity context;
	private int layout;
	private List<Wifi> wifiList;

	public WifiAdapter(Activity context, int layout, List<Wifi> wifiList) {
		super(context, layout,wifiList);
		this.layout = layout;
		this.wifiList = wifiList;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = context.getLayoutInflater();

		convertView = inflater.inflate(layout, null);

		// Si optamos por eliminar del constructor del Adaptador el
		// parámetro del layout que utilizamos como molde, podríamos
		// hacerlo de la siguiente manera:
		// convertView = inflater.inflate(R.layout.list_item_fruta, null);

		Wifi wifiActual = wifiList.get(position);

		// Rescato los elementos del molde para modificarlos con el nombre y el
		// icono de la fruta actual

		TextView wifiEssid = (TextView) convertView.findViewById(R.id.textItemWifi);
//		TextView wifiPass = (TextView) convertView.findViewById(R.id.wifiPassword);

		wifiEssid.setText(wifiActual.getName());
//		wifiPass.setText(wifiActual.getPassword());

		return convertView;
	}

}
