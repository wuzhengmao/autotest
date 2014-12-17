package cn.shaviation.autotest.internal.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.shavation.autotest.AutoTest;
import cn.shavation.autotest.runner.ISnapshotService;
import cn.shavation.autotest.runner.TestElement.Status;
import cn.shavation.autotest.runner.TestElement.Type;
import cn.shavation.autotest.runner.TestExecution;
import cn.shavation.autotest.runner.TestExecutionHelper;
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
import cn.shaviation.autotest.model.TestScript;
import cn.shaviation.autotest.model.TestScriptHelper;
import cn.shaviation.autotest.model.TestStep;
import cn.shaviation.autotest.model.TestStepIterator;
import cn.shaviation.autotest.model.TestStepIterator.ITestStepVisitor;
import cn.shaviation.autotest.util.IOUtils;
import cn.shaviation.autotest.util.Strings;

public class TestRunner {

	private static final Map<Class<?>, Object> services = new HashMap<Class<?>, Object>();

	private String project;
	private List<String> resources = new ArrayList<String>();
	private String charset;
	private boolean recursive = false;
	private String logPath;
	private String picPath;
	private PathPatternResolver resolver;
	private Map<Class<?>, Object> testObjects = new HashMap<Class<?>, Object>();
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private boolean stop = false;
	private RemoteTestConnector connector;

