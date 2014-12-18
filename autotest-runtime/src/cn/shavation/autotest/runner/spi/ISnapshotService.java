package cn.shavation.autotest.runner.spi;

import java.io.InputStream;

import cn.shavation.autotest.runner.TestContext;

public interface ISnapshotService {

	String type();

	InputStream take(TestContext testContext) throws Exception;
}
