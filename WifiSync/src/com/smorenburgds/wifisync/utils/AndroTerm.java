package com.smorenburgds.wifisync.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class AndroTerm {

	private boolean asRoot;

	private Process process;


	public AndroTerm(boolean asRoot) {
		setAsRoot(asRoot);
	}

	public class Command {
		private String commandString;

		public Command(String commandString) {
			this.commandString = commandString;
		}

		public String getCommandString() {
			return commandString;
		}

		public void setCommandString(String commandString) {
			this.commandString = commandString;
		}
	}

	public String convertStreamToString(final String filePath) {

		StringBuffer buf = new StringBuffer();
		BufferedReader reader = null;
		String str = "";
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(
					filePath)));
		
		if (reader != null) {
			while ((str = reader.readLine()) != null) {
				buf.append(str + "\n");
			}
		}
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
			}
		}} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return buf.toString();

	}

	public void commandExecutor(Command command) {

		// Perform su to get root privileges
		if (asRoot)
			try {
				process = Runtime.getRuntime().exec("su");
			} catch (IOException e) {
			}
		// To write in Terminal
		final DataOutputStream os = new DataOutputStream(
				process.getOutputStream());

		// To final so I can use it
		final String fCommand = command.getCommandString().trim();

		try {
			os.writeBytes(fCommand + "\n");
//			os.writeBytes("exit\n");
			os.flush();
			os.close();
		} catch (IOException e) {
		}

	}

	public void setAsRoot(boolean asRoot) {
		this.asRoot = asRoot;
	}
}
