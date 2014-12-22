package cn.shaviation.autotest.webdriver;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;

import cn.shaviation.autotest.runner.TestContext;
import cn.shaviation.autotest.runner.TestContexts;

import com.thoughtworks.selenium.Selenium;

@SuppressWarnings("deprecation")
public abstract class Globals {

	protected static final Map<TestContext, Configuration> configurations = new HashMap<TestContext, Configuration>();
	protected static final Map<TestContext, WebDriver> webdrivers = new HashMap<TestContext, WebDriver>();
	protected static final Map<TestContext, Selenium> seleniums = new HashMap<TestContext, Selenium>();

	public static Configuration configuration() {
		TestContext context = TestContexts.get();
		return context != null ? configurations.get(context) : null;
	}

	public static WebDriver webdriver() {
		TestContext context = TestContexts.get();
		return context != null ? webdrivers.get(context) : null;
	}

	@Deprecated
	public static Selenium selenium() {
		TestContext context = TestContexts.get();
		return context != null ? seleniums.get(context) : null;
	}
}
