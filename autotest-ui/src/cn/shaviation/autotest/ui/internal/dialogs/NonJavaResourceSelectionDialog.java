package cn.shaviation.autotest.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.model.IWorkbenchAdapter;

import cn.shaviation.autotest.core.AutoTestCore;
import cn.shaviation.autotest.core.jdt.INonJavaResourceVisitor;
import cn.shaviation.autotest.core.jdt.NonJavaResourceFinder;
import cn.shaviation.autotest.core.util.JavaUtils;
import cn.shaviation.autotest.ui.AutoTestUI;
import cn.shaviation.autotest.ui.internal.util.UIUtils;
import cn.shaviation.autotest.util.Strings;

public abstract class NonJavaResourceSelectionDialog extends
		FilteredItemsSelectionDialog {

	private IJavaProject javaProject;
	private ResourceItemLabelProvider itemLabelProvider;
	private ResourceItemsComparator itemsComparator;
	private ResourceItemsFilter filter;

	public NonJavaResourceSelectionDialog(Shell shell, IJavaProject javaProject) {
		super(shell, false);
		this.javaProject = javaProject;
		this.itemLabelProvider = new ResourceItemLabelProvider();
		this.itemsComparator = new ResourceItemsComparator();
		setListLabelProvider(this.itemLabelProvider);
		setListSelectionLabelDecorator(this.itemLabelProvider);
		setDetailsLabelProvider(new ResourceItemDetailsLabelProvider());
	}

	@Override
	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings = AutoTestUI.getDefault().getDialogSettings()
				.getSection(getClass().getName());
		if (settings == null) {
			settings = AutoTestUI.getDefault().getDialogSettings()
					.addNewSection(getClass().getName());
		}
		return settings;
	}

	@Override
	protected Control createExtendedContentArea(Composite parent) {
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void setResult(List newResult) {
		List<String> resultToReturn = new ArrayList<String>(newResult.size());
		for (int i = 0; i < newResult.size(); i++) {
			ResourceItem item = (ResourceItem) newResult.get(i);
			resultToReturn.add(item.path);
		}
		super.setResult(resultToReturn);
	}

	@Override
	public void create() {
		super.create();
		Control patternControl = getPatternControl();
		if ((patternControl instanceof Text)) {
			TextFieldNavigationHandler.install((Text) patternControl);
		}
	}

	@Override
	protected ItemsFilter createFilter() {
		filter = new ResourceItemsFilter();
		return filter;
	}

	protected abstract String getFileExtension();

	protected abstract String getResourceName(Object resource);

	protected abstract ImageDescriptor getResourceImageDescriptor();

	@Override
	protected void fillContentProvider(final AbstractContentProvider provider,
			final ItemsFilter itemsFilter, IProgressMonitor progressMonitor)
			throws CoreException {
		final Set<String> paths = new HashSet<String>();
		progressMonitor.setTaskName("Searching");
		NonJavaResourceFinder.search(javaProject, getFileExtension(),
				new INonJavaResourceVisitor() {

					@Override
					public boolean visit(String path, IFile resource)
							throws CoreException {
						if (paths.add(path)) {
							ResourceItem item = new ResourceItem();
							item.path = path;
							item.resource = resource;
							item.name = getResourceName(resource);
							provider.add(item, itemsFilter);
						}
						return true;
					}

					@Override
					public boolean visit(String path, IJarEntryResource resource)
							throws CoreException {
						if (paths.add(path)) {
							ResourceItem item = new ResourceItem();
							item.path = path;
							item.resource = resource;
							item.name = getResourceName(resource);
							provider.add(item, itemsFilter);
						}
						return true;
					}
				}, progressMonitor);
	}

	@Override
	protected Comparator<?> getItemsComparator() {
		return itemsComparator;
	}

	@Override
	public String getElementName(Object element) {
		ResourceItem item = (ResourceItem) element;
		return item.name;
	}

	@Override
	protected IStatus validateItem(Object item) {
		if (item == null) {
			return new Status(IStatus.ERROR, AutoTestUI.PLUGIN_ID, "", null);
		}
		return Status.OK_STATUS;
	}

	private static class ResourceItem {
		String path;
		String name;
		Object resource;
	}

	private static class ResourceItemDetailsLabelProvider extends LabelProvider {

		@Override
		public Image getImage(Object element) {
			ResourceItem item = (ResourceItem) element;
			if (item.resource instanceof IResource) {
				IResource parent = ((IResource) item.resource).getParent();
				IWorkbenchAdapter adapter = AutoTestCore.getAdapter(parent,
						IWorkbenchAdapter.class);
				if (adapter == null) {
					return null;
				}
				ImageDescriptor descriptor = adapter.getImageDescriptor(parent);
				if (descriptor == null) {
					return null;
				}
				return UIUtils.getImage(descriptor);
			} else if (item.resource instanceof IJarEntryResource) {
				return JavaUI.getSharedImages().getImage(
						ISharedImages.IMG_OBJS_JAR);
			}
			return null;
		}

		@Override
		public String getText(Object element) {
			ResourceItem item = (ResourceItem) element;
			if (item.resource instanceof IResource) {
				IResource parent = ((IResource) item.resource).getParent();
				return parent.getType() != IResource.ROOT ? parent
						.getFullPath().makeRelative().toString() : null;
			} else if (item.resource instanceof IJarEntryResource) {
				return ((IJarEntryResource) item.resource).getFullPath()
						+ " - "
						+ JavaUtils
								.getPackageFragmentRootLabel(((IJarEntryResource) item.resource)
										.getPackageFragmentRoot());
			}
			return null;
		}
	}

	private class ResourceItemLabelProvider extends LabelProvider implements
			ILabelDecorator, IStyledLabelProvider {

		@Override
		public Image getImage(Object element) {
			if (element != null) {
				return UIUtils.getImage(getResourceImageDescriptor());
			} else {
				return super.getImage(element);
			}
		}

		@Override
		public String getText(Object element) {
			if (element != null) {
				return getText(element, isDuplicateElement(element));
			} else {
				return super.getText(element);
			}
		}

		@Override
		public Image decorateImage(Image image, Object element) {
			return image;
		}

		@Override
		public String decorateText(String text, Object element) {
			if (element != null) {
				return getText(element, true);
			} else {
				return text;
			}
		}

		@Override
		public StyledString getStyledText(Object element) {
			String text = getText(element);
			StyledString string = new StyledString(text);
			int index = text.indexOf(" - ");
			if (index != -1) {
				string.setStyle(index, text.length() - index,
						StyledString.QUALIFIER_STYLER);
			}
			return string;
		}

		private String getText(Object element, boolean fully) {
			ResourceItem item = (ResourceItem) element;
			StringBuilder sb = new StringBuilder();
			sb.append(item.name);
			if (fully) {
				sb.append(" - ");
				sb.append(item.path);
			}
			return sb.toString();
		}
	}

	private static class ResourceItemsComparator implements Comparator<Object> {

		public int compare(Object left, Object right) {
			ResourceItem item1 = (ResourceItem) left;
			ResourceItem item2 = (ResourceItem) right;
			int result = item1.name.compareTo(item2.name);
			return result != 0 ? result : item1.path.compareTo(item2.path);
		}
	}

	private class ResourceItemsFilter extends ItemsFilter {

		@Override
		public String getPattern() {
			String pattern = super.getPattern();
			return !Strings.isEmpty(pattern) ? pattern : "*";
		}

		@Override
		public boolean matchItem(Object element) {
			ResourceItem item = (ResourceItem) element;
			if (matches(item.name) || matches(item.path)) {
				return true;
			}
			int i = item.path.lastIndexOf('/');
			return i >= 0 ? matches(item.path.substring(i + 1)) : false;
		}

		@Override
		public boolean isConsistentItem(Object item) {
			return true;
		}
	}
}