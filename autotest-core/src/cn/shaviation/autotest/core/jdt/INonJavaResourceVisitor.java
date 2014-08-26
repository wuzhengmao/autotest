package cn.shaviation.autotest.core.jdt;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJarEntryResource;

public interface INonJavaResourceVisitor {

	boolean visit(String path, IFile resource) throws CoreException;

	boolean visit(String path, IJarEntryResource resource) throws CoreException;
}
