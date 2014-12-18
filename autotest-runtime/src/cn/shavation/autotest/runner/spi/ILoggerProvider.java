package cn.shavation.autotest.runner.spi;

public interface ILoggerProvider {

	Logger getLogger(String name);

	Logger getLogger(Class<?> klass);
}
