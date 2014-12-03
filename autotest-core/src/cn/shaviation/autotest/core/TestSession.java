package cn.shaviation.autotest.core;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.core.IJavaProject;

import cn.shavation.autotest.runner.TestExecution;

public interface TestSession {

	ILaunch getLaunch();

	IJavaProject getProject();

	TestExecution getTestExecution();

	boolean isDone();

	void stop();

	void setListener(ITestSessionListener listener);
}
