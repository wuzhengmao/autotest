package cn.shaviation.autotest.internal.runner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cn.shaviation.autotest.runner.TestExecution;

public class TestExecutionImpl extends TestNodeImpl implements TestExecution {

	private Map<String, String> args = new HashMap<String, String>();

	public TestExecutionImpl() {
		super();
		setName("Root");
		setType(Type.ROOT);
	}

	@Override
	public Map<String, String> getArgs() {
		return Collections.unmodifiableMap(args);
	}

	public void put(String key, String value) {
		args.put(key, value);
	}
}