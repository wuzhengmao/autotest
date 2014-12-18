package cn.shavation.autotest.runner.spi;

import cn.shavation.autotest.runner.TestContext;

public interface IBootstrapService {

	void prepare(TestContext testContext) throws Exception;

	void cleanup(TestContext testContext) throws Exception;
}
