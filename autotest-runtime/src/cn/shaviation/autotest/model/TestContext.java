package cn.shaviation.autotest.model;

import java.util.Map;
import java.util.Set;

public interface TestContext {

	TestExecution getTestExecution();

	TestScript getTestScript();

	TestStep getTestStep();

	TestElement getTestNode();

	Set<String> keySet();

	String get(String key);

	void put(String key, String value);

	void remove(String key);

	MethodModel createMethodModel(Map<String, String> inputData,
			Map<String, String> outputData);
}
