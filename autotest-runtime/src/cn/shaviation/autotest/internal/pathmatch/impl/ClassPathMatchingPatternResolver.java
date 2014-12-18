package cn.shaviation.autotest.internal.pathmatch.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import cn.shavation.autotest.AutoTest;
import cn.shaviation.autotest.internal.pathmatch.PathMatcher;
import cn.shaviation.autotest.internal.pathmatch.PathPatternResolver;
import cn.shaviation.autotest.util.Strings;

public class ClassPathMatchingPatternResolver implements PathPatternResolver {

	private static Method equinoxResolveMethod;

	static {
		// Detect Equinox OSGi (e.g. on WebSphere 6.1)
		try {
			Class<?> fileLocatorClass = ClassPathMatchingPatternResolver.class
					.getClassLoader().loadClass(
							"org.eclipse.core.runtime.FileLocator");
			equinoxResolveMethod = fileLocatorClass.getMethod("resolve",
					new Class[] { URL.class });
		} catch (Throwable ex) {
			equinoxResolveMethod = null;
		}
	}

	private ClassLoader classLoader;
	private PathMatcher pathMatcher = new AntPathMatcher();

	public ClassPathMatchingPatternResolver() {
		this.classLoader = AutoTest.getDefaultClassLoader();
	}

	public ClassPathMatchingPatternResolver(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public void setPathMatcher(PathMatcher pathMatcher) {
		if (pathMatcher == null) {
			throw new NullPointerException("PathMatcher must not be null");
		}
		this.pathMatcher = pathMatcher;
	}

	@Override
	public PathMatcher getPathMatcher() {
		return pathMatcher;
	}

	@Override
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	@Override
	public String[] resolve(String locationPattern) throws IOException {
		if (locationPattern == null) {
			throw new NullPointerException("Location pattern must not be null");
		}
		// a class path resource (multiple resources for same name possible)
		if (getPathMatcher().isPattern(locationPattern)) {
			// a class path resource pattern
			return findPathMatchingResources(locationPattern);
		} else {
			// all class path resources with the given name
			return new String[] { locationPattern };
		}
	}

	protected URL[] findAllClassPathResources(String location)
			throws IOException {
		String path = location;
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		Enumeration<URL> resourceUrls = classLoader.getResources(path);
		Set<URL> result = new LinkedHashSet<URL>(16);
		while (resourceUrls.hasMoreElements()) {
			result.add(resourceUrls.nextElement());
		}
		return result.toArray(new URL[result.size()]);
	}

	protected String[] findPathMatchingResources(String locationPattern)
			throws IOException {
		String rootDirPath = determineRootDir(locationPattern);
		String subPattern = locationPattern.substring(rootDirPath.length());
		URL[] rootDirResources = findAllClassPathResources(rootDirPath);
		Set<String> result = new LinkedHashSet<String>(16);
		for (int i = 0; i < rootDirResources.length; i++) {
			URL rootDirResource = resolveRootDirResource(rootDirResources[i]);
			if (isJarResource(rootDirResource)) {
				result.addAll(doFindPathMatchingJarResources(rootDirResource,
						rootDirPath, subPattern));
			} else {
				result.addAll(doFindPathMatchingFileResources(rootDirResource,
						rootDirPath, subPattern));
			}
		}
		return result.toArray(new String[result.size()]);
	}

	protected String determineRootDir(String location) {
		int rootDirEnd = location.length();
		while (rootDirEnd > 0
				&& getPathMatcher()
						.isPattern(location.substring(0, rootDirEnd))) {
			rootDirEnd = location.lastIndexOf('/', rootDirEnd - 2) + 1;
		}
		return location.substring(0, rootDirEnd);
	}

	protected URL resolveRootDirResource(URL original) {
		if (equinoxResolveMethod != null) {
			if (original.getProtocol().startsWith("bundle")) {
				try {
					return (URL) equinoxResolveMethod.invoke(null, original);
				} catch (RuntimeException e) {
					throw (RuntimeException) e;
				} catch (InvocationTargetException e) {
					Throwable t = e.getTargetException();
					if (t instanceof RuntimeException) {
						throw (RuntimeException) t;
					} else if (t instanceof Error) {
						throw (Error) t;
					} else {
						throw new RuntimeException(e);
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return original;
	}

	protected boolean isJarResource(URL resource) throws IOException {
		String protocol = resource.getProtocol();
		return ("jar".equals(protocol) || "zip".equals(protocol)
				|| "wsjar".equals(protocol) || ("code-source".equals(protocol) && resource
				.getPath().contains("!/")));
	}

	protected Set<String> doFindPathMatchingJarResources(URL rootDirResource,
			String rootDirPath, String subPattern) throws IOException {
		URLConnection con = rootDirResource.openConnection();
		JarFile jarFile = null;
		String jarFileUrl = null;
		String rootEntryPath = null;
		boolean newJarFile = false;

		if (con instanceof JarURLConnection) {
			// Should usually be the case for traditional JAR files.
			JarURLConnection jarCon = (JarURLConnection) con;
			jarCon.setUseCaches(false);
			jarFile = jarCon.getJarFile();
			jarFileUrl = jarCon.getJarFileURL().toExternalForm();
			JarEntry jarEntry = jarCon.getJarEntry();
			rootEntryPath = (jarEntry != null ? jarEntry.getName() : "");
		} else {
			// No JarURLConnection -> need to resort to URL file parsing.
			// We'll assume URLs of the format "jar:path!/entry", with the
			// protocol
			// being arbitrary as long as following the entry format.
			// We'll also handle paths with and without leading "file:" prefix.
			String urlFile = rootDirResource.getFile();
			int separatorIndex = urlFile.indexOf("!/");
			if (separatorIndex != -1) {
				jarFileUrl = urlFile.substring(0, separatorIndex);
				rootEntryPath = urlFile.substring(separatorIndex + 2);
				jarFile = getJarFile(jarFileUrl);
			} else {
				jarFile = new JarFile(urlFile);
				jarFileUrl = urlFile;
				rootEntryPath = "";
			}
			newJarFile = true;
		}

		try {
			if (!"".equals(rootEntryPath) && !rootEntryPath.endsWith("/")) {
				// Root entry path must end with slash to allow for proper
				// matching.
				// The Sun JRE does not return a slash here, but BEA JRockit
				// does.
				rootEntryPath = rootEntryPath + "/";
			}
			Set<String> result = new LinkedHashSet<String>(8);
			for (Enumeration<JarEntry> entries = jarFile.entries(); entries
					.hasMoreElements();) {
				JarEntry entry = entries.nextElement();
				String entryPath = entry.getName();
				if (entryPath.startsWith(rootEntryPath)) {
					String relativePath = entryPath.substring(rootEntryPath
							.length());
					if (getPathMatcher().match(subPattern, relativePath)) {
						result.add(rootDirPath + relativePath);
					}
				}
			}
			return result;
		} finally {
			// Close jar file, but only if freshly obtained -
			// not from JarURLConnection, which might cache the file reference.
			if (newJarFile) {
				jarFile.close();
			}
		}
	}

	protected JarFile getJarFile(String jarFileUrl) throws IOException {
		if (jarFileUrl.startsWith("file:")) {
			try {
				return new JarFile(new URI(Strings.replace(jarFileUrl, " ",
						"%20")).getSchemeSpecificPart());
			} catch (URISyntaxException ex) {
				// Fallback for URLs that are not valid URIs (should hardly ever
				// happen).
				return new JarFile(jarFileUrl.substring(5));
			}
		} else {
			return new JarFile(jarFileUrl);
		}
	}

	protected Set<String> doFindPathMatchingFileResources(URL rootDirResource,
			String rootDirPath, String subPattern) throws IOException {
		if (rootDirResource != null
				&& "file".equals(rootDirResource.getProtocol())) {
			File rootDir;
			try {
				rootDir = new File(rootDirResource.toURI()
						.getSchemeSpecificPart());
			} catch (URISyntaxException ex) {
				// Fallback for URLs that are not valid URIs (should hardly ever
				// happen).
				rootDir = new File(rootDirResource.getFile());
			}
			return doFindMatchingFileSystemResources(rootDir, rootDirPath,
					subPattern);
		} else {
			return Collections.emptySet();
		}
	}

	protected Set<String> doFindMatchingFileSystemResources(File rootDir,
			String rootDirPath, String subPattern) throws IOException {
		if (!rootDir.isDirectory()) {
			throw new IllegalArgumentException("Resource path [" + rootDir
					+ "] does not denote a directory");
		}
		String fullPattern = Strings.replace(rootDir.getAbsolutePath(),
				File.separator, "/");
		if (!subPattern.startsWith("/")) {
			fullPattern += "/";
		}
		fullPattern = fullPattern
				+ Strings.replace(subPattern, File.separator, "/");
		Set<String> result = new LinkedHashSet<String>(8);
		doRetrieveMatchingFiles(fullPattern, rootDir, rootDirPath, result);
		return result;
	}

	protected void doRetrieveMatchingFiles(String fullPattern, File dir,
			String dirPath, Set<String> result) throws IOException {
		File[] dirContents = dir.listFiles();
		if (dirContents == null) {
			throw new IOException("Could not retrieve contents of directory ["
					+ dir.getAbsolutePath() + "]");
		}
		for (int i = 0; i < dirContents.length; i++) {
			File content = dirContents[i];
			String filePath = dirPath + content.getName();
			String currPath = Strings.replace(content.getAbsolutePath(),
					File.separator, "/");
			if (content.isDirectory()
					&& getPathMatcher().matchStart(fullPattern, currPath + "/")) {
				doRetrieveMatchingFiles(fullPattern, content, filePath + "/",
						result);
			}
			if (getPathMatcher().match(fullPattern, currPath)) {
				result.add(filePath);
			}
		}
	}
}
