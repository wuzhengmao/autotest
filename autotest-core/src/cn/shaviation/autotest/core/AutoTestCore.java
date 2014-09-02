package cn.shaviation.autotest.core;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.QualifiedName;
import org.osgi.framework.BundleContext;

public class AutoTestCore extends Plugin {

	public static final String PLUGIN_ID = "cn.shaviation.autotest.core"; //$NON-NLS-1$
	public static final String NATURE_ID = "cn.shaviation.autotest.core.nature"; //$NON-NLS-1$
	public static final String BUILDER_ID = "cn.shaviation.autotest.core.builder"; //$NON-NLS-1$
	public static final String TEST_DATA_FILE_EXTENSION = "tdd"; //$NON-NLS-1$
	public static final String TEST_SCRIPT_FILE_EXTENSION = "tsc"; //$NON-NLS-1$

	public static final QualifiedName TEST_DATA_NAME_KEY = new QualifiedName(
			PLUGIN_ID, "testDataName");
	public static final QualifiedName TEST_SCRIPT_NAME_KEY = new QualifiedName(
			PLUGIN_ID, "testScriptName");

	private static AutoTestCore plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
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
}
