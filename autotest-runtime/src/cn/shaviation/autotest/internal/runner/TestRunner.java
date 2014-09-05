package cn.shaviation.autotest.internal.runner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import cn.shaviation.autotest.annotation.TestMethod;
import cn.shaviation.autotest.model.MethodModel;
import cn.shaviation.autotest.model.TestDataDef;
import cn.shaviation.autotest.model.TestDataGroup;
import cn.shaviation.autotest.model.TestDataHelper;
import cn.shaviation.autotest.model.TestScript;
import cn.shaviation.autotest.model.TestScriptHelper;
import cn.shaviation.autotest.model.TestStep;
import cn.shaviation.autotest.model.TestStepIterator;
import cn.shaviation.autotest.model.TestStepIterator.ITestStepVisitor;
import cn.shaviation.autotest.util.IOUtils;
import cn.shaviation.autotest.util.Strings;

public class TestRunner {

	private List<String> resources = new ArrayList<String>();
	private boolean recursive = false;
	private boolean silent = false;

	public TestRunner(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if ("-r".equalsIgnoreCase(args[i])) {
				recursive = true;
			} else if ("-s".equalsIgnoreCase(args[i])) {
				silent = true;
			} else if (args[i].startsWith("-")) {
				throw new IllegalArgumentException(
						"Unknown commond line switch: " + args[i]);
			} else {
				resources.add(args[i]);
			}
		}
		if (resources.isEmpty()) {
			throw new IllegalArgumentException("No test script found.");
		}
	}

	public void run() throws Exception {
		for (String resource : resources) {
			try {
				run(resource);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	private void run(String resource) throws Exception {
		System.out.println("Run test script: " + resource);
		runTestScript(resource);
	}

	private void runTestScript(String resource) throws Exception {
		String json = IOUtils.toString(
				TestRunner.class.getResourceAsStream(resource), "utf-8");
		TestScript testScript = TestScriptHelper.parse(json);
		final List<TestStep> testSteps = testScript.getTestSteps();
		System.out.println("Starting \""
				+ (!Strings.isBlank(testScript.getName()) ? testScript
						.getName() : resource) + "\"...");
		if (testSteps != null) {
			final int[] counts = new int[4];
			counts[0] = testSteps.size();
			TestStepIterator.iterate(testSteps, new ITestStepVisitor() {
				@Override
				public boolean visit(TestStep testStep, int index) {
					System.out.println("Go step " + index + "... " + counts[1]
							+ "/" + counts[0]);
					try {
						switch (testStep.getInvokeType()) {
						case Method:
							runTestMethod(testStep);
							counts[1]++;
							counts[3]++;
							return true;
						case Script:
							runSubScript(testStep);
							counts[1]++;
							counts[3]++;
							return true;
						}
					} catch (Throwable e) {
						e.printStackTrace();
					}
					counts[1]++;
					counts[2]++;
					System.out.println("Error");
					return false;
				}
			});
		}
		System.out.println("Completed");
	}

	private void runTestMethod(TestStep testStep) throws Exception {
		String method = testStep.getInvokeTarget().trim();
		int p = method.lastIndexOf('#');
		Class<?> testClass = Class.forName(method.substring(0, p));
		Method testMethod = testClass.getMethod(method.substring(p + 1),
				MethodModel.class);
		TestMethod annotation = testMethod.getAnnotation(TestMethod.class);
		String name = annotation != null ? annotation.value() : null;
		if (Strings.isBlank(name)) {
			name = method;
		}
		String msg = "Invoke method \"" + name + "\" "
				+ testStep.getLoopTimes() + " times";
		TestDataDef testDataDef = null;
		if (!Strings.isBlank(testStep.getTestDataFile())) {
			String json = IOUtils.toString(TestRunner.class
					.getResourceAsStream(testStep.getTestDataFile().trim()),
					"utf-8");
			testDataDef = TestDataHelper.parse(json);
			msg += " with "
					+ (testDataDef.getDataList() != null ? testDataDef
							.getDataList().size() : 0)
					+ " group(s) test data \""
					+ (!Strings.isBlank(testDataDef.getName()) ? testDataDef
							.getName() : testStep.getTestDataFile().trim())
					+ "\"";
		}
		System.out.println(msg);
		for (int i = 0; i < testStep.getLoopTimes(); i++) {
			msg = "Loop " + (i + 1) + "/" + testStep.getLoopTimes();
			if (testDataDef != null) {
				if (testDataDef.getDataList() != null) {
					for (int j = 0; j < testDataDef.getDataList().size(); j++) {
						System.out.print(msg + " group " + (j + 1));
						invokeTestMethod(testMethod, testDataDef.getDataList()
								.get(j));
						System.out.println(" - Pass");
					}
				}
			} else {
				System.out.print(msg);
				invokeTestMethod(testMethod, null);
				System.out.println(" - Pass");
			}
		}
	}

	private void invokeTestMethod(Method testMethod, TestDataGroup testDataGroup)
			throws Exception {

	}

	private void runSubScript(TestStep testStep) throws Exception {
		String msg = "Execute sub script \""
				+ testStep.getInvokeTarget().trim() + "\" "
				+ testStep.getLoopTimes() + " times";
		System.out.println(msg);
		for (int i = 0; i < testStep.getLoopTimes(); i++) {
			System.out.println("Loop " + (i + 1) + "/"
					+ testStep.getLoopTimes());
			runTestScript(testStep.getInvokeTarget().trim());
		}
	}
}
