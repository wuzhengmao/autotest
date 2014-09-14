package cn.shavation.autotest.runner;

import java.util.Map;

public interface TestExecution extends TestNode {

	Map<String, String> getArgs();
}
