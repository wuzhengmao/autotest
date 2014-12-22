package cn.shaviation.autotest.internal.runner;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import cn.shaviation.autotest.model.MethodModel;
import cn.shaviation.autotest.runner.TestContext;
import cn.shaviation.autotest.util.Strings;

public class MethodModelImpl implements MethodModel {

	private TestContextImpl testContext;
	private Map<String, String> inputData;
	private Map<String, String> outputData;
	private boolean success = true;
	private String description;
	private File snapshot;

	public MethodModelImpl(TestContextImpl testContext,
			Map<String, String> inputData, Map<String, String> outputData) {
		this.testContext = testContext;
		this.inputData = inputData;
		this.outputData = outputData;
	}

	@Override
	public TestContext getTestContext() {
		return testContext;
	}

	@Override
	public Set<String> inputKeySet() {
		return Collections.unmodifiableSet(inputData.keySet());
	}

	@Override
	public String getInput(String key) {
		return inputData.get(key);
	}

	@Override
	public Set<String> outputKeySet() {
		return Collections.unmodifiableSet(outputData.keySet());
	}

	@Override
	public String getOutput(String key) {
		return outputData.get(key);
	}

	@Override
	public Set<String> paramKeySet() {
		return testContext.keySet();
	}

	@Override
	public String getParam(String key) {
		return testContext.get(key);
	}

	@Override
	public void success() {
		success = true;
	}

	@Override
	public void success(String description) {
		success = true;
		this.description = description;
	}

	@Override
	public void success(String description, boolean takeSnapshot) {
		success(description);
		if (takeSnapshot) {
			snapshot = testContext.getTestRunner().takeSnapshot(testContext);
		}
	}

	@Override
	public void fail(String description) {
		success = false;
		this.description = description;
	}

	@Override
	public void fail(String description, boolean takeSnapshot) {
		fail(description);
		if (takeSnapshot) {
			snapshot = testContext.getTestRunner().takeSnapshot(testContext);
		}
	}

	@Override
	public boolean isSuccess() {
		return success;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void appendDescription(String description) {
		if (Strings.isEmpty(this.description)) {
			this.description = description;
		} else {
			this.description += "\n" + description;
		}
	}

	@Override
	public File getSnapshot() {
		return snapshot;
	}
}
