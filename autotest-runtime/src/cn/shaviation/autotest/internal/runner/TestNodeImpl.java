package cn.shaviation.autotest.internal.runner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import cn.shaviation.autotest.model.TestExecution;
import cn.shaviation.autotest.model.TestNode;
import cn.shaviation.autotest.util.Strings;

public class TestNodeImpl implements TestExecution, TestNode {

	private String name;
	private Status status;
	private Long startTime;
	private Long runTime;
	private String description;
	private String snapshot;
	private List<TestNodeImpl> children;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public Long getRunTime() {
		return runTime;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getSnapshot() {
		return snapshot;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TestNodeImpl> getChildren() {
		return children != null ? Collections.unmodifiableList(children)
				: Collections.EMPTY_LIST;
	}

	@Override
	public int total() {
		return children != null ? children.size() : 0;
	}

	@Override
	public int count(Status status) {
		int count = 0;
		for (TestNodeImpl node : children) {
			if ((status == null && node.getStatus() == null)
					|| (status != null && status.equals(node.getStatus()))) {
				count++;
			}
		}
		return count;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TestNodeImpl add(String name) {
		if (children == null) {
			children = new ArrayList<TestNodeImpl>();
		}
		TestNodeImpl node = new TestNodeImpl();
		node.setName(name);
		children.add(node);
		return node;
	}

	public void addAll(Collection<String> names) {
		for (String name : names) {
			add(name);
		}
	}

	public void start() {
		status = Status.RUNNING;
		startTime = System.currentTimeMillis();
	}

	public void success(String description, String snapshot) {
		runTime = System.currentTimeMillis() - startTime;
		status = Status.PASS;
		this.description = description;
		this.snapshot = snapshot;
	}

	public void fail(String description, String snapshot) {
		runTime = System.currentTimeMillis() - startTime;
		status = Status.FAILURE;
		this.description = description;
		this.snapshot = snapshot;
	}

	public void error(String description) {
		runTime = System.currentTimeMillis() - startTime;
		status = Status.ERROR;
		this.description = description;
	}

	public void error(Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw, true));
		error(sw.toString());
	}

	public void block() {
		status = Status.BLOCKED;
	}

	public void complete() {
		runTime = System.currentTimeMillis() - startTime;
		int pass = 0;
		int failure = 0;
		int error = 0;
		int blocked = 0;
		for (TestNodeImpl node : children) {
			switch (node.getStatus()) {
			case PASS:
				pass++;
				break;
			case FAILURE:
				failure++;
				break;
			case ERROR:
				error++;
				break;
			case BLOCKED:
				blocked++;
				break;
			}
		}
		if (failure == 0 && error == 0 && blocked == 0) {
			status = Status.PASS;
		} else if (error > 0) {
			status = Status.ERROR;
		} else {
			status = Status.FAILURE;
		}
		description = "Pass:" + pass + " Failure:" + failure + " Error:"
				+ error + " Blocked:" + blocked;
	}

	public void printOut() {
		printOut("", "");
	}

	private void printOut(String padding1, String padding2) {
		System.out.println(padding1 + "["
				+ (status != null ? status.name().charAt(0) : ' ') + "] "
				+ name + " - Cost " + runTime + "ms");
		if (children != null && !children.isEmpty()) {
			for (TestNodeImpl child : children) {
				child.printOut(padding2 + " |--", padding2 + " |  ");
			}
		}
		if (!Strings.isEmpty(description)) {
			String message = insertPadding(description, padding2);
			if (status != Status.ERROR) {
				System.out.println(message);
			} else {
				System.err.println(message);
			}
		}
	}

	private String insertPadding(String message, String padding) {
		return padding + message.replaceAll("\n", "\n" + padding);
	}
}
