package cn.shavation.autotest;

import org.osgi.framework.BundleContext;

import cn.shaviation.autotest.internal.runner.TestRunner;

public class AutoTest {

	public static final String PLUGIN_ID = "cn.shaviation.autotest.runtime"; //$NON-NLS-1$

	public static class Plugin extends org.eclipse.core.runtime.Plugin {

		private static Plugin plugin;

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

		public static Plugin getDefault() {
			return plugin;
		}
	}

	public static void main(String[] args) throws Exception {
		TestRunner runner = new TestRunner(args);
		runner.run();
	}
}
