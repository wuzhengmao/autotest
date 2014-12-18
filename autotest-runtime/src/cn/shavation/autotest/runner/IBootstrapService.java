package cn.shavation.autotest.runner;

public interface IBootstrapService {

	void prepare(TestContext testContext) throws Exception;

	void cleanup(TestContext testContext) throws Exception;
}
