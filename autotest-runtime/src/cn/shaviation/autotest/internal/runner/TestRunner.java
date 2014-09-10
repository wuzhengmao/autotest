package cn.shaviation.autotest.internal.runner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.shavation.autotest.AutoTest;
import cn.shaviation.autotest.annotation.Singleton;
import cn.shaviation.autotest.annotation.TestMethod;
import cn.shaviation.autotest.internal.pathmatch.PathPatternResolver;
import cn.shaviation.autotest.internal.pathmatch.impl.ClassPathMatchingPatternResolver;
import cn.shaviation.autotest.model.MethodModel;
import cn.shaviation.autotest.model.Parameter;
import cn.shaviation.autotest.model.TestDataDef;
import cn.shaviation.autotest.model.TestDataEntry;
import cn.shaviation.autotest.model.TestDataGroup;
import cn.shaviation.autotest.model.TestDataHelper;
import cn.shaviation.autotest.model.TestElement.Status;
import cn.shaviation.autotest.model.TestExecution;
import cn.shaviation.autotest.model.TestExecutionHelper;
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
	private String logPath;
	private String charset;
	private PathPatternResolver resolver;
	private Map<Class<?>, Object> testObjects = new HashMap<Class<?>, Object>();

	public TestRunner(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if ("-r".equals(args[i])) {
				recursive = true;
			} else if ("-s".equals(args[i])) {
				silent = true;
			} else if ("-l".equals(args[i])) {
				logPath = args[++i];
			} else if ("-c".equals(args[i])) {
				charset = args[++i];
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
		resolver = new ClassPathMatchingPatternResolver();
	}

	public TestRunner(List<String> resources, boolean recursive,
			boolean silent, String logPath, String charset,
			ClassLoader classLoader) {
		this.resources = resources;
		this.recursive = recursive;
		this.silent = silent;
		this.logPath = logPath;
		this.charset = charset;
		this.resolver = classLoader != null ? new ClassPathMatchingPatternResolver(
				classLoader) : new ClassPathMatchingPatternResolver();
	}

	private Set<String> resolveResources() throws IOException {
		Set<String> locations = new LinkedHashSet<String>();
		for (String resource : resources) {
			if (resource.endsWith("/")) {
				if (recursive) {
					resource += "**/";
				}
				resource += "*." + AutoTest.TEST_SCRIPT_FILE_EXTENSION;
				for (String location : resolver.resolve(resource)) {
					locations.add(location);
				}
			} else {
				locations.add(resource);
			}
		}
		return locations;
	}

	private URL getResource(String resource) throws IOException {
		resource = Strings.cleanPath(resource);
		if (resource.startsWith("/")) {
			resource = resource.substring(1);
		}
		URL url = resolver.getClassLoader().getResource(resource);
		if (url == null) {
			throw new FileNotFoundException("Class path resource \"" + resource
					+ "\" not found");
		}
		return url;
	}

	public void run() throws Exception {
		TestContextImpl context = new TestContextImpl();
		TestNodeImpl testNode = context.getTestExecution();
		testNode.setName("Root");
		context.setTestNode(testNode);
		testNode.start();
		try {
			testNode.addAll(resolveResources());
			for (TestNodeImpl child : testNode.getChildren()) {
				context.setTestNode(child);
				child.start();
				try {
					run(child.getName(), context);
				} catch (Throwable t) {
					child.error(t);
				}
			}
			testNode.complete();
		} catch (Throwable t) {
			testNode.error(t);
		} finally {
			context.setTestNode(testNode);
		}
		testNode.printOut();
		if (!Strings.isBlank(logPath)) {
			saveLog(testNode);
		}
	}

	private void saveLog(TestExecution testExecution) throws IOException {
		Date now = new Date();
		File path = new File(logPath.trim(), Strings.formatYMD(now));
		if (!path.exists()) {
			path.mkdirs();
		}
		if (path.exists()) {
			File file = new File(path, Strings.formatHMS(now) + "."
					+ AutoTest.TEST_RESULT_FILE_EXTENSION);
			FileOutputStream fos = new FileOutputStream(file);
			OutputStreamWriter writer = charset != null ? new OutputStreamWriter(
					fos, charset) : new OutputStreamWriter(fos);
			writer.write(TestExecutionHelper.serialize(testExecution));
			writer.flush();
			writer.close();
		}
	}

	private void run(String resource, TestContextImpl context) throws Throwable {
		System.out.println("Run test script: " + resource);
		runTestScript("", resource, context);
	}

	private void runTestScript(String prefix, String resource,
			TestContextImpl context) throws Throwable {
		final TestNodeImpl testNode = context.getTestNode();
		String json = IOUtils.toString(getResource(resource).openStream(),
				"utf-8");
		TestScript testScript = TestScriptHelper.parse(json);
		context.setTestScript(testScript);
		String nodeName = testScript.getName();
		if (Strings.isBlank(nodeName)) {
			nodeName = resource;
		} else {
			nodeName = nodeName.trim() + "(" + resource + ")";
		}
		System.out.println("Starting \"" + nodeName + "\" >>>");
		if (!Strings.isEmpty(prefix)) {
			nodeName = prefix + ": " + nodeName;
		}
		testNode.setName(nodeName);
		final List<TestStep> testSteps = testScript.getTestSteps();
		if (testSteps != null) {
			final Map<String, Integer> map = new HashMap<String, Integer>();
			TestStepIterator.iterate(testSteps, new ITestStepVisitor() {
				@Override
				public boolean visit(TestStep testStep, int index) {
					String nodeName = "Step " + index;
					testNode.add(nodeName);
					map.put(nodeName, index - 1);
					return true;
				}
			});
			for (int i = 0; i < testNode.getChildren().size(); i++) {
				TestNodeImpl child = testNode.getChildren().get(i);
				context.setTestStepIndex(map.get(child.getName()));
				TestStep testStep = context.getTestStep();
				if (testStep.getDependentSteps() != null
						&& !testStep.getDependentSteps().isEmpty()) {
					Set<Integer> set = new HashSet<Integer>(
							testStep.getDependentSteps());
					for (int j = i - 1; j >= 0; j--) {
						TestNodeImpl check = testNode.getChildren().get(j);
						String pf = check.getName().substring(0,
								check.getName().indexOf(':'));
						if (set.remove(map.get(pf) + 1)) {
							if (check.getStatus() == Status.PASS) {
								if (set.isEmpty()) {
									break;
								}
							} else {
								child.block();
								break;
							}
						}
					}
					if (child.getStatus() == Status.BLOCKED) {
						continue;
					}
				}
				context.setTestNode(child);
				child.start();
				try {
					System.out.println("Go " + child.getName().toLowerCase()
							+ "... "
							+ (testNode.total() - testNode.count(null)) + "/"
							+ testNode.total());
					if (testStep.getParameters() != null) {
						for (Parameter param : testStep.getParameters()) {
							if (!Strings.isEmpty(param.getKey())) {
								context.put(param.getKey(), param.getValue());
							}
						}
					}
					switch (testStep.getInvokeType()) {
					case Method:
						runTestMethod(child.getName(), testStep, context);
						break;
					case Script:
						runSubScript(child.getName(), testStep, context);
						break;
					default:
						child.error("Unknow test step type: "
								+ testStep.getInvokeType());
						break;
					}
				} catch (Throwable t) {
					child.error(t);
				}
			}
		}
		context.setTestNode(testNode);
		testNode.complete();
		System.out.println("<<< Completed");
	}

	private void runTestMethod(String prefix, TestStep testStep,
			TestContextImpl context) throws Throwable {
		TestNodeImpl testNode = context.getTestNode();
		String method = testStep.getInvokeTarget().trim();
		int p = method.lastIndexOf('#');
		Class<?> testClass = Class.forName(method.substring(0, p));
		Method testMethod = testClass.getMethod(method.substring(p + 1),
				MethodModel.class);
		TestMethod annotation = testMethod.getAnnotation(TestMethod.class);
		String nodeName = annotation != null ? annotation.value() : null;
		if (Strings.isBlank(nodeName)) {
			nodeName = method;
		} else {
			nodeName = nodeName.trim() + "(" + method + ")";
		}
		String msg = "Invoke method \"" + nodeName + "\" "
				+ testStep.getLoopTimes() + " times";
		if (!Strings.isEmpty(prefix)) {
			nodeName = prefix + ": " + nodeName;
		}
		testNode.setName(nodeName);
		TestDataDef testDataDef = null;
		if (!Strings.isBlank(testStep.getTestDataFile())) {
			String json = IOUtils
					.toString(getResource(testStep.getTestDataFile().trim())
							.openStream(), "utf-8");
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
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (int i = 0; i < testStep.getLoopTimes(); i++) {
			if (testDataDef != null && testDataDef.getDataList() != null
					&& !testDataDef.getDataList().isEmpty()) {
				for (int j = 0; j < testDataDef.getDataList().size(); j++) {
					String loop = "Loop " + (i + 1) + " group " + (j + 1);
					testNode.add(loop);
					map.put(loop, j);
				}
			} else {
				testNode.add("Loop " + (i + 1));
			}
		}
		for (TestNodeImpl child : testNode.getChildren()) {
			context.setTestNode(child);
			child.start();
			System.out.println(child.getName());
			try {
				Integer i = map.get(child.getName());
				invokeTestMethod(testClass, testMethod, i != null ? testDataDef
						.getDataList().get(i) : null, context);
				if (child.getStatus() == Status.PASS) {
					System.out.println(" - Pass");
				} else if (child.getStatus() == Status.FAILURE) {
					System.out.println(" - Failure");
				} else {
					System.out.println();
				}
			} catch (Throwable t) {
				System.out.println(" - Error");
				child.error(t);
			}
		}
		testNode.complete(true);
	}

	private void invokeTestMethod(Class<?> testClass, Method testMethod,
			TestDataGroup testDataGroup, TestContextImpl context)
			throws Throwable {
		Object testObject = null;
		if (!Modifier.isStatic(testMethod.getModifiers())) {
			if (testClass.getAnnotation(Singleton.class) != null) {
				testObject = testObjects.get(testClass);
				if (testObject == null) {
					testObject = testClass.newInstance();
					testObjects.put(testClass, testObject);
				}
			} else {
				testObject = testClass.newInstance();
			}
		}
		Map<String, String> inputData = new HashMap<String, String>();
		Map<String, String> outputData = new HashMap<String, String>();
		if (testDataGroup != null && testDataGroup.getEntries() != null) {
			for (TestDataEntry entry : testDataGroup.getEntries()) {
				if (!Strings.isEmpty(entry.getKey())) {
					if (entry.getType() == TestDataEntry.Type.Input) {
						inputData.put(entry.getKey(), entry.getValue());
					} else if (entry.getType() == TestDataEntry.Type.Output) {
						outputData.put(entry.getKey(), entry.getValue());
					}
				}
			}
		}
		MethodModel model = context.createMethodModel(inputData, outputData);
		testMethod.invoke(testObject, model);
		TestNodeImpl testNode = context.getTestNode();
		if (model.isSuccess()) {
			testNode.success(model.getDescription(), model.getSnapshot());
		} else {
			testNode.fail(model.getDescription(), model.getSnapshot());
		}
	}

	private void runSubScript(String prefix, TestStep testStep,
			TestContextImpl context) throws Throwable {
		TestNodeImpl testNode = context.getTestNode();
		String msg = "Execute sub script \""
				+ testStep.getInvokeTarget().trim() + "\" "
				+ testStep.getLoopTimes() + " times";
		System.out.println(msg);
		for (int i = 0; i < testStep.getLoopTimes(); i++) {
			testNode.add("Loop " + (i + 1));
		}
		for (TestNodeImpl child : testNode.getChildren()) {
			context.setTestNode(child);
			child.start();
			System.out.println(child.getName());
			TestScript testScript = context.getTestScript();
			int testStepIndex = context.getTestStepIndex();
			try {
				runTestScript(prefix, testStep.getInvokeTarget().trim(),
						context);
			} catch (Throwable t) {
				child.error(t);
			} finally {
				context.setTestScript(testScript);
				context.setTestStepIndex(testStepIndex);
			}
		}
		testNode.complete(true);
	}
}
