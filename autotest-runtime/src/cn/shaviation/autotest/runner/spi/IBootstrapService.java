package cn.shaviation.autotest.runner.spi;

import cn.shaviation.autotest.runner.TestContext;

public interface IBootstrapService {

	void prepare(TestContext testContext) throws Exception;

	void cleanup(TestContext testContext) throws Exception;
}
