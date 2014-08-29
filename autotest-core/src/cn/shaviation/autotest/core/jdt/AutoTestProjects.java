package cn.shaviation.autotest.core.jdt;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeReferenceMatch;

import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.core.annotation.TestMethod;
import cn.shaviation.autotest.core.model.TestDataDef;
import cn.shaviation.autotest.core.model.TestDataHelper;
import cn.shaviation.autotest.core.model.TestScript;
import cn.shaviation.autotest.core.model.TestScriptHelper;
import cn.shaviation.autotest.core.util.IOUtils;
import cn.shaviation.autotest.core.util.Strings;

public abstract class AutoTestProjects {

	public static void searchTestMethods(IJavaProject javaProject,
			SearchRequestor requestor, IProgressMonitor monitor)
			throws CoreException {
		SearchPattern pattern = SearchPattern.createPattern(
				TestMethod.class.getName(), IJavaSearchConstants.TYPE,
				IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
				SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
		IJavaSearchScope scope = SearchEngine
				.createJavaSearchScope(new IJavaElement[] { javaProject });
		SearchEngine engine = new SearchEngine();
		engine.search(pattern, new SearchParticipant[] { SearchEngine
				.getDefaultSearchParticipant() }, scope, requestor, monitor);
	}

	public static IMethod getTestMethod(IJavaProject javaProject,
			String testMethod) {
		try {
			SearchPattern pattern = SearchPattern.createPattern(testMethod
					.replace('$', '.').replace('#', '.'),
					IJavaSearchConstants.METHOD,
					IJavaSearchConstants.DECLARATIONS,
					SearchPattern.R_PATTERN_MATCH
							| SearchPattern.R_CASE_SENSITIVE);
			IJavaSearchScope scope = SearchEngine
					.createJavaSearchScope(new IJavaElement[] { javaProject });
			final IMethod[] result = new IMethod[1];
			SearchRequestor requestor = new SearchRequestor() {
				@Override
				public void acceptSearchMatch(SearchMatch match)
						throws CoreException {
					if (result[0] == null) {
						result[0] = (IMethod) match.getElement();
					}
				}
			};
			SearchEngine engine = new SearchEngine();
			engine.search(pattern, new SearchParticipant[] { SearchEngine
					.getDefaultSearchParticipant() }, scope, requestor, null);
			return result[0];
		} catch (CoreException e) {
		}
		return null;
	}

	public static boolean checkTestMethod(IJavaProject javaProject,
			String testMethod) {
		IMethod method = getTestMethod(javaProject, testMethod);
		return method.getAnnotation(TestMethod.class.getName()) != null
				&& method.getDeclaringType().exists();
	}

	public static String getTestMethodName(IJavaProject javaProject,
			String testMethod) {
		IMethod method = getTestMethod(javaProject, testMethod);
		IAnnotation annotation = method.getAnnotation(TestMethod.class
				.getName());
		return annotation != null ? getTestMethodName(annotation) : "";
	}

	public static IAnnotation getTestMethodAnnotation(TypeReferenceMatch element) {
		try {
			IJavaElement javaElement = ((TypeReferenceMatch) element)
					.getLocalElement();
			if (javaElement != null) {
				if (javaElement instanceof IAnnotation
						&& javaElement.getParent() instanceof IMethod) {
					IAnnotation annotation = (IAnnotation) javaElement;
					IMethod method = (IMethod) javaElement.getParent();
					if (Flags.isPublic(method.getFlags())
							&& Flags.isPublic(method.getDeclaringType()
									.getFlags())) {
						return annotation;
					}
				}
			} else if (((TypeReferenceMatch) element).getElement() instanceof IMethod) {
				IMethod method = (IMethod) ((TypeReferenceMatch) element)
						.getElement();
				if (Flags.isPublic(method.getFlags())
						&& Flags.isPublic(method.getDeclaringType().getFlags())) {
					return method.getAnnotation(TestMethod.class.getName());
				}
			}
		} catch (JavaModelException e) {
		}
		return null;
	}

	public static String getAnnotationValue(IAnnotation annotation, String name) {
		try {
			for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
				if (pair.getMemberName().equals(name)) {
					return Strings.objToString(pair.getValue());
				}
			}
		} catch (JavaModelException e) {
		}
		return "";
	}

	public static String getTestMethodName(TypeReferenceMatch element) {
		IAnnotation annotation = getTestMethodAnnotation(element);
		return getTestMethodName(annotation);
	}

	public static String getTestMethodName(IAnnotation annotation) {
		IMethod method = (IMethod) annotation.getParent();
		String methodName = AutoTestProjects.getAnnotationValue(annotation,
				"value");
		if (Strings.isEmpty(methodName)) {
			methodName = method.getElementName();
		}
		return methodName;
	}

	public static String getTestMethodQualifiedName(TypeReferenceMatch element) {
		IAnnotation annotation = getTestMethodAnnotation(element);
		return getTestMethodQualifiedName(annotation);
	}

	public static String getTestMethodQualifiedName(IAnnotation annotation) {
		IMethod method = (IMethod) annotation.getParent();
		IType type = method.getDeclaringType();
		return type.getFullyQualifiedName() + "#" + method.getElementName();
	}

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

	public static boolean checkNonJavaResource(IJavaProject javaProject,
			String location) {
		try {
			Object resource = NonJavaResourceFinder.lookup(javaProject,
					location, null);
			if (resource instanceof IResource) {
				return true;
			} else if (resource instanceof IJarEntryResource) {
				return true;
			}
		} catch (CoreException e) {
		}
		return false;
	}
}
