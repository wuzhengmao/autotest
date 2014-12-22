package cn.shaviation.autotest.runner;

import java.util.List;

public interface TestNode extends TestElement {

	List<? extends TestElement> getChildren();

	int total();

	int count(Status status);
}
