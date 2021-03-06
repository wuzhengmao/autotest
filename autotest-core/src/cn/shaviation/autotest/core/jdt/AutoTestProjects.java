package cn.shaviation.autotest.core.jdt;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
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
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
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
import cn.shaviation.autotest.core.util.JavaUtils;
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

	public static String getResourceLocation(IResource resource,
			String fileExtension, boolean matchPackage) {
		if (JavaUtils.isJavaProject(resource.getProject())) {
			if (resource instanceof IProject) {
				if (matchPackage || Strings.isEmpty(fileExtension)) {
					return "/";
				}
			} else if (resource instanceof IFile) {
				if (isSourceFolder(resource.getParent())) {
					if (Strings.isEmpty(fileExtension)
							|| fileExtension.equalsIgnoreCase(resource
									.getFileExtension())) {
						return getPackagePath(resource.getParent())
								+ resource.getName();
					} else if (matchPackage) {
						return getPackagePath(resource.getParent());
					}
				}
			} else if (resource instanceof IFolder) {
				if ((matchPackage || Strings.isEmpty(fileExtension))
						&& isSourceFolder((IFolder) resource)) {
					return getPackagePath((IFolder) resource);
				}
			}
		}
		return null;
	}

	public static boolean isSourceFolder(IContainer folder) {
		IJavaElement element = JavaCore.create(folder);
		if (element != null) {
			return (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT)
					|| (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT);
		} else {
			IContainer parent = folder.getParent();
			return parent != null ? isSourceFolder(parent) : false;
		}
	}

	private static String getPackagePath(IContainer folder) {
		IJavaElement element = JavaCore.create(folder);
		if (element != null) {
			return getPackagePath(element);
		} else {
			String path = getPackagePath(folder.getParent());
			return path != null ? path + folder.getName() + "/" : null;
		}
	}

	private static String getPackagePath(IJavaElement element) {
		int type = element.getElementType();
		if (type == IJavaElement.PACKAGE_FRAGMENT) {
			return "/" + element.getElementName().replace('.', '/') + "/";
		} else if (type == IJavaElement.PACKAGE_FRAGMENT_ROOT
				|| type == IJavaElement.JAVA_PROJECT) {
			return "/";
		} else {
			IJavaElement parent;
			while ((parent = element.getParent()) != null) {
				int t = parent.getElementType();
				if (t == IJavaElement.PACKAGE_FRAGMENT
						|| t == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
					return getPackagePath(parent);
				}
			}
			return null;
		}
	}

	public static String getResourceLocation(IJarEntryResource resource,
			String fileExtension, boolean matchPackage) {
		if (resource.isFile()) {
			if (Strings.isEmpty(fileExtension)
					|| resource.getName().toLowerCase()
							.endsWith("." + fileExtension.toLowerCase())) {
				return getResourceLocation(resource);
			} else if (matchPackage) {
				return getJarEntryResourceParentLocation(resource.getParent());
			}
		} else if (matchPackage || Strings.isEmpty(fileExtension)) {
			return getJarEntryResourceParentLocation(resource);
		}
		return null;
	}

	private static String getResourceLocation(IJarEntryResource resource) {
		return getJarEntryResourceParentLocation(resource.getParent())
				+ resource.getName();
	}

	private static String getJarEntryResourceParentLocation(Object parent) {
		if (parent instanceof IJavaElement) {
			return getPackagePath((IJavaElement) parent);
		} else {
			return getResourceLocation((IJarEntryResource) parent) + '/';
		}
	}

	public static String getResourceLocation(IJavaElement resource,
			String fileExtension, boolean matchPackage) {
		if (resource instanceof ITypeRoot) {
			if (Strings.isEmpty(fileExtension)
					|| "class".equalsIgnoreCase(fileExtension)) {
				return getPackagePath(resource) + resource.getElementName()
						+ ".class";
			} else if (matchPackage) {
				return getPackagePath(resource);
			}
		} else if (matchPackage || Strings.isEmpty(fileExtension)) {
			return getPackagePath(resource);
		}
		return null;
	}
}
