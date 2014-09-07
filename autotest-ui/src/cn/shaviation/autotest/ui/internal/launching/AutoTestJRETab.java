package cn.shaviation.autotest.ui.internal.launching;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class AutoTestJRETab extends JavaJRETab {

	private VMArgumentsBlock vmArgumentsBlock = new VMArgumentsBlock();

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		vmArgumentsBlock.createControl((Composite) ((Composite) parent
				.getChildren()[0]).getChildren()[0]);
		((GridData) vmArgumentsBlock.getControl().getLayoutData()).horizontalSpan = 2;
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);
		vmArgumentsBlock.performApply(configuration);
		setLaunchConfigurationWorkingCopy(configuration);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
		vmArgumentsBlock.initializeFrom(configuration);
	}

	@Override
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		super.setLaunchConfigurationDialog(dialog);
		vmArgumentsBlock.setLaunchConfigurationDialog(dialog);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		super.setDefaults(config);
		IResource resource = LaunchHelper.getContext();
		if (resource != null) {
			IPath path = LaunchHelper
					.getJREContainerPath(resource.getProject());
			if (path != null) {
				config.setAttribute(
						IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH,
						path.toPortableString());
				return;
			}
		}
		IVMInstall defaultVMInstall = getDefaultVMInstall(config);
		if (defaultVMInstall != null) {
			setDefaultVMInstallAttributes(defaultVMInstall, config);
		}
	}

	private IVMInstall getDefaultVMInstall(ILaunchConfiguration config) {
		IVMInstall defaultVMInstall;
		try {
			defaultVMInstall = JavaRuntime.computeVMInstall(config);
		} catch (CoreException localCoreException) {
			defaultVMInstall = JavaRuntime.getDefaultVMInstall();
		}
		return defaultVMInstall;
	}

	@SuppressWarnings("deprecation")
	private void setDefaultVMInstallAttributes(IVMInstall defaultVMInstall,
			ILaunchConfigurationWorkingCopy config) {
		String vmName = defaultVMInstall.getName();
		String vmTypeID = defaultVMInstall.getVMInstallType().getId();
		config.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME, vmName);
		config.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE,
				vmTypeID);
	}

	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		setLaunchConfigurationWorkingCopy(workingCopy);
	}

	@Override
	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {

	}
}
