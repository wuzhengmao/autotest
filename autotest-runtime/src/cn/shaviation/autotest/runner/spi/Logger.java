package cn.shaviation.autotest.runner.spi;

public interface Logger {

	String getName();

	boolean isTraceEnabled();

	void trace(String message);

	boolean isDebugEnabled();

	void debug(String message);

	boolean isInfoEnabled();

	void info(String message);

	boolean isWarnEnabled();

	void warn(String message);

	void warn(String message, Throwable t);

	boolean isErrorEnabled();

	void error(String message);

	void error(String message, Throwable t);
}
