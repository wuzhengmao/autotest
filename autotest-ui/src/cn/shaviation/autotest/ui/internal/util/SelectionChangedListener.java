package cn.shaviation.autotest.ui.internal.util;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;

import cn.shaviation.autotest.util.Objects;

public abstract class SelectionChangedListener implements
		ISelectionChangedListener {

	@Override
	public final void selectionChanged(SelectionChangedEvent event) {
		if (event.getSource() instanceof Viewer) {
			Object oldValue = ((Viewer) event.getSource()).getControl()
					.getData();
			ISelection selection = event.getSelection();
			if (selection instanceof IStructuredSelection) {
				Object newValue = ((IStructuredSelection) selection)
						.getFirstElement();
				if (Objects.equals(oldValue, newValue)) {
					return;
				}
				((Viewer) event.getSource()).getControl().setData(newValue);
			}
		}
		onSelectionChanged(event);
	}

	public abstract void onSelectionChanged(SelectionChangedEvent event);
}
