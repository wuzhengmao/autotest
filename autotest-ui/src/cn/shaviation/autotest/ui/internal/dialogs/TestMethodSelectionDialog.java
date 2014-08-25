package cn.shaviation.autotest.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
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
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogSettings;
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

import cn.shaviation.autotest.core.annotation.TestMethod;
import cn.shaviation.autotest.core.util.JavaUtils;
import cn.shaviation.autotest.core.util.Logs;
import cn.shaviation.autotest.core.util.Strings;
import cn.shaviation.autotest.ui.AutoTestUI;
import cn.shaviation.autotest.ui.internal.util.UIUtils;

public class TestMethodSelectionDialog extends FilteredItemsSelectionDialog {

	private IJavaProject javaProject;
	private String title;
	private MethodItemLabelProvider methodItemLabelProvider;
	private MethodItemsComparator methodItemsComparator;
	private MethodItemsFilter filter;

	public TestMethodSelectionDialog(Shell shell, IJavaProject javaProject) {
		super(shell, false);
		this.javaProject = javaProject;
		this.methodItemLabelProvider = new MethodItemLabelProvider();
		this.methodItemsComparator = new MethodItemsComparator();
		setListLabelProvider(this.methodItemLabelProvider);
		setListSelectionLabelDecorator(this.methodItemLabelProvider);
		setDetailsLabelProvider(new MethodItemDetailsLabelProvider());
		setTitle("Select Test Method");
		setMessage("Enter method name prefix or pattern (*, ?, or camel case):");
	}

	@Override
	public void setTitle(String title) {
		super.setTitle(title);
		this.title = title;
	}

	@Override
	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings = AutoTestUI
				.getDefault()
				.getDialogSettings()
				.getSection(
						"cn.shaviation.autotest.dialogs.TestMethodSelectionDialog");
		if (settings == null) {
			settings = AutoTestUI
					.getDefault()
					.getDialogSettings()
					.addNewSection(
							"cn.shaviation.autotest.dialogs.TestMethodSelectionDialog");
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
		List<IAnnotation> resultToReturn = new ArrayList<IAnnotation>(1);
		for (int i = 0; i < newResult.size(); i++) {
			IAnnotation annotation = getAnnotation(newResult.get(i));
			IMethod method = (IMethod) annotation.getParent();
			IType type = method.getDeclaringType();
			if (type.exists()) {
				resultToReturn.add(annotation);
			} else {
				IPackageFragmentRoot root = (IPackageFragmentRoot) type
						.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
				UIUtils.showError(
						getShell(),
						title,
						"Type '"
								+ type.getFullyQualifiedName()
								+ "' could not be found in '"
								+ JavaUtils.getPackageFragmentRootLabel(root)
								+ "'. Make sure all workspace resources are refreshed.");
			}
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
		filter = new MethodItemsFilter();
		return filter;
	}

	@Override
	protected void fillContentProvider(AbstractContentProvider provider,
			ItemsFilter itemsFilter, IProgressMonitor progressMonitor)
			throws CoreException {
		MethodItemsFilter methodSearchFilter = (MethodItemsFilter) itemsFilter;
		MethodSearchRequestor requestor = new MethodSearchRequestor(provider,
				methodSearchFilter);
		SearchPattern pattern = SearchPattern.createPattern(
				TestMethod.class.getName(), IJavaSearchConstants.TYPE,
				IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
				SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE);
		IJavaSearchScope scope = SearchEngine
				.createJavaSearchScope(new IJavaElement[] { javaProject });
		SearchEngine engine = new SearchEngine();
		progressMonitor.setTaskName("Searching");
		engine.search(pattern, new SearchParticipant[] { SearchEngine
				.getDefaultSearchParticipant() }, scope, requestor,
				progressMonitor);
	}

	@Override
	protected Comparator<?> getItemsComparator() {
		return methodItemsComparator;
	}

	@Override
	public String getElementName(Object item) {
		return getMethodName(item);
	}

	@Override
	protected IStatus validateItem(Object item) {
		if (item == null) {
			return new Status(IStatus.ERROR, AutoTestUI.PLUGIN_ID, "", null);
		}
		return Status.OK_STATUS;
	}

	private static class MethodItemDetailsLabelProvider extends LabelProvider {

		@Override
		public Image getImage(Object element) {
			IMethod method = (IMethod) getAnnotation(element).getParent();
			IType type = method.getDeclaringType();
			if (type.getDeclaringType() == null) {
				return JavaUI.getSharedImages().getImage(
						ISharedImages.IMG_OBJS_PACKAGE);
			} else {
				return JavaUI.getSharedImages().getImage(
						ISharedImages.IMG_OBJS_CLASS);
			}
		}

		@Override
		public String getText(Object element) {
			IMethod method = (IMethod) getAnnotation(element).getParent();
			IType type = method.getDeclaringType();
			StringBuilder sb = new StringBuilder();
			if (type.getDeclaringType() == null) {
				sb.append(type.getPackageFragment().getElementName());
			} else {
				sb.append(type.getFullyQualifiedName());
			}
			if (sb.length() > 0) {
				sb.append(" - ");
			}
			IPackageFragmentRoot root = (IPackageFragmentRoot) type
					.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
			sb.append(JavaUtils.getPackageFragmentRootLabel(root));
			return sb.toString();
		}
	}

	private class MethodItemLabelProvider extends LabelProvider implements
			ILabelDecorator, IStyledLabelProvider {

		@Override
		public Image getImage(Object element) {
			if (element != null) {
				return JavaUI.getSharedImages().getImage(
						ISharedImages.IMG_OBJS_CLASS);
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
			IAnnotation annotation = getAnnotation(element);
			StringBuilder sb = new StringBuilder();
			sb.append(getMethodName(element));
			if (fully) {
				sb.append(" - ");
				sb.append(getMethodQualifiedName(element));
				sb.append(" - ");
				String version = getAnnotationValue(annotation, "version");
				if (version.isEmpty()) {
					try {
						version = Strings.objToString(TestMethod.class
								.getMethod("version").getDefaultValue());
					} catch (Exception e) {
						Logs.w(e);
					}
				}
				if (!version.isEmpty()) {
					if (version.charAt(0) == 'v') {
						version = "V" + version.substring(1);
					} else if (version.charAt(0) != 'V') {
						version = "V" + version;
					}
					sb.append(version);
				}
				String author = getAnnotationValue(annotation, "author");
				if (!author.isEmpty()) {
					sb.append(" by ").append(author);
				}
			}
			return sb.toString();
		}
	}

	private static IAnnotation getAnnotation(Object element) {
		try {
			if (element instanceof TypeReferenceMatch) {
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
							&& Flags.isPublic(method.getDeclaringType()
									.getFlags())) {
						return method.getAnnotation(TestMethod.class.getName());
					}
				}
			}
		} catch (JavaModelException e) {
			Logs.w(e);
		}
		return null;
	}

