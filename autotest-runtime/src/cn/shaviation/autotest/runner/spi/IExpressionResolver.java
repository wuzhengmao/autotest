package cn.shaviation.autotest.runner.spi;

import cn.shaviation.autotest.runner.TestContext;

public interface IExpressionResolver {

	String subject();

	Object resolve(TestContext testContext, String expression) throws Exception;
}
