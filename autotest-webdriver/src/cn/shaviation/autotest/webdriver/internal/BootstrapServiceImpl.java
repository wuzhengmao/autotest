package cn.shaviation.autotest.webdriver.internal;

import java.io.IOException;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import cn.shavation.autotest.runner.TestContext;
import cn.shavation.autotest.runner.spi.IBootstrapService;
import cn.shaviation.autotest.util.Strings;
import cn.shaviation.autotest.webdriver.Configuration;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;

@SuppressWarnings("deprecation")
public class BootstrapServiceImpl implements IBootstrapService {

	private ConfigurationImpl configuration;
	private WebDriver webdriver;
	private Selenium selenium;

	@Override
	public void prepare(TestContext testContext) throws Exception {
		loadConfiguration(testContext);
		initWebdriver(testContext);
		initSelenium(testContext);
		GlobalsBinder.bind(testContext, configuration);
		GlobalsBinder.bind(testContext, webdriver);
		GlobalsBinder.bind(testContext, selenium);
	}

	private void loadConfiguration(TestContext testContext) throws IOException {
		configuration = new ConfigurationImpl("configuration.properties");
		configuration.init();
	}

	private void initWebdriver(TestContext testContext) {
		String adapter = configuration.get(Configuration.WEBDRIVER_ADAPTER);
		adapter = !Strings.isBlank(adapter) ? adapter.trim() : "firefox";
		if ("firefox".equalsIgnoreCase(adapter)) {
			transferSystemProperty(configuration,
					Configuration.WEBDRIVER_FIREFOX_BIN);
			webdriver = new FirefoxDriver();
		} else if ("chrome".equalsIgnoreCase(adapter)) {
			transferSystemProperty(configuration,
					Configuration.WEBDRIVER_CHROME_DRIVER);
			webdriver = new ChromeDriver();
		} else if ("ie".equalsIgnoreCase(adapter)) {
			transferSystemProperty(configuration,
					Configuration.WEBDRIVER_IE_DRIVER);
			webdriver = new InternetExplorerDriver();
		} else if ("html".equalsIgnoreCase(adapter)) {
			webdriver = new HtmlUnitDriver(true);
		} else {
			throw new RuntimeException("Unsupported webdriver adapter: "
					+ adapter);
		}
		webdriver.manage().window().maximize();
	}

	private void initSelenium(TestContext testContext) {
		String baseurl = configuration.get(Configuration.SELENIUM_BASEURL);
		if (Strings.isBlank(baseurl)) {
			throw new RuntimeException("Miss configuration: "
					+ Configuration.SELENIUM_BASEURL);
		}
		selenium = new WebDriverBackedSelenium(webdriver, baseurl);
	}

	private void transferSystemProperty(Configuration configuration, String key) {
		String value = configuration.get(key);
		if (!Strings.isBlank(value)) {
			System.setProperty(key, value.trim());
		}
	}

	@Override
	public void cleanup(TestContext testContext) throws Exception {
		GlobalsBinder.unbind(testContext);
		webdriver.quit();
	}
}
