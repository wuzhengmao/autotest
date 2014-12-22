package cn.shaviation.autotest.internal.runner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cn.shaviation.autotest.model.MethodModel;
import cn.shaviation.autotest.model.TestScript;
import cn.shaviation.autotest.model.TestStep;
import cn.shaviation.autotest.runner.TestContext;

public class TestContextImpl implements TestContext {

	private TestRunner testRunner;
	private TestExecutionImpl testExecution = new TestExecutionImpl();
	private TestScript testScript;
	private int testStepIndex;
	private TestNodeImpl testNode;
	private Map<String, String> params = new HashMap<String, String>();
	private Map<String, Object> attrs = new HashMap<String, Object>();

	public TestContextImpl(TestRunner testRunner) {
		this.testRunner = testRunner;
	}

	@Override
	public TestExecutionImpl getTestExecution() {
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
		return Collections.unmodifiableSet(params.keySet());
	}

	@Override
	public String get(String key) {
		return params.get(key);
	}

	@Override
	public Set<String> attrNames() {
		return Collections.unmodifiableSet(attrs.keySet());
	}

	@Override
	public Object getAttr(String key) {
		return attrs.get(key);
	}

	@Override
	public Object setAttr(String key, Object value) {
		return attrs.put(key, value);
	}

	@Override
	public Object removeAttr(String key) {
		return attrs.remove(key);
	}

	@Override
	public MethodModel createMethodModel(Map<String, String> inputData,
			Map<String, String> outputData) {
		return new MethodModelImpl(this, inputData, outputData);
	}

	public TestRunner getTestRunner() {
		return testRunner;
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

	public void put(String key, String value) {
		params.put(key, value);
	}

	public void remove(String key) {
		params.remove(key);
	}
}
