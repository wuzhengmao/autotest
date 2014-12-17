package cn.shavation.autotest.runner;

import java.io.InputStream;

public interface ISnapshotService {

	String type();

	InputStream take(TestContext testContext) throws Exception;
}
