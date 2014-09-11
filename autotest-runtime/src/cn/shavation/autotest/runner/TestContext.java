package cn.shavation.autotest.runner;

import java.util.Map;
import java.util.Set;

import cn.shaviation.autotest.model.MethodModel;
import cn.shaviation.autotest.model.TestScript;
import cn.shaviation.autotest.model.TestStep;

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
