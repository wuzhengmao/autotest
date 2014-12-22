package cn.shaviation.autotest.runner.spi;

public interface ILoggerProvider {

	Logger getLogger(String name);

	Logger getLogger(Class<?> klass);
}
