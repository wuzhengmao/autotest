package cn.shaviation.autotest.core.internal.builders;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.core.jdt.AutoTestProjects;
import cn.shaviation.autotest.core.util.JavaUtils;
import cn.shaviation.autotest.core.util.Validators;
import cn.shaviation.autotest.model.Parameter;
import cn.shaviation.autotest.model.TestDataDef;
import cn.shaviation.autotest.model.TestDataEntry;
import cn.shaviation.autotest.model.TestDataGroup;
import cn.shaviation.autotest.model.TestDataHelper;
import cn.shaviation.autotest.model.TestScript;
import cn.shaviation.autotest.model.TestScriptHelper;
import cn.shaviation.autotest.model.TestStep;
import cn.shaviation.autotest.util.IOUtils;
import cn.shaviation.autotest.util.Logs;
import cn.shaviation.autotest.util.Strings;

public class AutoTestProjectBuilder extends IncrementalProjectBuilder {

	private static final String MARKER = "cn.shaviation.autotest.core.problemmarker";

	private IProject project;
	private IJavaProject javaProject;
	private IPath[] outputPaths;

	@Override
	protected void startupOnInitialize() {
		super.startupOnInitialize();
		project = getProject();
		javaProject = JavaUtils.getJavaProject(project);
		if (javaProject != null) {
			Set<IPath> paths = new HashSet<IPath>();
			try {
				if (javaProject.getOutputLocation() != null) {
					paths.add(javaProject.getOutputLocation());
				}
				for (IClasspathEntry entry : javaProject.getRawClasspath()) {
					if (entry.getOutputLocation() != null) {
						paths.add(entry.getOutputLocation());
					}
				}
			} catch (JavaModelException e) {
				Logs.e(e);
			}
			outputPaths = paths.toArray(new IPath[paths.size()]);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		switch (kind) {
		case FULL_BUILD:
			fullBuild(monitor);
			break;
		default:
			IResourceDelta delta = getDelta(project);
			if (delta == null) {
				fullBuild(monitor);
			} else {
				IFile file = project.getFile(".classpath");
				if (file != null) {
					IResourceDelta rd = delta.findMember(file
							.getProjectRelativePath());
					if (rd != null && rd.getResource().equals(file)) {
						fullBuild(monitor);
						break;
					}
				}
				incrementalBuild(delta, monitor);
			}
			break;
		}
		if (javaProject == null) {
			addProblem(project, "It's not a Java project",
					IMarker.SEVERITY_ERROR);
		}
		return null;
	}

	private void fullBuild(IProgressMonitor monitor) throws CoreException {
		project.deleteMarkers(MARKER, true, IResource.DEPTH_INFINITE);
		project.accept(new IResourceVisitor() {
			@Override
			public boolean visit(IResource resource) throws CoreException {
				validate(resource);
				return true;
			}
		});
	}

	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor)
			throws CoreException {
		delta.accept(new IResourceDeltaVisitor() {
			@Override
			public boolean visit(IResourceDelta delta) throws CoreException {
				IResource resource = delta.getResource();
				switch (delta.getKind()) {
				case IResourceDelta.ADDED:
					validate(resource);
					break;
				case IResourceDelta.CHANGED:
					validate(resource);
					break;
				default:
					break;
				}
				return true;
			}
		});
	}

	private void validate(IResource resource) {
		if (resource instanceof IFile) {
			if (AutoTestCore.TEST_DATA_FILE_EXTENSION.equals(resource
					.getFileExtension()) && !ignore(resource)) {
				validateTestDataDef(resource);
			} else if (AutoTestCore.TEST_SCRIPT_FILE_EXTENSION.equals(resource
					.getFileExtension()) && !ignore(resource)) {
				validateTestScript(resource);
			}
		}
	}

	private boolean ignore(IResource resource) {
		if (outputPaths != null) {
			for (IPath path : outputPaths) {
				if (path.isPrefixOf(resource.getFullPath())) {
					return true;
				}
			}
		}
		return false;
	}

	private void validateTestDataDef(IResource resource) {
		deleteProblems(resource);
		try {
			resource.setPersistentProperty(AutoTestCore.TEST_DATA_NAME_KEY,
					null);
			String json = IOUtils.toString(
					((IFile) resource).getContents(true),
					((IFile) resource).getCharset());
			if (!Strings.isEmpty(json)) {
				TestDataDef testDataDef = TestDataHelper.parse(json);
				if (!Strings.isBlank(testDataDef.getName())) {
					resource.setPersistentProperty(
							AutoTestCore.TEST_DATA_NAME_KEY, testDataDef
									.getName().trim());
				}
				for (String error : validate(testDataDef)) {
					addProblem(resource, error, IMarker.SEVERITY_ERROR);
				}
			} else {
				addProblem(resource, "No content", IMarker.SEVERITY_WARNING);
			}
		} catch (CoreException e) {
			addProblem(resource, e.getStatus().getMessage(),
					getSeverity(e.getStatus()));
		} catch (Exception e) {
			addProblem(resource, e.getMessage(), IMarker.SEVERITY_ERROR);
		}
	}

