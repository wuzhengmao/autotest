package cn.shaviation.autotest.model;

import java.util.List;

public interface TestNode extends TestElement {

	List<? extends TestElement> getChildren();

	int total();

	int count(Status status);
}
