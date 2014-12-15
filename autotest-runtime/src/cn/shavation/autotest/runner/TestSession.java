package cn.shavation.autotest.runner;

public interface TestSession {

	TestExecution getTestExecution();

	boolean isDone();

	void stop();

	void setListener(ITestSessionListener listener);
}
