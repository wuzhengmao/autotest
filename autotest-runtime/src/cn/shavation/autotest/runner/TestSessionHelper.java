package cn.shavation.autotest.runner;

import cn.shaviation.autotest.internal.runner.TestSessionImpl;

public abstract class TestSessionHelper {

	public static TestSession create(int port) {
		return new TestSessionImpl(port);
	}
}
