package cn.shaviation.autotest.runner.spi;

import cn.shaviation.autotest.runner.TestContext;

public abstract class ExpressionEvaluator {

	public static Object evaluate(TestContext testContext, String expression)
			throws Exception {
		IExpressionResolver defaultResolver = null;
		String subject = getSubject(expression);
		for (IExpressionResolver resolver : ServiceLocator
				.getServices(IExpressionResolver.class)) {
			if ("*".equals(resolver.subject())) {
				defaultResolver = resolver;
			} else if (subject.equals(resolver.subject())) {
				return resolver.resolve(testContext, expression);
			}
		}
		return defaultResolver != null ? defaultResolver.resolve(testContext,
				expression) : null;
	}

	public static String getSubject(String expression) {
		int i = 0;
		for (; i < expression.length(); i++) {
			char c = expression.charAt(i);
			if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
					|| (c >= '0' && c <= '9') || c == '_' || c == '$')) {
				break;
			}
		}
		return expression.substring(0, i);
	}

	public static String removeSubject(String expression) {
		String subject = getSubject(expression);
		String str = expression.substring(subject.length()).trim();
		if (!str.isEmpty() && str.charAt(0) == '.') {
			str = str.substring(1).trim();
		}
		return str;
	}
}
