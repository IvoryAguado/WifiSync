package com.smorenburgds.wifisync.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class AndroTerm {

	private boolean asRoot;

	private Process process;

	private ExecutorService executorCacheThreadPool = Executors
			.newFixedThreadPool(5);

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

		FutureTask<String> theTask = new FutureTask<String>(
				new Callable<String>() {
					@Override
					public String call() throws Exception {
						StringBuffer buf = new StringBuffer();
						BufferedReader reader = null;
						String str = "";
						reader = new BufferedReader(new InputStreamReader(
								new FileInputStream(filePath)));
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
						}
						return buf.toString();
					}
				});
		try {
			executorCacheThreadPool.execute(theTask);
			return theTask.get();
		} catch (InterruptedException e) {
			return "File read failed!";
		} catch (ExecutionException e) {
			return "File read failed!";
		}
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

		executorCacheThreadPool.execute(new Runnable() {

			@Override
			public void run() {
				try {
					os.writeBytes(fCommand + "\n");
					os.writeBytes("exit\n");
					os.flush();
					os.close();
				} catch (IOException e) {
				}
			}
		});
		// executorCacheThreadPool.shutdown();
	}

	public void setAsRoot(boolean asRoot) {
		this.asRoot = asRoot;
	}
}
