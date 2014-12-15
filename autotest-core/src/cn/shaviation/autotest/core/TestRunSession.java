package cn.shaviation.autotest.core;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.core.IJavaProject;

import cn.shavation.autotest.runner.ITestSessionListener;
import cn.shavation.autotest.runner.TestExecution;
import cn.shavation.autotest.runner.TestSession;

public class TestRunSession implements TestSession {

	private ILaunch launch;
	private IJavaProject project;
	private TestSession session;

	public TestRunSession(ILaunch launch, IJavaProject project,
			TestSession session) {
		this.launch = launch;
		this.project = project;
		this.session = session;
	}

	public ILaunch getLaunch() {
		return launch;
	}

	public IJavaProject getProject() {
		return project;
	}

	@Override
	public TestExecution getTestExecution() {
		return session.getTestExecution();
	}

	@Override
	public boolean isDone() {
		return session.isDone();
	}

	@Override
	public void stop() {
		session.stop();
	}

	@Override
	public void setListener(ITestSessionListener listener) {
		session.setListener(listener);
	}
}