	@SuppressWarnings("unchecked")
	private static <T> T getService(Class<T> interfaceClass) {
		if (!services.containsKey(interfaceClass)) {
			synchronized (interfaceClass) {
				if (!services.containsKey(interfaceClass)) {
					Object service = null;
					String resource = "META-INF/services/"
							+ interfaceClass.getCanonicalName();
					ClassLoader cl = interfaceClass.getClassLoader();
					URL url = cl.getResource(resource);
					if (url != null) {
						try {
							String implClass = new BufferedReader(
									new InputStreamReader(url.openStream()))
									.readLine();
							if (!Strings.isBlank(implClass)) {
								service = Class.forName(implClass.trim(), true,
										cl).newInstance();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					services.put(interfaceClass, service);
					if (service == null) {
						System.err.println("Cannot find service: " + resource);
					}
				}
			}
		}
		return (T) services.get(interfaceClass);
	}

	public TestRunner(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if ("-j".equals(args[i])) {
				project = Strings.decodeUrl(args[++i]);
			} else if ("-r".equals(args[i])) {
				recursive = true;
			} else if ("-c".equals(args[i])) {
				charset = args[++i];
			} else if ("-l".equals(args[i])) {
				logPath = Strings.decodeUrl(args[++i]);
			} else if ("-s".equals(args[i])) {
				picPath = Strings.decodeUrl(args[++i]);
			} else if ("-p".equals(args[i])) {
				++i;
			} else if (args[i].startsWith("-")) {
				throw new IllegalArgumentException(
						"Unknown commond line switch: " + args[i]);
			} else {
				resources.add(Strings.decodeUrl(args[i]));
			}
		}
		resolver = new ClassPathMatchingPatternResolver();
		validateArgs();
	}

	public TestRunner(List<String> resources, String charset,
			boolean recursive, String logPath, String picPath,
			ClassLoader classLoader) {
		this.resources = resources;
		this.charset = charset;
		this.recursive = recursive;
		this.logPath = logPath;
		this.picPath = picPath;
		this.resolver = classLoader != null ? new ClassPathMatchingPatternResolver(
				classLoader) : new ClassPathMatchingPatternResolver();
		validateArgs();
	}

	private void validateArgs() {
		if (!Strings.isEmpty(charset)) {
			if (!Charset.isSupported(charset)) {
				throw new IllegalArgumentException("Unsupport charset: "
						+ charset);
			}
		} else {
			charset = Charset.defaultCharset().name();
		}
		if (resources.isEmpty()) {
			throw new IllegalArgumentException("No test script found");
		}
	}

	private Set<String> resolveResources() throws IOException {
		Set<String> locations = new LinkedHashSet<String>();
		for (String resource : resources) {
			if (resolver.getPathMatcher().isPattern(resource)) {
				for (String location : resolver.resolve(resource)) {
					locations.add(location);
				}
			} else if (resource.endsWith("/")) {
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

	private void fireNodeAdd(TestNodeImpl testNode, TestNodeImpl parent) {
		if (connector != null) {
			try {
				connector.sendNodeAdd(testNode.getId(), testNode.getName(),
						testNode.getType(), parent != null ? parent.getId()
								: null);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	private void fireNodeUpdate(TestNodeImpl testNode) {
		if (connector != null) {
			try {
				connector.sendNodeUpdate(testNode.getId(), testNode.getName(),
						testNode.getRunTime(), testNode.getStatus(),
						testNode.getDescription(), testNode.getSnapshot());
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	public void run() throws IOException {
		TestContextImpl context = new TestContextImpl(this);
		TestExecutionImpl execution = context.getTestExecution();
		context.setTestNode(execution);
		saveArgs(execution);
		execution.start();
		fireNodeAdd(execution, null);
		try {
			for (String resource : resolveResources()) {
				fireNodeAdd(execution.add(resource, Type.SCRIPT), execution);
			}
			for (TestNodeImpl child : execution.getChildren()) {
				if (stop) {
					break;
				}
				context.setTestNode(child);
				child.start();
				fireNodeUpdate(child);
				System.out.println("Run test script: " + child.getName());
				runTestScript("", child.getName(), context);
			}
			execution.complete();
			fireNodeUpdate(execution);
		} catch (Throwable t) {
			execution.error(t);
			fireNodeUpdate(execution);
		} finally {
			context.setTestNode(execution);
		}
		execution.printOut();
		if (connector != null) {
			connector.close();
		}
		if (!executor.isTerminated()) {
			executor.shutdown();
		}
		if (!Strings.isBlank(logPath)) {
			saveLog(execution);
		}
	}

	private void saveArgs(TestExecutionImpl execution) {
		execution.put(TestExecution.ARG_PROJECT, project);
		execution
				.put(TestExecution.ARG_LOCATION, Strings.merge(resources, ","));
		execution.put(TestExecution.ARG_RECURSIVE, String.valueOf(recursive));
		execution.put(TestExecution.ARG_LOG_PATH, logPath);
		execution.put(TestExecution.ARG_PIC_PATH, picPath);
	}

	private void saveLog(TestExecution testExecution) throws IOException {
		Date now = new Date();
		File path = new File(logPath.trim(), Strings.formatYMD(now));
		if (!path.exists()) {
			if (!path.mkdirs()) {
				System.err.println("Make dirs failed: " + path.getPath());
				return;
			}
		}
		File file = new File(path, Strings.formatHMS(now) + "."
				+ AutoTest.TEST_RESULT_FILE_EXTENSION);
		FileOutputStream fos = new FileOutputStream(file);
		OutputStreamWriter writer = charset != null ? new OutputStreamWriter(
				fos, charset) : new OutputStreamWriter(fos);
		TestExecutionHelper.serialize(writer, testExecution);
	}

	private void runTestScript(String prefix, String resource,
			TestContextImpl context) throws IOException {
		InputStreamReader reader = new InputStreamReader(getResource(resource)
				.openStream(), charset);
		TestScript testScript = TestScriptHelper.parse(reader);
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
		TestNodeImpl testNode = context.getTestNode();
		testNode.setName(nodeName);
		fireNodeUpdate(testNode);
		runTestScript(testScript, context);
	}

	private void runTestScript(TestScript testScript, TestContextImpl context) {
		TestScript backTestScript = context.getTestScript();
		int backTestStepIndex = context.getTestStepIndex();
		final TestNodeImpl testNode = context.getTestNode();
		try {
			context.setTestScript(testScript);
			final List<TestStep> testSteps = testScript.getTestSteps();
			if (testSteps != null) {
				final Map<String, Integer> map = new HashMap<String, Integer>();
				TestStepIterator.iterate(testSteps, new ITestStepVisitor() {
					@Override
					public boolean visit(TestStep testStep, int index) {
						String nodeName = "Step " + index;
						Type type = null;
						switch (testStep.getInvokeType()) {
						case Method:
							type = Type.METHOD;
							break;
						case Script:
							type = Type.SCRIPT;
							break;
						}
						fireNodeAdd(testNode.add(nodeName, type), testNode);
						map.put(nodeName, index - 1);
						return true;
					}
				});
				for (int i = 0; i < testNode.getChildren().size(); i++) {
					TestNodeImpl child = testNode.getChildren().get(i);
					context.setTestStepIndex(map.get(child.getName()));
					if (stop) {
						break;
					}
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
									fireNodeUpdate(child);
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
					fireNodeUpdate(child);
					try {
						System.out.println("Go "
								+ child.getName().toLowerCase() + "... "
								+ (testNode.total() - testNode.count(null))
								+ "/" + testNode.total());
						if (testStep.getParameters() != null) {
							for (Parameter param : testStep.getParameters()) {
								if (!Strings.isEmpty(param.getKey())) {
									context.put(param.getKey(),
											param.getValue());
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
							fireNodeUpdate(child);
							break;
						}
					} catch (Throwable t) {
						child.error(t);
						fireNodeUpdate(child);
					}
				}
			}
			testNode.complete();
			fireNodeUpdate(testNode);
			System.out.println("<<< Completed");
		} catch (Throwable t) {
			testNode.error(t);
			fireNodeUpdate(testNode);
		} finally {
			context.setTestNode(testNode);
			context.setTestScript(backTestScript);
			context.setTestStepIndex(backTestStepIndex);
		}
	}

	private void runTestMethod(String prefix, TestStep testStep,
			TestContextImpl context) throws ClassNotFoundException,
			NoSuchMethodException, IOException {
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
		fireNodeUpdate(testNode);
		TestDataDef testDataDef = null;
		if (!Strings.isBlank(testStep.getTestDataFile())) {
			String resource = testStep.getTestDataFile().trim();
			InputStreamReader reader = new InputStreamReader(getResource(
					resource).openStream(), charset);
			testDataDef = TestDataHelper.parse(reader);
			msg += " with "
					+ (testDataDef.getDataList() != null ? testDataDef
							.getDataList().size() : 0)
					+ " group(s) test data \""
					+ (!Strings.isBlank(testDataDef.getName()) ? testDataDef
							.getName() : resource) + "\"";
		}
		System.out.println(msg);
		Map<String, Integer> map = new LinkedHashMap<String, Integer>();
		for (int i = 0; i < testStep.getLoopTimes(); i++) {
			if (testDataDef != null && testDataDef.getDataList() != null
					&& !testDataDef.getDataList().isEmpty()) {
				for (int j = 0; j < testDataDef.getDataList().size(); j++) {
					String loop = "Loop " + (i + 1) + " group " + (j + 1);
					map.put(loop, j);
				}
			} else {
				map.put("Loop " + (i + 1), null);
			}
		}
		if (map.size() > 1) {
			for (String name : map.keySet()) {
				fireNodeAdd(testNode.add(name, Type.LOOP), testNode);
			}
			for (TestNodeImpl child : testNode.getChildren()) {
				if (stop) {
					break;
				}
				context.setTestNode(child);
				child.start();
				fireNodeUpdate(child);
				Integer i = map.get(child.getName());
				loopTestMethod(child.getName(), testClass, testMethod,
						i != null ? testDataDef.getDataList().get(i) : null,
						context);
			}
			context.setTestNode(testNode);
			testNode.complete();
			fireNodeUpdate(testNode);
		} else if (map.size() == 1) {
			if (!stop) {
				String loop = map.keySet().iterator().next();
				Integer i = map.get(loop);
				loopTestMethod(loop, testClass, testMethod,
						i != null ? testDataDef.getDataList().get(i) : null,
						context);
			}
		} else {
			testNode.error("Loop times shall larger than 0");
			fireNodeUpdate(testNode);
		}
	}

	private void loopTestMethod(String loop, final Class<?> testClass,
			final Method testMethod, final TestDataGroup testDataGroup,
			final TestContextImpl context) {
		TestNodeImpl testNode = context.getTestNode();
		System.out.println(loop);
		try {
			executor.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					invokeTestMethod(testClass, testMethod, testDataGroup,
							context);
					return null;
				}
			}).get();
			if (testNode.getStatus() == Status.PASS) {
				System.out.println(" - Pass");
			} else if (testNode.getStatus() == Status.FAILURE) {
				System.out.println(" - Failure");
			} else {
				testNode.error("Unexpected case");
				fireNodeUpdate(testNode);
			}
		} catch (ExecutionException e) {
			System.out.println(" - Error");
			testNode.error(e.getCause());
			fireNodeUpdate(testNode);
		} catch (InterruptedException e) {
			System.out.println(" - Stopped");
			testNode.stop();
			fireNodeUpdate(testNode);
		} catch (Throwable t) {
			System.out.println(" - Error");
			testNode.error(t);
			fireNodeUpdate(testNode);
		}
	}

	private void invokeTestMethod(Class<?> testClass, Method testMethod,
			TestDataGroup testDataGroup, TestContextImpl context)
			throws Exception {
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
		fireNodeUpdate(testNode);
	}

	private void runSubScript(String prefix, TestStep testStep,
			TestContextImpl context) throws IOException {
		TestNodeImpl testNode = context.getTestNode();
		String resource = testStep.getInvokeTarget().trim();
		InputStreamReader reader = new InputStreamReader(getResource(resource)
				.openStream(), charset);
		TestScript testScript = TestScriptHelper.parse(reader);
		String nodeName = testScript.getName();
		if (Strings.isBlank(nodeName)) {
			nodeName = resource;
		} else {
			nodeName = nodeName.trim() + "(" + resource + ")";
		}
		String msg = "Execute sub script \"" + nodeName + "\" "
				+ testStep.getLoopTimes() + " times";
		System.out.println(msg);
		if (!Strings.isEmpty(prefix)) {
			nodeName = prefix + ": " + nodeName;
		}
		testNode.setName(nodeName);
		fireNodeUpdate(testNode);
		if (testStep.getLoopTimes() > 1) {
			for (int i = 0; i < testStep.getLoopTimes(); i++) {
				fireNodeAdd(testNode.add("Loop " + (i + 1), Type.LOOP),
						testNode);
			}
			for (TestNodeImpl child : testNode.getChildren()) {
				if (stop) {
					break;
				}
				context.setTestNode(child);
				child.start();
				System.out.println(child.getName());
				runTestScript(testScript, context);
			}
			context.setTestNode(testNode);
			testNode.complete();
			fireNodeUpdate(testNode);
		} else if (testStep.getLoopTimes() == 1) {
			if (!stop) {
				System.out.println("Loop 1");
				runTestScript(testScript, context);
			}
		} else {
			testNode.error("Loop times shall larger than 0");
			fireNodeUpdate(testNode);
		}
	}

	public File takeSnapshot(TestContextImpl context) {
		if (Strings.isBlank(picPath)) {
			System.err.println("Snapshot path not specified");
			return null;
		}
		ISnapshotService service = getService(ISnapshotService.class);
		if (service == null) {
			return null;
		}
		File path = new File(picPath.trim());
		if (!path.exists()) {
			if (!path.mkdirs()) {
				System.err.println("Make dirs failed: " + path.getPath());
				return null;
			}
		}
		String suffix = service.type();
		if (suffix != null) {
			suffix = "." + suffix;
		}
		try {
			File file = File.createTempFile("TSS", suffix, path);
			IOUtils.saveFile(service.take(context), file);
			return file;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void stop() {
		stop = true;
		executor.shutdownNow();
	}

	public void setConnector(RemoteTestConnector connector) {
		this.connector = connector;
	}
}
