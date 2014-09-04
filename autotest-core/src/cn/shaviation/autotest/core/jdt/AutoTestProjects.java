package cn.shaviation.autotest.core.jdt;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IImportDeclaration;
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

import cn.shaviation.autotest.annotation.TestMethod;
import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.model.TestDataDef;
import cn.shaviation.autotest.model.TestDataHelper;
import cn.shaviation.autotest.model.TestScript;
import cn.shaviation.autotest.model.TestScriptHelper;
import cn.shaviation.autotest.util.IOUtils;
import cn.shaviation.autotest.util.Objects;
import cn.shaviation.autotest.util.Strings;

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
		} catch (Exception e) {
		}
		return null;
	}

	public static boolean checkTestMethod(IJavaProject javaProject,
			String testMethod) {
		IMethod method = getTestMethod(javaProject, testMethod);
		return method != null
				&& getAnnotation(method, TestMethod.class.getName()) != null
				&& method.getDeclaringType().exists();
	}

	public static String getTestMethodName(IJavaProject javaProject,
			String testMethod) {
		IMethod method = getTestMethod(javaProject, testMethod);
		IAnnotation annotation = method != null ? getAnnotation(method,
				TestMethod.class.getName()) : null;
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
					return getAnnotation(method, TestMethod.class.getName());
				}
			}
		} catch (JavaModelException e) {
		}
		return null;
	}

	private static IAnnotation getAnnotation(IMethod method,
			String annotationName) {
		try {
			for (IAnnotation annotation : method.getAnnotations()) {
				if (annotation.getElementName().equals(annotationName)) {
					return annotation;
				} else {
					int i = annotationName.lastIndexOf('.');
					if (i >= 0) {
						if (annotation.getElementName().equals(
								annotationName.substring(i + 1))) {
							for (IImportDeclaration importDec : method
									.getCompilationUnit().getImports()) {
								if (importDec.getElementName().equals(
										annotationName)
										|| importDec.getElementName().equals(
												annotationName.substring(0, i)
														+ "*")) {
									return annotation;
								}
							}
						}
					}
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
					return Objects.toString(pair.getValue());
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

	public static String getTestDataName(IResource resource) {
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

	public static String getTestDataName(IJarEntryResource resource) {
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

	public static String getTestScriptName(IResource resource) {
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

	public static String getTestScriptName(IJarEntryResource resource) {
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
