package cn.shaviation.autotest.internal.runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.shavation.autotest.runner.ITestSessionListener;
import cn.shavation.autotest.runner.TestElement.Status;
import cn.shavation.autotest.runner.TestElement.Type;
import cn.shavation.autotest.runner.TestExecution;
import cn.shavation.autotest.runner.TestSession;
import cn.shaviation.autotest.util.Logs;
import cn.shaviation.autotest.util.Strings;

public class TestSessionImpl implements TestSession {

	private static final String DELIMITER = "#@#&";
	private static final String ESCAPE = "#ESC#&";
	private static final int WAITING = 0;
	private static final int RUNNING = 1;
	private static final int COMPLETED = 2;
	private static final int STOPPED = -1;

	private ServerConnection connection;
	private TestExecution testExecution;
	private Map<Long, TestNodeImpl> testNodes;
	private int state = WAITING;
	private ITestSessionListener listener;

	public TestSessionImpl(int port) {
		connection = new ServerConnection(port);
		connection.start();
	}

	@Override
	public TestExecution getTestExecution() {
		return testExecution;
	}

	@Override
	public boolean isDone() {
		return state == COMPLETED || state == STOPPED;
	}

	@Override
	public void stop() {
		connection.send("STOP");
	}

	@Override
	public void setListener(ITestSessionListener listener) {
		this.listener = listener;
	}

	private void receiveMessage(String message) {
		List<String> args = Strings.split(
				message.replace(ESCAPE + "\\r", "\r"), DELIMITER);
		if ("A".equals(args.get(0))) {
			long id = Long.parseLong(args.get(1));
			String name = args.get(2);
			Type type = Type.valueOf(args.get(3));
			Long parentId = !Strings.isEmpty(args.get(4)) ? Long.parseLong(args
					.get(4)) : null;
			TestNodeImpl testNode = type == Type.ROOT ? new TestExecutionImpl()
					: new TestNodeImpl();
			testNode.setName(name);
			testNode.setType(type);
			if (type == Type.ROOT) {
				testExecution = (TestExecution) testNode;
				testNodes = new HashMap<Long, TestNodeImpl>();
				state = RUNNING;
				if (listener != null) {
					listener.onStart(testExecution);
				}
			}
			testNodes.put(id, testNode);
			if (parentId != null) {
				TestNodeImpl parent = testNodes.get(parentId);
				parent.appendChild(testNode);
			}
			if (listener != null) {
				listener.onNodeAdd(testNode);
			}
		} else if ("U".equals(args.get(0))) {
			long id = Long.parseLong(args.get(1));
			String name = args.get(2);
			Long runTime = !Strings.isEmpty(args.get(3)) ? Long.parseLong(args
					.get(3)) : null;
			Status status = Status.valueOf(args.get(4));
			String description = args.get(5);
			String snapshot = args.get(6);
			TestNodeImpl testNode = testNodes.get(id);
			testNode.setName(name);
			testNode.setRunTime(runTime);
			testNode.setStatus(status);
			testNode.setDescription(description);
			testNode.setSnapshot(snapshot);
			if (listener != null) {
				listener.onNodeUpdate(testNode);
			}
			if (testNode.getType() == Type.ROOT) {
				testNodes.clear();
				testNodes = null;
				connection.shutdown();
				state = COMPLETED;
				if (listener != null) {
					listener.onComplete(testExecution);
				}
			}
		}
	}

	private void notifyTerminated() {
		if (!isDone()) {
			testNodes.clear();
			testNodes = null;
			state = STOPPED;
			if (listener != null) {
				listener.onTerminate(testExecution);
			}
		}
	}

	private class ServerConnection extends Thread {

		private int port;
		private ServerSocket serverSocket;
		private Socket socket;
		private BufferedReader reader;
		private PrintWriter writer;

		public ServerConnection(int port) {
			this.port = port;
		}

		@Override
		public void run() {
			try {
				serverSocket = new ServerSocket(port);
				socket = serverSocket.accept();
				try {
					reader = new BufferedReader(new InputStreamReader(
							socket.getInputStream(), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					reader = new BufferedReader(new InputStreamReader(
							socket.getInputStream()));
				}
				try {
					writer = new PrintWriter(new OutputStreamWriter(
							socket.getOutputStream(), "UTF-8"), true);
				} catch (UnsupportedEncodingException e) {
					writer = new PrintWriter(new OutputStreamWriter(
							socket.getOutputStream()), true);
				}
				String message;
				while ((message = reader.readLine()) != null) {
					receiveMessage(message);
					if (reader == null) {
						break;
					}
				}
			} catch (SocketException e) {
				notifyTerminated();
			} catch (IOException e) {
				Logs.e(e);
			}
			shutdown();
		}

		public void send(String message) {
			writer.println(message);
		}

		public void shutdown() {
			if (writer != null) {
				writer.close();
				writer = null;
			}
			try {
				if (reader != null) {
					reader.close();
					reader = null;
				}
			} catch (IOException e) {
			}
			try {
				if (socket != null) {
					socket.close();
					socket = null;
				}
			} catch (IOException e) {
			}
			try {
				if (serverSocket != null) {
					serverSocket.close();
					serverSocket = null;
				}
			} catch (IOException e) {
			}
		}
	}
}
