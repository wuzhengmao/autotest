package cn.shaviation.autotest.runner.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.shaviation.autotest.util.Strings;

public abstract class ServiceLocator {

	private static final Logger logger = LoggerFactory
			.getLogger(ServiceLocator.class);
	private static final Map<Class<?>, List<Object>> serviceCache = new HashMap<Class<?>, List<Object>>();

	@SuppressWarnings("unchecked")
	public static <T> List<T> getServices(Class<T> interfaceClass) {
		if (!serviceCache.containsKey(interfaceClass)) {
			synchronized (interfaceClass) {
				if (!serviceCache.containsKey(interfaceClass)) {
					List<Object> services = new ArrayList<Object>();
					try {
						String resource = "META-INF/services/"
								+ interfaceClass.getCanonicalName();
						ClassLoader cl = interfaceClass.getClassLoader();
						for (URL url : Collections.list(cl
								.getResources(resource))) {
							BufferedReader reader = null;
							try {
								reader = new BufferedReader(
										new InputStreamReader(url.openStream()));
								String implClass;
								while ((implClass = reader.readLine()) != null) {
									implClass = implClass.trim();
									if (!Strings.isEmpty(implClass)) {
										try {
											services.add(Class.forName(
													implClass, true, cl)
													.newInstance());
										} catch (Exception e) {
											logger.error(
													"Failed to create service instance for "
															+ implClass, e);
										}
									}
								}
							} catch (IOException e) {
								logger.error("Failed to read resource: " + url,
										e);
							} finally {
								if (reader != null) {
									try {
										reader.close();
									} catch (IOException e1) {
									}
								}
							}
						}
						if (services.isEmpty()) {
							logger.warn("No service found: " + interfaceClass);
						}
					} catch (Exception e) {
						logger.error("Failed to create services: "
								+ interfaceClass, e);
					}
					serviceCache.put(interfaceClass,
							Collections.unmodifiableList(services));
				}
			}
		}
		return (List<T>) serviceCache.get(interfaceClass);
	}

	public static <T> T getService(Class<T> interfaceClass) {
		List<T> services = getServices(interfaceClass);
		return !services.isEmpty() ? services.get(0) : null;
	}
}
