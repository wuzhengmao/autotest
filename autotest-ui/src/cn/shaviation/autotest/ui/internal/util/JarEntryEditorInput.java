package cn.shaviation.autotest.ui.internal.util;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PlatformUI;

public class JarEntryEditorInput implements IStorageEditorInput {

	private IJarEntryResource resource;

	public JarEntryEditorInput(IJarEntryResource resource) {
		Assert.isNotNull(resource);
		this.resource = resource;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof JarEntryEditorInput))
			return false;
		return resource.equals(((JarEntryEditorInput) obj).resource);
	}

	@Override
	public int hashCode() {
		return resource.hashCode();
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		IEditorRegistry registry = PlatformUI.getWorkbench()
				.getEditorRegistry();
		return registry.getImageDescriptor(resource.getFullPath()
				.getFileExtension());
	}

	@Override
	public String getName() {
		return resource.getName();
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		IPackageFragmentRoot root = resource.getPackageFragmentRoot();
		IPath fullPath = root.getPath().append(resource.getFullPath());
		return root.isExternal() ? fullPath.toOSString() : fullPath
				.makeRelative().toString();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public IStorage getStorage() throws CoreException {
		return resource;
	}
}
