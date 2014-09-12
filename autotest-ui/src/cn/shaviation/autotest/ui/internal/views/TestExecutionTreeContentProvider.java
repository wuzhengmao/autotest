package cn.shaviation.autotest.ui.internal.views;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import cn.shavation.autotest.runner.TestElement;
import cn.shavation.autotest.runner.TestNode;

public class TestExecutionTreeContentProvider implements ITreeContentProvider {

	private final TreeNode[] NO_CHILDREN = new TreeNode[0];

	public static class TreeNode {

		private TreeNode parent;
		private TestElement element;

		public TreeNode(TestElement element) {
			this.element = element;
		}

		public TreeNode(TreeNode parent, TestElement element) {
			this.parent = parent;
			this.element = element;
		}

		public TreeNode getParent() {
			return parent;
		}

		public TestElement getElement() {
			return element;
		}
	}

	@Override
	public Object[] getChildren(Object element) {
		TreeNode node = (TreeNode) element;
		if (node.getElement() instanceof TestNode) {
			List<? extends TestElement> children = ((TestNode) node
					.getElement()).getChildren();
			if (children != null && !children.isEmpty()) {
				TreeNode[] subnodes = new TreeNode[children.size()];
				for (int i = 0; i < children.size(); i++) {
					subnodes[i] = new TreeNode(node, children.get(i));
				}
				return subnodes;
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
		return ((TreeNode) element).getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		TreeNode node = (TreeNode) element;
		if (node.getElement() instanceof TestNode) {
			List<? extends TestElement> children = ((TestNode) node
					.getElement()).getChildren();
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