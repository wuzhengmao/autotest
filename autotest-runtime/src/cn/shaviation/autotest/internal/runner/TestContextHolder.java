package cn.shaviation.autotest.internal.runner;

import cn.shaviation.autotest.runner.TestContext;
import cn.shaviation.autotest.runner.TestContexts;

public abstract class TestContextHolder extends TestContexts {

	public static void set(TestContext context) {
		contexts.set(context);
	}

	public static void unset() {
		contexts.remove();
	}
}
