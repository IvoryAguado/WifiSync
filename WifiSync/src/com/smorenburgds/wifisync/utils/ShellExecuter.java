package com.smorenburgds.wifisync.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellExecuter {
	public ShellExecuter() {
	}

	public static String Executer(String command, boolean asRoot) {
		StringBuffer output = new StringBuffer();
		Process p = null;
		try {
			if (asRoot)
				try {
					p = Runtime.getRuntime().exec(new String[]{"su","-","root"});
				} catch (IOException e) {
				}
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		String response = output.toString();
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}
}