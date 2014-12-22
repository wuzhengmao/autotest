package cn.shaviation.autotest.runner.spi.impl;

import cn.shaviation.autotest.runner.TestContext;
import cn.shaviation.autotest.runner.spi.ExpressionEvaluator;
import cn.shaviation.autotest.runner.spi.IExpressionResolver;

public class ContextExpressionResolverImpl implements IExpressionResolver {

	@Override
	public String subject() {
		return "$";
	}

	@Override
	public Object resolve(TestContext testContext, String expression)
			throws Exception {
		String key = ExpressionEvaluator.removeSubject(expression);
		if (testContext.attrNames().contains(key)) {
			return testContext.getAttr(key);
		} else {
			return testContext.get(key);
		}
	}
}
