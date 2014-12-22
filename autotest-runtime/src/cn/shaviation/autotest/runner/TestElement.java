package cn.shaviation.autotest.runner;

public interface TestElement {

	public static enum Status {
		PASS, FAILURE, ERROR, BLOCKED, RUNNING, STOPPED
	}

	public static enum Type {
		ROOT, SCRIPT, METHOD, LOOP
	}

	String getName();

	Type getType();

	Status getStatus();

	String getDescription();

	String getSnapshot();

	Long getRunTime();

	TestNode getParent();
}
