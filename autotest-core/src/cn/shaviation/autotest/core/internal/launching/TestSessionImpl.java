package cn.shaviation.autotest.core.internal.launching;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.core.IJavaProject;

import cn.shavation.autotest.runner.TestElement.Status;
import cn.shavation.autotest.runner.TestElement.Type;
import cn.shavation.autotest.runner.TestExecution;
import cn.shaviation.autotest.core.ITestSessionListener;
import cn.shaviation.autotest.core.TestSession;
import cn.shaviation.autotest.util.Logs;
import cn.shaviation.autotest.util.Strings;

public class TestSessionImpl implements TestSession {

	private static final String DELIMITER = "#@#&";
	private static final int WAITING = 0;
	private static final int RUNNING = 1;
	private static final int COMPLETED = 2;
	private static final int STOPPED = -1;

	private ILaunch launch;
	private IJavaProject project;
	private ServerConnection connection;
	private TestExecution testExecution;
	private Map<Long, TestNodeImpl> testNodes;
	private int state = WAITING;
	private ITestSessionListener listener;

	public TestSessionImpl(ILaunch launch, IJavaProject project, int port) {
		this.launch = launch;
		this.project = project;
		connection = new ServerConnection(port);
		connection.start();
	}

	@Override
	public ILaunch getLaunch() {
		return launch;
	}

	@Override
	public IJavaProject getProject() {
		return project;
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
		List<String> args = Strings.split(message, DELIMITER);
		if ("S".equals(args.get(0))) {
			long id = Long.parseLong(args.get(1));
			String name = args.get(2);
			Type type = Type.valueOf(args.get(3));
			Long parentId = args.get(4) != null ? Long.parseLong(args.get(4))
					: null;
			TestNodeImpl testNode = new TestNodeImpl();
			testNode.setName(name);
			testNode.setType(type);
			if (type == Type.ROOT) {
				testExecution = testNode;
				testNodes = new HashMap<Long, TestNodeImpl>();
				state = RUNNING;
				if (listener != null) {
					listener.onStart();
				}
			}
			testNodes.put(id, testNode);
			if (parentId != null) {
				TestNodeImpl parent = testNodes.get(parentId);
				if (parent.getChildren() == null) {
					parent.setChildren(new ArrayList<TestNodeImpl>());
				}
				parent.getChildren().add(testNode);
			}
		} else if ("C".equals(args.get(0))) {
			long id = Long.parseLong(args.get(1));
			Long runTime = args.get(2) != null ? Long.parseLong(args.get(2))
					: null;
			Status status = Status.valueOf(args.get(3));
			String description = args.get(4);
			String snapshot = args.get(5);
			TestNodeImpl testNode = testNodes.get(id);
			testNode.setRunTime(runTime);
			testNode.setStatus(status);
			testNode.setDescription(description);
			testNode.setSnapshot(snapshot);
			if (testNode.getType() == Type.ROOT) {
				testNodes.clear();
				testNodes = null;
				connection.shutdown();
				state = COMPLETED;
				if (listener != null) {
					listener.onComplete();
				}
			}
		}
	}

	private void notifyTerminated() {
		testNodes.clear();
		testNodes = null;
		state = STOPPED;
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
