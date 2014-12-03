package cn.shaviation.autotest.internal.runner;

import cn.shavation.autotest.runner.TestExecution;

public class TestExecutionImpl extends TestNodeImpl implements TestExecution {

	public TestExecutionImpl() {
		super();
		setName("Root");
		setType(Type.ROOT);
	}
}