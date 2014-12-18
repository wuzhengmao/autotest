package cn.shaviation.autotest.internal.runner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import cn.shavation.autotest.runner.Logger;
import cn.shaviation.autotest.util.Strings;

public abstract class ServiceLocator {

	private static final Logger logger = LoggerFactory
			.getLogger(ServiceLocator.class);
	private static final Map<Class<?>, Object> services = new HashMap<Class<?>, Object>();

	@SuppressWarnings("unchecked")
	public static <T> T getService(Class<T> interfaceClass) {
		if (!services.containsKey(interfaceClass)) {
			synchronized (interfaceClass) {
				if (!services.containsKey(interfaceClass)) {
					Object service = null;
					try {
						String resource = "META-INF/services/"
								+ interfaceClass.getCanonicalName();
						ClassLoader cl = interfaceClass.getClassLoader();
						URL url = cl.getResource(resource);
						if (url != null) {
							String implClass = new BufferedReader(
									new InputStreamReader(url.openStream()))
									.readLine();
							if (!Strings.isBlank(implClass)) {
								service = Class.forName(implClass.trim(), true,
										cl).newInstance();
							}
						}
						if (service == null) {
							logger.warn("Cannot find service: " + resource);
						}
					} catch (Exception e) {
						logger.error("Failed to create service: "
								+ interfaceClass, e);
					}
					services.put(interfaceClass, service);
				}
			}
		}
		return (T) services.get(interfaceClass);
	}
}
