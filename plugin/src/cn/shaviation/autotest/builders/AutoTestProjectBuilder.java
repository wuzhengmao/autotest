package cn.shaviation.autotest.builders;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import cn.shaviation.autotest.models.TestDataDef;
import cn.shaviation.autotest.models.TestDataHelper;
import cn.shaviation.autotest.util.IOUtils;

public class AutoTestProjectBuilder extends IncrementalProjectBuilder {

	public static final String ID = "cn.shaviation.autotest.builder";
	public static final String MARKER = "cn.shaviation.autotest.problemmarker";

	private IProject project;

	@Override
	protected void startupOnInitialize() {
		super.startupOnInitialize();
		project = getProject();
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
				incrementalBuild(delta, monitor);
			}
			break;
		}
		return null;
	}

	private void fullBuild(IProgressMonitor monitor) throws CoreException {
		getProject().deleteMarkers(MARKER, true, IResource.DEPTH_INFINITE);
		getProject().accept(new IResourceVisitor() {
			@Override
			public boolean visit(IResource resource) throws CoreException {
				if (resource instanceof IFile
						&& "tdd".equals(resource.getFileExtension())) {
					validateTestDataDef(resource);
				}
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
					if (resource instanceof IFile
							&& "tdd".equals(resource.getFileExtension())) {
						validateTestDataDef(resource);
					}
					break;
				case IResourceDelta.CHANGED:
					if (resource instanceof IFile
							&& "tdd".equals(resource.getFileExtension())) {
						validateTestDataDef(resource);
					}
					break;
				default:
					break;
				}
				return true;
			}
		});
	}

	private void validateTestDataDef(IResource resource) {
		deleteProblems(resource);
		try {
			String json = IOUtils.toString(((IFile) resource).getContents(true),
					((IFile) resource).getCharset());
			if (json != null && !json.isEmpty()) {
				TestDataDef testDataDef = TestDataHelper.parse(json);
				String error = TestDataHelper.validate(testDataDef);
				if (error != null) {
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
}
