package cn.shaviation.autotest.webdriver.internal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import cn.shavation.autotest.runner.TestContext;
import cn.shavation.autotest.runner.spi.ISnapshotService;

public class SnapshotServiceImpl implements ISnapshotService {

	@Override
	public String type() {
		return "png";
	}

	@Override
	public InputStream take(TestContext testContext) throws Exception {
		TakesScreenshot service = (TakesScreenshot) GlobalsBinder
				.webdriver(testContext);
		byte[] bytes = service.getScreenshotAs(OutputType.BYTES);
		return new ByteArrayInputStream(bytes);
	}
}
