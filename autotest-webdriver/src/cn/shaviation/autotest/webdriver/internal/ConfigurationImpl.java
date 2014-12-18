package cn.shaviation.autotest.webdriver.internal;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import cn.shavation.autotest.AutoTest;
import cn.shavation.autotest.runner.spi.Logger;
import cn.shavation.autotest.runner.spi.LoggerFactory;
import cn.shaviation.autotest.webdriver.Configuration;

public class ConfigurationImpl implements Configuration {

	private static final Logger logger = LoggerFactory
			.getLogger(ConfigurationImpl.class);

	private final String location;
	private final Properties properties = new Properties();

	public ConfigurationImpl(String location) {
		this.location = location;
	}

	public void init() throws IOException {
		URL url = AutoTest.getDefaultClassLoader().getResource(location);
		if (url != null) {
			properties.load(url.openStream());
		} else {
			logger.warn("Configuration file not found: " + location);
		}
	}

	@Override
	public String get(String key) {
		return properties.getProperty(key);
	}
}
