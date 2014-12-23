package cn.shaviation.autotest.runner.spi;

import cn.shaviation.autotest.runner.TestContext;

public interface IResourceInterceptor<T> {

	Class<T> support();

	boolean intercept(TestContext testContext, String location,
			Payload<T> origin, Payload<T> current);
}
