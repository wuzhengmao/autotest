package cn.shaviation.autotest.ui.internal.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;

public class OpenEditorAtLineAction extends OpenEditorAction {

	private int lineNumber;

	public OpenEditorAtLineAction(Shell shell, IJavaProject javaProject,
			String className, int line) {
		super(shell, javaProject, className);
		this.lineNumber = line;
	}

	@Override
	protected void reveal(ITextEditor textEditor) {
		if (lineNumber >= 0) {
			try {
				IDocument document = textEditor.getDocumentProvider()
						.getDocument(textEditor.getEditorInput());
				textEditor.selectAndReveal(
						document.getLineOffset(lineNumber - 1),
						document.getLineLength(lineNumber - 1));
			} catch (BadLocationException e) {
			}
		}
	}

	@Override
	protected IJavaElement findElement(IJavaProject project, String className)
			throws CoreException {
		return findType(project, className);
	}
}
