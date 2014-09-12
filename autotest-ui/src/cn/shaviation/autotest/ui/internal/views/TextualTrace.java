package cn.shaviation.autotest.ui.internal.views;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class TextualTrace {

	public static final int LINE_TYPE_NORMAL = 0;
	public static final int LINE_TYPE_EXCEPTION = 1;
	public static final int LINE_TYPE_STACKFRAME = 2;

	private final String trace;

	public TextualTrace(String trace) {
		this.trace = trace;
	}

	public void display(FailureTableDisplay display, int maxLabelLength) {
		StringReader stringReader = new StringReader(trace);
		BufferedReader bufferedReader = new BufferedReader(stringReader);
		try {
			String line = readLine(bufferedReader);
			if (line == null) {
				return;
			}
			displayWrappedLine(display, maxLabelLength, line,
					LINE_TYPE_EXCEPTION);
			while ((line = readLine(bufferedReader)) != null) {
				int type = isAStackFrame(line) ? LINE_TYPE_STACKFRAME
						: LINE_TYPE_NORMAL;
				displayWrappedLine(display, maxLabelLength, line, type);
			}
		} catch (IOException e) {
			display.addTraceLine(LINE_TYPE_NORMAL, trace);
		}
	}

	private void displayWrappedLine(FailureTableDisplay display,
			int maxLabelLength, String line, int type) {
		int labelLength = line.length();
		if (labelLength < maxLabelLength) {
			display.addTraceLine(type, line);
		} else {
			display.addTraceLine(type, line.substring(0, maxLabelLength));
			int offset = maxLabelLength;
			while (offset < labelLength) {
				int nextOffset = Math.min(labelLength, offset + maxLabelLength);
				display.addTraceLine(LINE_TYPE_NORMAL,
						line.substring(offset, nextOffset));
				offset = nextOffset;
			}
		}
	}

	private boolean isAStackFrame(String itemLabel) {
		return itemLabel.indexOf(" at ") >= 0;
	}

	private String readLine(BufferedReader bufferedReader) throws IOException {
		String readLine = bufferedReader.readLine();
		return readLine == null ? null : readLine.replace('\t', ' ');
	}
}