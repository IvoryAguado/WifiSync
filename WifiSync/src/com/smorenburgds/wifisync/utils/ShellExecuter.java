package com.smorenburgds.wifisync.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.os.AsyncTask;

public abstract class ShellExecuter extends AsyncTask<String, Integer, String> {
	
	private boolean asRoot;
	
	public ShellExecuter(boolean asRoot) {
		this.asRoot=asRoot;
	}

	private String termExecuter(String command) {
		StringBuffer output = new StringBuffer();
		Process p = null;
		try {
			if (asRoot)
				try {
					p = Runtime.getRuntime().exec(
							new String[] { "su", "-", "root" });
				} catch (IOException e) {
				}
			p = Runtime.getRuntime().exec("");
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}
		} catch (Exception e) {}
		String response = output.toString();
		return response;
	}

	@Override
	protected String doInBackground(String... commands) {
		
		String line = "";
		
		for (String command : commands) {
			line += termExecuter(command);
		}

		return line;
	}
}