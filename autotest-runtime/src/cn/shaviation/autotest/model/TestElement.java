package cn.shaviation.autotest.model;

public interface TestElement {

	public static enum Status {
		PASS, FAILURE, ERROR, BLOCKED, RUNNING
	}

	String getName();

	Status getStatus();

	String getDescription();

	String getSnapshot();

	Long getRunTime();
}
