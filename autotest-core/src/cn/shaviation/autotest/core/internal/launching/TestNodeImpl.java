package cn.shaviation.autotest.core.internal.launching;

import java.util.List;

import cn.shavation.autotest.runner.TestElement;
import cn.shavation.autotest.runner.TestExecution;
import cn.shavation.autotest.runner.TestNode;

public class TestNodeImpl implements TestElement, TestNode, TestExecution {

	private String name;
	private Type type;
	private Status status;
	private Long runTime;
	private String description;
	private String snapshot;
	private List<TestNodeImpl> children;

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

	@Override
	public List<TestNodeImpl> getChildren() {
		return children;
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
		this.children = children;
	}
}
