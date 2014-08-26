package cn.shaviation.autotest.core.jdt;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaProject;

import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.core.model.TestDataDef;
import cn.shaviation.autotest.core.model.TestDataHelper;
import cn.shaviation.autotest.core.model.TestScript;
import cn.shaviation.autotest.core.model.TestScriptHelper;
import cn.shaviation.autotest.core.util.IOUtils;
import cn.shaviation.autotest.core.util.Strings;

public abstract class AutoTestProjects {

	public static Map<String, String> searchTestDataFiles(
			IJavaProject javaProject) throws CoreException {
		final Map<String, String> result = new LinkedHashMap<String, String>();
		NonJavaResourceFinder.search(javaProject,
				AutoTestCore.TEST_DATA_FILE_EXTENSION,
				new INonJavaResourceVisitor() {

					@Override
					public boolean visit(String path, IFile resource)
							throws CoreException {
						result.put(path, getTestDataName(resource));
						return true;
					}

					@Override
					public boolean visit(String path, IJarEntryResource resource)
							throws CoreException {
						result.put(path, getTestDataName(resource));
						return true;
					}
				}, null);
		return result;
	}

	public static String getTestDataName(IJavaProject javaProject,
			String location) {
		try {
			Object resource = NonJavaResourceFinder.lookup(javaProject,
					location, null);
			if (resource instanceof IResource) {
				return getTestDataName((IResource) resource);
			} else if (resource instanceof IJarEntryResource) {
				return getTestDataName((IJarEntryResource) resource);
			}
		} catch (CoreException e) {
		}
		return "";
	}

	private static String getTestDataName(IResource resource) {
		String name = null;
		try {
			name = resource
					.getPersistentProperty(AutoTestCore.TEST_DATA_NAME_KEY);
		} catch (CoreException e) {
		}
		if (name == null) {
			name = resource.getName();
		}
		return name;
	}

	private static String getTestDataName(IJarEntryResource resource) {
		try {
			String json = IOUtils.toString(resource.getContents(), "UTF-8");
			if (!Strings.isEmpty(json)) {
				TestDataDef testDataDef = TestDataHelper.parse(json);
				if (!Strings.isBlank(testDataDef.getName())) {
					return testDataDef.getName().trim();
				}
			}
		} catch (Exception e) {
		}
		return resource.getName();
	}

	public static Map<String, String> searchTestScriptFiles(
			IJavaProject javaProject) throws CoreException {
		final Map<String, String> result = new LinkedHashMap<String, String>();
		NonJavaResourceFinder.search(javaProject,
				AutoTestCore.TEST_SCRIPT_FILE_EXTENSION,
				new INonJavaResourceVisitor() {

					@Override
					public boolean visit(String path, IFile resource)
							throws CoreException {
						result.put(path, getTestScriptName(resource));
						return true;
					}

					@Override
					public boolean visit(String path, IJarEntryResource resource)
							throws CoreException {
						result.put(path, getTestScriptName(resource));
						return true;
					}
				}, null);
		return result;
	}

	public static String getTestScriptName(IJavaProject javaProject,
			String location) {
		try {
			Object resource = NonJavaResourceFinder.lookup(javaProject,
					location, null);
			if (resource instanceof IResource) {
				return getTestScriptName((IResource) resource);
			} else if (resource instanceof IJarEntryResource) {
				return getTestScriptName((IJarEntryResource) resource);
			}
		} catch (CoreException e) {
		}
		return "";
	}

	private static String getTestScriptName(IResource resource) {
		String name = null;
		try {
			name = resource
					.getPersistentProperty(AutoTestCore.TEST_SCRIPT_NAME_KEY);
		} catch (CoreException e) {
		}
		if (name == null) {
			name = resource.getName();
		}
		return name;
	}

	private static String getTestScriptName(IJarEntryResource resource) {
		try {
			String json = IOUtils.toString(resource.getContents(), "UTF-8");
			if (!Strings.isEmpty(json)) {
				TestScript testScript = TestScriptHelper.parse(json);
				if (!Strings.isBlank(testScript.getName())) {
					return testScript.getName().trim();
				}
			}
		} catch (Exception e) {
		}
		return resource.getName();
	}
}
