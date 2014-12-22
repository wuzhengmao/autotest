package cn.shaviation.autotest.runner.spi;

public abstract class LoggerFactory {

	private static final ILoggerProvider provider;
	private static final Logger NONE = new Logger() {

		@Override
		public String getName() {
			return null;
		}

		@Override
		public boolean isTraceEnabled() {
			return false;
		}

		@Override
		public void trace(String message) {

		}

		@Override
		public boolean isDebugEnabled() {
			return false;
		}

		@Override
		public void debug(String message) {

		}

		@Override
		public boolean isInfoEnabled() {
			return false;
		}

		@Override
		public void info(String message) {

		}

		@Override
		public boolean isWarnEnabled() {
			return false;
		}

		@Override
		public void warn(String message) {

		}

		@Override
		public void warn(String message, Throwable t) {

		}

		@Override
		public boolean isErrorEnabled() {
			return false;
		}

		@Override
		public void error(String message) {

		}

		@Override
		public void error(String message, Throwable t) {

		}
	};

	static {
		provider = ServiceLocator.getService(ILoggerProvider.class);
	}

	public static Logger getLogger(String name) {
		Logger logger = null;
		if (provider != null) {
			logger = provider.getLogger(name);
		}
		return logger != null ? logger : NONE;
	}

	public static Logger getLogger(Class<?> klass) {
		Logger logger = null;
		if (provider != null) {
			logger = provider.getLogger(klass);
		}
		return logger != null ? logger : NONE;
	}
}