	private void validateTestScript(IResource resource) {
		deleteProblems(resource);
		try {
			resource.setPersistentProperty(AutoTestCore.TEST_SCRIPT_NAME_KEY,
					null);
			String json = IOUtils.toString(
					((IFile) resource).getContents(true),
					((IFile) resource).getCharset());
			if (!Strings.isEmpty(json)) {
				TestScript testScript = TestScriptHelper.parse(json);
				if (!Strings.isBlank(testScript.getName())) {
					resource.setPersistentProperty(
							AutoTestCore.TEST_SCRIPT_NAME_KEY, testScript
									.getName().trim());
				}
				for (String error : validate(testScript)) {
					addProblem(resource, error, IMarker.SEVERITY_ERROR);
				}
				if (javaProject != null && testScript.getTestSteps() != null) {
					for (TestStep step : testScript.getTestSteps()) {
						if (!Strings.isEmpty(step.getInvokeTarget())) {
							if (step.getInvokeType() == TestStep.Type.Method) {
								if (!AutoTestProjects.checkTestMethod(
										javaProject, step.getInvokeTarget())) {
									addProblem(
											resource,
											"Test method '"
													+ step.getInvokeTarget()
													+ "' not found",
											IMarker.SEVERITY_ERROR);
								}
								if (!Strings.isEmpty(step.getTestDataFile())) {
									if (!AutoTestProjects
											.checkNonJavaResource(javaProject,
													step.getTestDataFile())) {
										addProblem(resource, "Resource '"
												+ step.getTestDataFile()
												+ "' not found",
												IMarker.SEVERITY_ERROR);
									}
								}
							} else {
								if (!AutoTestProjects.checkNonJavaResource(
										javaProject, step.getInvokeTarget())) {
									addProblem(
											resource,
											"Resource '"
													+ step.getInvokeTarget()
													+ "' not found",
											IMarker.SEVERITY_ERROR);
								}
							}
						}
					}
				}
			} else {
				addProblem(resource, "No content", IMarker.SEVERITY_WARNING);
			}
		} catch (CoreException e) {
			addProblem(resource, e.getStatus().getMessage(),
					getSeverity(e.getStatus()));
		} catch (Exception e) {
			addProblem(resource, e.getMessage(), IMarker.SEVERITY_ERROR);
		}
	}

	private int getSeverity(IStatus status) {
		switch (status.getSeverity()) {
		case IStatus.ERROR:
			return IMarker.SEVERITY_ERROR;
		case IStatus.WARNING:
			return IMarker.SEVERITY_WARNING;
		default:
			return IMarker.SEVERITY_INFO;
		}
	}

	private void addProblem(IResource resource, String message, int severity) {
		try {
			IMarker marker = resource.createMarker(MARKER);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
		} catch (CoreException e) {
		}
	}

	private void deleteProblems(IResource resource) {
		try {
			resource.deleteMarkers(MARKER, true, IResource.DEPTH_ZERO);
		} catch (CoreException e) {
		}
	}

	private Collection<String> validate(TestDataDef testDataDef) {
		Set<String> messages = new LinkedHashSet<String>();
		addMessages(messages, Validators.validate(testDataDef));
		if (testDataDef.getDataList() != null) {
			for (TestDataGroup group : testDataDef.getDataList()) {
				addMessages(messages, Validators.validate(group));
				if (group.getEntries() != null) {
					for (TestDataEntry entry : group.getEntries()) {
						addMessages(messages, Validators.validate(entry));
					}
				}
			}
		}
		return messages;
	}

	private Collection<String> validate(TestScript testScript) {
		Set<String> messages = new LinkedHashSet<String>();
		addMessages(messages, Validators.validate(testScript));
		if (testScript.getTestSteps() != null) {
			for (TestStep step : testScript.getTestSteps()) {
				addMessages(messages, Validators.validate(step));
				if (step.getParameters() != null) {
					for (Parameter param : step.getParameters()) {
						addMessages(messages, Validators.validate(param));
					}
				}
			}
		}
		return messages;
	}

	private <T> void addMessages(Set<String> messages,
			Set<ConstraintViolation<T>> violations) {
		for (ConstraintViolation<T> violation : violations) {
			messages.add(violation.getMessage());
		}
	}
}
