package cn.shavation.autotest.runner;

import java.util.Map;

public interface TestExecution extends TestNode {

	String ARG_PROJECT = "project";
	String ARG_LOCATION = "location";
	String ARG_RECURSIVE = "recursive";
	String ARG_LOG_PATH = "logPath";

	Map<String, String> getArgs();
}
