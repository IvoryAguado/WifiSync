package com;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class SetUpWifiSyncDB {

	public static void main(String args[]) throws Exception {
		Schema schema = new Schema(1, "com.smorenburgds.wifisync.dao");
		Entity upload = schema.addEntity("Wifi");
		
		upload.addIdProperty();
		upload.addStringProperty("name");
		upload.addStringProperty("password");
		upload.addStringProperty("rawData");
		
		new DaoGenerator().generateAll(schema,
				"../WifiSync/src/");
	}

}
