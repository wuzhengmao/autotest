package cn.shaviation.autotest.runner.spi;

import java.io.InputStream;

import cn.shaviation.autotest.runner.TestContext;

public interface ISnapshotService {

	String type();

	InputStream take(TestContext testContext) throws Exception;
}
