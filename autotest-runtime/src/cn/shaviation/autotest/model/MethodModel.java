package cn.shaviation.autotest.model;

import java.util.Set;

public interface MethodModel {

	TestContext getTextContext();

	Set<String> inputKeySet();

	String getInput(String key);

	Set<String> outputKeySet();

	String getOutput(String key);

	Set<String> paramKeySet();

	String getParam(String key);

	void success();

	void success(String description);

	void fail(String description);

	void fail(String description, String snapshot);

	boolean isSuccess();

	String getDescription();

	void appendDescription(String description);

	String getSnapshot();
}