	private static String getAnnotationValue(IAnnotation annotation, String name) {
		try {
			for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
				if (pair.getMemberName().equals(name)) {
					return Strings.objToString(pair.getValue());
				}
			}
		} catch (JavaModelException e) {
			Logs.w(e);
		}
		return "";
	}

	private static String getMethodName(Object element) {
		IAnnotation annotation = getAnnotation(element);
		IMethod method = (IMethod) annotation.getParent();
		String methodName = getAnnotationValue(annotation, "value");
		if (methodName.isEmpty()) {
			methodName = method.getElementName();
		}
		return methodName;
	}

	private static String getMethodQualifiedName(Object element) {
		IMethod method = (IMethod) getAnnotation(element).getParent();
		IType type = method.getDeclaringType();
		return type.getFullyQualifiedName() + "#" + method.getElementName();
	}

	private static String getMethodTypeName(Object element) {
		IMethod method = (IMethod) getAnnotation(element).getParent();
		IType type = method.getDeclaringType();
		return type.getElementName();
	}

	private static class MethodItemsComparator implements Comparator<Object> {

		public int compare(Object left, Object right) {
			int result = getMethodName(left).compareTo(getMethodName(right));
			if (result != 0) {
				return result;
			}
			IMethod leftMethod = (IMethod) getAnnotation(left).getParent();
			IMethod rightMethod = (IMethod) getAnnotation(right).getParent();
			result = leftMethod
					.getDeclaringType()
					.getFullyQualifiedName()
					.compareTo(
							rightMethod.getDeclaringType()
									.getFullyQualifiedName());
			if (result != 0) {
				return result;
			}
			return leftMethod.getElementName().compareTo(
					rightMethod.getElementName());
		}
	}

	private class MethodItemsFilter extends ItemsFilter {

		@Override
		public String getPattern() {
			String pattern = super.getPattern();
			return pattern != null && !pattern.isEmpty() ? pattern : "*";
		}

		@Override
		public boolean matchItem(Object item) {
			String text = getMethodName(item);
			if (matches(text)) {
				return true;
			}
			text = getMethodTypeName(item);
			if (matches(text)) {
				return true;
			}
			text = getMethodQualifiedName(item);
			return matches(text);
		}

		@Override
		public boolean isConsistentItem(Object item) {
			return true;
		}
	}

	private static class MethodSearchRequestor extends SearchRequestor {

		private final AbstractContentProvider contentProvider;
		private final MethodItemsFilter methodItemsFilter;

		public MethodSearchRequestor(AbstractContentProvider contentProvider,
				MethodItemsFilter methodItemsFilter) {
			this.contentProvider = contentProvider;
			this.methodItemsFilter = methodItemsFilter;
		}

		@Override
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			if (getAnnotation(match) != null) {
				contentProvider.add(match, methodItemsFilter);
			}
		}
	}
}