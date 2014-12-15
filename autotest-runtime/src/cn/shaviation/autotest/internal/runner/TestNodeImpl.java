package cn.shaviation.autotest.internal.runner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import cn.shavation.autotest.runner.TestNode;
import cn.shaviation.autotest.util.Strings;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TestNodeImpl implements TestNode {

	private static final AtomicLong SEQ = new AtomicLong();

	private long id;
	private String name;
	private Type type;
	private Status status;
	private Long startTime;
	private Long runTime;
	private String description;
	private String snapshot;
	private TestNodeImpl parent;
	private List<TestNodeImpl> children;

	public TestNodeImpl() {
		id = SEQ.incrementAndGet();
	}

	public TestNodeImpl(Long id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Type getType() {
		return type;
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

	@JsonIgnore
	@Override
	public TestNode getParent() {
		return parent;
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
		if (children != null) {
			for (TestNodeImpl node : children) {
				if ((status == null && node.getStatus() == null)
						|| (status != null && status.equals(node.getStatus()))) {
					count++;
				}
			}
		}
		return count;
	}

	@JsonIgnore
	public long getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setRunTime(Long runTime) {
		this.runTime = runTime;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setSnapshot(String snapshot) {
		this.snapshot = snapshot;
	}

	public void setChildren(List<TestNodeImpl> children) {
		this.children = children != null ? new ArrayList<TestNodeImpl>(children)
				: null;
		if (this.children != null) {
			for (TestNodeImpl child : this.children) {
				child.parent = this;
			}
		}
	}

	public void appendChild(TestNodeImpl child) {
		if (this.children == null) {
			this.children = new ArrayList<TestNodeImpl>();
		}
		this.children.add(child);
		child.parent = this;
	}

	public TestNodeImpl add(String name, Type type) {
		if (children == null) {
			children = new ArrayList<TestNodeImpl>();
		}
		TestNodeImpl node = new TestNodeImpl();
		node.setName(name);
		node.setType(type);
		children.add(node);
		node.parent = this;
		return node;
	}

	public void addAll(Collection<String> names, Type type) {
		for (String name : names) {
			add(name, type);
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

	public void stop() {
		runTime = System.currentTimeMillis() - startTime;
		status = Status.STOPPED;
	}

	public void complete() {
		if (startTime != null) {
			runTime = System.currentTimeMillis() - startTime;
		}
		int pass = 0;
		int failure = 0;
		int error = 0;
		int blocked = 0;
		int unfinished = 0;
		for (TestNodeImpl node : children) {
			Status ns = node.getStatus();
			if (ns == null || ns == Status.RUNNING || ns == Status.STOPPED) {
				unfinished++;
			} else if (ns == Status.PASS) {
				pass++;
			} else if (ns == Status.FAILURE) {
				failure++;
			} else if (ns == Status.ERROR) {
				error++;
			} else if (ns == Status.BLOCKED) {
				blocked++;
			}
		}
		if (unfinished > 0) {
			status = Status.STOPPED;
		} else if (failure == 0 && error == 0 && blocked == 0) {
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
