package cn.shaviation.autotest.model;

import java.io.File;
import java.util.Set;

import cn.shavation.autotest.runner.TestContext;

public interface MethodModel {

	TestContext getTestContext();

	Set<String> inputKeySet();

	String getInput(String key);

	Set<String> outputKeySet();

	String getOutput(String key);

	Set<String> paramKeySet();

	String getParam(String key);

	void success();

	void success(String description);

	void success(String description, boolean takeSnapshot);

	void fail(String description);

	void fail(String description, boolean takeSnapshot);

	boolean isSuccess();

	String getDescription();

	void appendDescription(String description);

	File getSnapshot();
}
