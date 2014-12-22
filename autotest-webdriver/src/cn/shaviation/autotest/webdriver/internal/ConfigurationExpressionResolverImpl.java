package cn.shaviation.autotest.webdriver.internal;

import cn.shaviation.autotest.runner.TestContext;
import cn.shaviation.autotest.runner.spi.ExpressionEvaluator;
import cn.shaviation.autotest.runner.spi.IExpressionResolver;
import cn.shaviation.autotest.webdriver.Configuration;

public class ConfigurationExpressionResolverImpl implements IExpressionResolver {

	@Override
	public String subject() {
		return "$cfg";
	}

	@Override
	public Object resolve(TestContext testContext, String expression)
			throws Exception {
		String key = ExpressionEvaluator.removeSubject(expression);
		Configuration configuration = GlobalsBinder.configuration(testContext);
		return configuration != null ? configuration.get(key) : null;
	}
}
