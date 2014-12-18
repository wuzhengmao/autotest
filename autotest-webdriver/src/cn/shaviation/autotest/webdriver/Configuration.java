package cn.shaviation.autotest.webdriver;

import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.ie.InternetExplorerDriverService;

public interface Configuration {

	String WEBDRIVER_ADAPTER = "webdriver.adapter";
	String WEBDRIVER_FIREFOX_BIN = "webdriver.firefox.bin";
	String WEBDRIVER_CHROME_DRIVER = ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY;
	String WEBDRIVER_IE_DRIVER = InternetExplorerDriverService.IE_DRIVER_EXE_PROPERTY;
	String SELENIUM_BASEURL = "selenium.baseurl";

	String get(String key);
}
