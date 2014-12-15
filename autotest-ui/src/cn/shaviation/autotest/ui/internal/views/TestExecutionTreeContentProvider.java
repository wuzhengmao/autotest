package cn.shaviation.autotest.ui.internal.views;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import cn.shavation.autotest.runner.TestElement;
import cn.shavation.autotest.runner.TestNode;

public class TestExecutionTreeContentProvider implements ITreeContentProvider {

	private final TestElement[] NO_CHILDREN = new TestElement[0];

	@Override
	public Object[] getChildren(Object element) {
		TestElement node = (TestElement) element;
		if (node instanceof TestNode) {
			List<? extends TestElement> children = ((TestNode) node)
					.getChildren();
			if (children != null && !children.isEmpty()) {
				return children.toArray();
			}
		}
		return NO_CHILDREN;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object getParent(Object element) {
		return ((TestElement) element).getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		TestElement node = (TestElement) element;
		if (node instanceof TestNode) {
			List<? extends TestElement> children = ((TestNode) node)
					.getChildren();
			return children != null && !children.isEmpty();
		}
		return false;
	}

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}
}