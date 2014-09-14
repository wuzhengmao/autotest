package cn.shaviation.autotest.internal.pathmatch;

import java.io.IOException;

public interface PathPatternResolver {

	ClassLoader getClassLoader();

	PathMatcher getPathMatcher();

	String[] resolve(String locationPattern) throws IOException;
}
