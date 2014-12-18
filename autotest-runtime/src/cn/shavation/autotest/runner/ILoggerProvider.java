package cn.shavation.autotest.runner;

public interface ILoggerProvider {

	Logger getLogger(String name);

	Logger getLogger(Class<?> klass);
}
