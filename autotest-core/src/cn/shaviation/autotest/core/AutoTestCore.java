package cn.shaviation.autotest.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.osgi.framework.BundleContext;

import cn.shavation.autotest.AutoTest;
import cn.shaviation.autotest.core.internal.launching.AutoTestLaunchListener;

public class AutoTestCore extends Plugin {

	public static final String PLUGIN_ID = "cn.shaviation.autotest.core"; //$NON-NLS-1$
	public static final String NATURE_ID = "cn.shaviation.autotest.core.nature"; //$NON-NLS-1$
	public static final String BUILDER_ID = "cn.shaviation.autotest.core.builder"; //$NON-NLS-1$
	public static final String LAUNCH_CONFIG_TYPE = "cn.shaviation.autotest.launching.AutoTestLaunchConfigType"; //$NON-NLS-1$
	public static final String TEST_DATA_FILE_EXTENSION = "tdd"; //$NON-NLS-1$
	public static final String TEST_SCRIPT_FILE_EXTENSION = AutoTest.TEST_SCRIPT_FILE_EXTENSION;
	public static final String TEST_RESULT_FILE_EXTENSION = AutoTest.TEST_RESULT_FILE_EXTENSION;
	public static final String DEFAULT_RESOURCE_FOLDER = "resources"; //$NON-NLS-1$
	public static final String DEFAULT_LOG_FOLDER = "logs"; //$NON-NLS-1$

	public static final QualifiedName TEST_DATA_NAME_KEY = new QualifiedName(
			PLUGIN_ID, "testDataName");
	public static final QualifiedName TEST_SCRIPT_NAME_KEY = new QualifiedName(
			PLUGIN_ID, "testScriptName");

	public static final String LAUNCH_CONFIG_ATTR_LOCATION = "cn.shaviation.autotest.launching.ATTR_LOCATION";
	public static final String LAUNCH_CONFIG_ATTR_RECURSIVE = "cn.shaviation.autotest.launching.ATTR_RECURSIVE";
	public static final String LAUNCH_CONFIG_ATTR_LOG_PATH = "cn.shaviation.autotest.launching.ATTR_LOG_PATH";
	public static final String LAUNCH_CONFIG_ATTR_PORT = "cn.shaviation.autotest.launching.PORT";

	private static AutoTestCore plugin;

	private AutoTestLaunchListener listener = new AutoTestLaunchListener();
	private List<ITestLaunchListener> listeners = new ArrayList<ITestLaunchListener>();
	private TestRunSession session;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		ILaunchManager launchManager = DebugPlugin.getDefault()
				.getLaunchManager();
		launchManager.addLaunchListener(listener);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			ILaunchManager launchManager = DebugPlugin.getDefault()
					.getLaunchManager();
			launchManager.removeLaunchListener(listener);
		} finally {
			plugin = null;
			super.stop(context);
		}
	}

	public static AutoTestCore getDefault() {
		return plugin;
	}

	public static <T> T getAdapter(Object sourceObject, Class<T> adapterType) {
		Assert.isNotNull(adapterType);
		if (sourceObject == null) {
			return null;
		}
		if (adapterType.isInstance(sourceObject)) {
			return adapterType.cast(sourceObject);
		}
		if ((sourceObject instanceof IAdaptable)) {
			IAdaptable adaptable = (IAdaptable) sourceObject;
			Object result = adaptable.getAdapter(adapterType);
			if (result != null) {
				Assert.isTrue(adapterType.isInstance(result));
				return adapterType.cast(result);
			}
		}
		if (!(sourceObject instanceof PlatformObject)) {
			Object result = Platform.getAdapterManager().getAdapter(
					sourceObject, adapterType);
			if (result != null) {
				return adapterType.cast(result);
			}
		}
		return null;
	}

	public static void addTestLaunchListener(ITestLaunchListener listener) {
		if (plugin != null) {
			if (!plugin.listeners.contains(listener)) {
				plugin.listeners.add(listener);
			}
		}
	}

	public static void removeTestLaunchListener(ITestLaunchListener listener) {
		if (plugin != null) {
			plugin.listeners.remove(listener);
		}
	}

	public static TestRunSession getTestSession() {
		return plugin != null ? plugin.session : null;
	}

	public static void setTestRunSession(TestRunSession session) {
		plugin.session = session;
		for (ITestLaunchListener listener : plugin.listeners) {
			listener.onLaunch(session);
		}
	}
}
