package cn.shaviation.autotest.webdriver.internal;

import org.openqa.selenium.WebDriver;

import cn.shavation.autotest.runner.TestContext;
import cn.shaviation.autotest.webdriver.Configuration;
import cn.shaviation.autotest.webdriver.Globals;

import com.thoughtworks.selenium.Selenium;

@SuppressWarnings("deprecation")
public abstract class GlobalsBinder extends Globals {

	public static Configuration configuration(TestContext context) {
		return configurations.get(context);
	}

	public static WebDriver webdriver(TestContext context) {
		return webdrivers.get(context);
	}

	public static Selenium selenium(TestContext context) {
		return seleniums.get(context);
	}

	/* package */static void bind(TestContext context,
			Configuration configuration) {
		configurations.put(context, configuration);
	}

	/* package */static void bind(TestContext context, WebDriver webdriver) {
		webdrivers.put(context, webdriver);
	}

	/* package */static void bind(TestContext context, Selenium selenium) {
		seleniums.put(context, selenium);
	}

	/* package */static void unbind(TestContext context) {
		configurations.remove(context);
		webdrivers.remove(context);
		seleniums.remove(context);
	}
}
