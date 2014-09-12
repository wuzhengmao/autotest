package cn.shaviation.autotest.ui.internal.views;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.actions.SelectionListenerAction;

import cn.shaviation.autotest.util.Strings;

public class FailureTraceCopyAction extends SelectionListenerAction {

	private FailureTrace failureTrace;
	private final Clipboard clipboard;

	public FailureTraceCopyAction(FailureTrace failureTrace, Clipboard clipboard) {
		super("Copy Trace");
		Assert.isNotNull(clipboard);
		this.failureTrace = failureTrace;
		this.clipboard = clipboard;
	}

	@Override
	public void run() {
		String trace = failureTrace.getTrace();
		String source = convertLineTerminators(trace);
		if (Strings.isEmpty(source)) {
			return;
		}
		TextTransfer plainTextTransfer = TextTransfer.getInstance();
		try {
			clipboard.setContents(
					new String[] { convertLineTerminators(source) },
					new Transfer[] { plainTextTransfer });
		} catch (SWTError e) {
			if (e.code != 2002) {
				throw e;
			}
			if (MessageDialog
					.openQuestion(failureTrace.getComposite().getShell(),
							"Problem Copying to Clipboard",
							"There was a problem when accessing the system clipboard. Retry?")) {
				run();
			}
		}
	}

	private String convertLineTerminators(String in) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		StringReader stringReader = new StringReader(in);
		BufferedReader bufferedReader = new BufferedReader(stringReader);
		try {
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				printWriter.println(line);
			}
		} catch (IOException e) {
			return in;
		}
		return stringWriter.toString();
	}
}
