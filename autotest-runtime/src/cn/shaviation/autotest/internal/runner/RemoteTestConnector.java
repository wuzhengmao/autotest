package cn.shaviation.autotest.internal.runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import cn.shaviation.autotest.runner.TestElement.Status;
import cn.shaviation.autotest.runner.TestElement.Type;
import cn.shaviation.autotest.util.Strings;

public class RemoteTestConnector {

	private static final String DELIMITER = "#@#&";
	private static final String ESCAPE = "#ESC#&";

	private TestRunner runner;
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
	private Thread thread;

	public RemoteTestConnector(TestRunner runner) {
		this.runner = runner;
	}

	public void connect(int port) throws IOException {
		socket = new Socket((String) null, port);
		try {
			writer = new PrintWriter(new OutputStreamWriter(
					socket.getOutputStream(), "UTF-8"), true);
		} catch (UnsupportedEncodingException e) {
			writer = new PrintWriter(new OutputStreamWriter(
					socket.getOutputStream()), true);
		}
		try {
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
		}
		thread = new Thread() {
			@Override
			public void run() {
				try {
					String message;
					while ((message = reader.readLine()) != null) {
						receiveMessage(message);
						if (reader == null) {
							break;
						}
					}
				} catch (IOException e) {
				}
				thread = null;
				try {
					if (reader != null) {
						reader.close();
						reader = null;
					}
				} catch (IOException e) {
				}
				if (writer != null) {
					writer.flush();
					writer.close();
					writer = null;
				}
				try {
					if (socket != null) {
						socket.close();
						socket = null;
					}
				} catch (IOException e) {
				}
			}
		};
		thread.setDaemon(true);
		thread.start();
	}

	public void close() {
		try {
			if (reader != null) {
				reader.close();
				reader = null;
			}
		} catch (IOException e) {
		}
		if (writer != null) {
			writer.flush();
			writer.close();
			writer = null;
		}
		try {
			if (socket != null) {
				socket.close();
				socket = null;
			}
		} catch (IOException e) {
		}
	}

	private void receiveMessage(String message) {
		if ("STOP".equals(message)) {
			runner.stop();
		}
	}

	public void sendNodeAdd(long id, String name, Type type, Long parentId) {
		List<Object> list = new ArrayList<Object>(5);
		list.add("A");
		list.add(id);
		list.add(name);
		list.add(type);
		list.add(parentId);
		String message = Strings.merge(list, DELIMITER).replace("\r\n", "\n")
				.replace("\n", ESCAPE + "\\n");
		writer.println(message);
	}

	public void sendNodeUpdate(long id, String name, Long runTime,
			Status status, String description, String snapshot) {
		List<Object> list = new ArrayList<Object>(6);
		list.add("U");
		list.add(id);
		list.add(name);
		list.add(runTime);
		list.add(status.name());
		list.add(description);
		list.add(snapshot);
		String message = Strings.merge(list, DELIMITER).replace("\r\n", "\n")
				.replace("\n", ESCAPE + "\\n");
		writer.println(message);
	}
}
