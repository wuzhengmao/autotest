package cn.shaviation.autotest.runner;

public abstract class TestContexts {

	protected static final ThreadLocal<TestContext> contexts = new ThreadLocal<TestContext>();

	public static TestContext get() {
		return contexts.get();
	}
}
