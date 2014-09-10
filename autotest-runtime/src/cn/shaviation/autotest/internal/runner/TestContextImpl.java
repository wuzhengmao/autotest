package cn.shaviation.autotest.internal.runner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cn.shaviation.autotest.model.MethodModel;
import cn.shaviation.autotest.model.TestContext;
import cn.shaviation.autotest.model.TestScript;
import cn.shaviation.autotest.model.TestStep;

public class TestContextImpl implements TestContext {

	private TestNodeImpl testExecution = new TestNodeImpl();
	private TestScript testScript;
	private int testStepIndex;
	private TestNodeImpl testNode;
	private Map<String, String> map = new HashMap<String, String>();

	@Override
	public TestNodeImpl getTestExecution() {
		return testExecution;
	}

	@Override
	public TestScript getTestScript() {
		return testScript;
	}

	@Override
	public TestStep getTestStep() {
		return testScript.getTestSteps().get(testStepIndex);
	}

	@Override
	public TestNodeImpl getTestNode() {
		return testNode;
	}

	@Override
	public Set<String> keySet() {
		return Collections.unmodifiableSet(map.keySet());
	}

	@Override
	public String get(String key) {
		return map.get(key);
	}

	@Override
	public void put(String key, String value) {
		map.put(key, value);
	}

	@Override
	public void remove(String key) {
		map.remove(key);
	}

	@Override
	public MethodModel createMethodModel(Map<String, String> inputData,
			Map<String, String> outputData) {
		return new MethodModelImpl(this, inputData, outputData);
	}

	public void setTestScript(TestScript testScript) {
		this.testScript = testScript;
	}

	public int getTestStepIndex() {
		return testStepIndex;
	}

	public void setTestStepIndex(int testStepIndex) {
		this.testStepIndex = testStepIndex;
	}

	public void setTestNode(TestNodeImpl testNode) {
		this.testNode = testNode;
	}
}
