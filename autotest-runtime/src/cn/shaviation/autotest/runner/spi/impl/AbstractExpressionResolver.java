package cn.shaviation.autotest.runner.spi.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import cn.shaviation.autotest.runner.TestContext;
import cn.shaviation.autotest.runner.spi.ExpressionEvaluator;
import cn.shaviation.autotest.runner.spi.IExpressionResolver;
import cn.shaviation.autotest.util.MethodUtils;
import cn.shaviation.autotest.util.Strings;

public abstract class AbstractExpressionResolver implements IExpressionResolver {

	private static final Pattern NUMBER = Pattern
			.compile("^(([\\+\\-]?\\d+)l?|([\\+\\-]?\\d+)(\\.\\d+)?[df]?)$");

	@Override
	public Object resolve(TestContext testContext, String expression)
			throws Exception {
		String body = ExpressionEvaluator.removeSubject(expression);
		if (Strings.isEmpty(body)) {
			throw error(expression);
		}
		String method;
		Object[] args;
		int i = body.indexOf('(');
		if (i < 0) {
			method = body;
			args = new Object[0];
		} else if (i == 0) {
			throw error(expression);
		} else if (body.charAt(body.length() - 1) != ')') {
			throw error(expression);
		} else {
			method = body.substring(0, i);
			String argStr = body.substring(i + 1, body.length() - 1).trim();
			if (Strings.isEmpty(argStr)) {
				args = new Object[0];
			} else {
				try {
					args = parseArgs(testContext, argStr);
				} catch (ParseArgException e) {
					throw new Exception("Cannot parse arguments \"" + argStr
							+ "\".", e);
				}
			}
		}
		return MethodUtils.invokeMethod(this, method, args);
	}

	private static Object[] parseArgs(TestContext testContext, String str)
			throws Exception {
		List<String> list = splitArgs(str);
		Object[] args = new Object[list.size()];
		for (int i = 0; i < list.size(); i++) {
			String arg = list.get(i);
			if (Strings.isEmpty(arg)) {
				throw new ParseArgException();
			}
			if ("null".equals(arg)) {
				args[i] = null;
			} else if ("true".equals(arg)) {
				args[i] = true;
			} else if ("false".equals(arg)) {
				args[i] = false;
			} else if (arg.charAt(0) == '"' || arg.charAt(0) == '\'') {
				String s = Strings.unescapeJavaString(arg.substring(1,
						arg.length() - 1));
				if (arg.charAt(0) == '"') {
					args[i] = s;
				} else if (s.length() == 1) {
					args[i] = s.charAt(0);
				} else {
					throw new ParseArgException();
				}
			} else if (NUMBER.matcher(arg).matches()) {
				try {
					if (arg.endsWith("d")) {
						args[i] = Double.parseDouble(arg.substring(0,
								arg.length() - 1));
					} else if (arg.endsWith("f")) {
						args[i] = Float.parseFloat(arg.substring(0,
								arg.length() - 1));
					} else if (arg.endsWith("l")) {
						args[i] = Long.parseLong(arg.substring(0,
								arg.length() - 1));
					} else if (arg.indexOf('.') < 0) {
						args[i] = Double.parseDouble(arg);
					} else {
						args[i] = Integer.parseInt(arg);
					}
				} catch (NumberFormatException e) {
					throw new ParseArgException(e);
				}
			} else {
				args[i] = ExpressionEvaluator.evaluate(testContext, arg);
			}
		}
		return args;
	}

	private static List<String> splitArgs(String str) throws ParseArgException {
		List<String> list = new ArrayList<String>();
		while (true) {
			int i = scanArg(str);
			if (i == 0 || i == str.length() - 1) {
				throw new ParseArgException();
			}
			list.add(str.substring(0, i).trim());
			if (i < str.length()) {
				str = str.substring(i + 1).trim();
			} else {
				break;
			}
		}
		return list;
	}

	private static final int MATCH_NONE = 0;
	private static final int MATCH_STRING = 1;
	private static final int MATCH_CHAR = 2;
	private static final int MATCH_BRACKET = 3;

	private static int scanArg(String str) throws ParseArgException {
		Stack<Integer> stack = new Stack<Integer>();
		int status = MATCH_NONE;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '\\') {
				i++;
			} else if (status == MATCH_NONE) {
				if (c == ',') {
					if (stack.isEmpty()) {
						return i;
					} else {
						status = stack.pop();
					}
				} else if (c == '"') {
					stack.push(status);
					status = MATCH_STRING;
				} else if (c == '\'') {
					stack.push(status);
					status = MATCH_CHAR;
				} else if (c == '(') {
					stack.push(status);
					status = MATCH_BRACKET;
				}
			} else if (status == MATCH_STRING) {
				if (c == '"') {
					if (stack.isEmpty()) {
						return i + 1;
					} else {
						status = stack.pop();
					}
				}
			} else if (status == MATCH_CHAR) {
				if (c == '\'') {
					if (stack.isEmpty()) {
						return i + 1;
					} else {
						status = stack.pop();
					}
				}
			} else if (status == MATCH_BRACKET) {
				if (c == ')') {
					status = stack.pop();
				} else if (c == '"') {
					stack.push(status);
					status = MATCH_STRING;
				} else if (c == '\'') {
					stack.push(status);
					status = MATCH_CHAR;
				} else if (c == '(') {
					stack.push(status);
					status = MATCH_BRACKET;
				}
			}
		}
		if (stack.isEmpty() && status == MATCH_NONE) {
			return str.length();
		}
		throw new ParseArgException();
	}

	private Exception error(String expression) {
		return new Exception("Cannot resolve \"" + expression
				+ "\", expected format is \"" + subject()
				+ ".method(arg1, arg2, ...)\".");
	}

	@SuppressWarnings("serial")
	public static class ParseArgException extends Exception {

		public ParseArgException() {
			super();
		}

		public ParseArgException(Throwable cause) {
			super(cause);
		}
	}
}
