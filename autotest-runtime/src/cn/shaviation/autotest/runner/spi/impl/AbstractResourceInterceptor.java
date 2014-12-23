package cn.shaviation.autotest.runner.spi.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import cn.shaviation.autotest.internal.pathmatch.PathMatcher;
import cn.shaviation.autotest.internal.pathmatch.impl.AntPathMatcher;
import cn.shaviation.autotest.runner.TestContext;
import cn.shaviation.autotest.runner.spi.IResourceInterceptor;
import cn.shaviation.autotest.runner.spi.Payload;

public abstract class AbstractResourceInterceptor<T> implements
		IResourceInterceptor<T> {

	private static final PathMatcher matcher = new AntPathMatcher();

	@SuppressWarnings("unchecked")
	@Override
	public final Class<T> support() {
		Type type = getClass().getGenericSuperclass();
		while (type != null && type != Object.class) {
			if (type instanceof ParameterizedType) {
				return (Class<T>) ((ParameterizedType) type)
						.getActualTypeArguments()[0];
			}
			type = ((Class<?>) type).getGenericSuperclass();
		}
		return null;
	}

	@Override
	public final boolean intercept(TestContext testContext, String location,
			Payload<T> origin, Payload<T> current) {
		if (matcher.match(pattern(), location)) {
			current.set(process(testContext, origin.get()));
			return true;
		}
		return false;
	}

	protected abstract String pattern();

	protected abstract T process(TestContext testContext, T origin);
}
