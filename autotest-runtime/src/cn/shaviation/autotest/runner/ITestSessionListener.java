package cn.shaviation.autotest.runner;


public interface ITestSessionListener {

	void onStart(TestExecution execution);

	void onNodeAdd(TestElement element);

	void onNodeUpdate(TestElement element);

	void onComplete(TestExecution execution);

	void onTerminate(TestExecution execution);
}
