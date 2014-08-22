package cn.shaviation.autotest.text;

import java.text.CharacterIterator;

import org.eclipse.core.runtime.Assert;

import com.ibm.icu.text.BreakIterator;

public class JavaBreakIterator extends BreakIterator {

	private static final Run WHITESPACE = new Whitespace();
	private static final Run DELIMITER = new LineDelimiter();
	private static final Run CAMELCASE = new CamelCaseIdentifier();
	private static final Run OTHER = new Other();
	protected final BreakIterator fIterator;
	protected CharSequence fText;
	private int fIndex;

	public JavaBreakIterator() {
		this.fIterator = BreakIterator.getWordInstance();
		this.fIndex = this.fIterator.current();
	}

	public int current() {
		return this.fIndex;
	}

	public int first() {
		this.fIndex = this.fIterator.first();
		return this.fIndex;
	}

	public int following(int offset) {
		if (offset == getText().getEndIndex()) {
			return -1;
		}
		int next = this.fIterator.following(offset);
		if (next == -1) {
			return -1;
		}

		Run run = consumeRun(offset);
		return offset + run.length;
	}

	private Run consumeRun(int offset) {
		char ch = this.fText.charAt(offset);
		int length = this.fText.length();
		Run run = getRun(ch);
		while ((run.consume(ch)) && (offset < length - 1)) {
			offset++;
			ch = this.fText.charAt(offset);
		}

		return run;
	}

	private Run getRun(char ch) {
		Run run;
		if (WHITESPACE.isValid(ch)) {
			run = WHITESPACE;
		} else {
			if (DELIMITER.isValid(ch)) {
				run = DELIMITER;
			} else {
				if (CAMELCASE.isValid(ch)) {
					run = CAMELCASE;
				} else {
					if (OTHER.isValid(ch)) {
						run = OTHER;
					} else {
						Assert.isTrue(false);
						return null;
					}
				}
			}
		}
		run.init();
		return run;
	}

	public CharacterIterator getText() {
		return this.fIterator.getText();
	}

	public boolean isBoundary(int offset) {
		if (offset == getText().getBeginIndex()) {
			return true;
		}
		return following(offset - 1) == offset;
	}

	public int last() {
		this.fIndex = this.fIterator.last();
		return this.fIndex;
	}

	public int next() {
		this.fIndex = following(this.fIndex);
		return this.fIndex;
	}

	public int next(int n) {
		return this.fIterator.next(n);
	}

	public int preceding(int offset) {
		if (offset == getText().getBeginIndex()) {
			return -1;
		}
		if (isBoundary(offset - 1)) {
			return offset - 1;
		}
		int previous = offset - 1;
		do
			previous = this.fIterator.preceding(previous);
		while (!isBoundary(previous));

		int last = -1;
		while (previous < offset) {
			last = previous;
			previous = following(previous);
		}

		return last;
	}

	public int previous() {
		this.fIndex = preceding(this.fIndex);
		return this.fIndex;
	}

	public void setText(String newText) {
		setText(newText);
	}

	public void setText(CharSequence newText) {
		this.fText = newText;
		this.fIterator.setText(new SequenceCharacterIterator(newText));
		first();
	}

	public void setText(CharacterIterator newText) {
		if ((newText instanceof CharSequence)) {
			this.fText = ((CharSequence) newText);
			this.fIterator.setText(newText);
			first();
		} else {
			throw new UnsupportedOperationException(
					"CharacterIterator not supported");
		}
	}

	static final class CamelCaseIdentifier extends JavaBreakIterator.Run {

		private static final int S_INIT = 0;
		private static final int S_LOWER = 1;
		private static final int S_ONE_CAP = 2;
		private static final int S_ALL_CAPS = 3;
		private static final int S_EXIT = 4;
		private static final int S_EXIT_MINUS_ONE = 5;
		private static final int K_INVALID = 0;
		private static final int K_LOWER = 1;
		private static final int K_UPPER = 2;
		private static final int K_OTHER = 3;
		private int fState;
		private static final int[][] MATRIX = {
				{ S_EXIT, S_LOWER, S_ONE_CAP, S_LOWER },
				{ S_EXIT, S_LOWER, S_EXIT, S_LOWER },
				{ S_EXIT, S_LOWER, S_ALL_CAPS, S_LOWER },
				{ S_EXIT, S_EXIT_MINUS_ONE, S_ALL_CAPS, S_LOWER } };

		protected void init() {
			super.init();
			this.fState = S_INIT;
		}

		protected boolean consume(char ch) {
			int kind = getKind(ch);
			this.fState = MATRIX[this.fState][kind];
			switch (this.fState) {
			case S_LOWER:
			case S_ONE_CAP:
			case S_ALL_CAPS:
				this.length += 1;
				return true;
			case S_EXIT:
				return false;
			case S_EXIT_MINUS_ONE:
				this.length -= 1;
				return false;
			}
			Assert.isTrue(false);
			return false;
		}

		private int getKind(char ch) {
			if (Character.isUpperCase(ch))
				return K_UPPER;
			if (Character.isLowerCase(ch))
				return K_LOWER;
			if (Character.isJavaIdentifierPart(ch))
				return K_OTHER;
			return K_INVALID;
		}

		protected boolean isValid(char ch) {
			return Character.isJavaIdentifierPart(ch);
		}
	}

	static final class Identifier extends JavaBreakIterator.Run {
		protected boolean isValid(char ch) {
			return Character.isJavaIdentifierPart(ch);
		}
	}

	static final class LineDelimiter extends JavaBreakIterator.Run {

		private char fState;
		private static final char INIT = '\000';
		private static final char EXIT = '\001';

		protected void init() {
			super.init();
			this.fState = INIT;
		}

		protected boolean consume(char ch) {
			if ((!isValid(ch)) || (this.fState == EXIT)) {
				return false;
			}
			if (this.fState == 0) {
				this.fState = ch;
				this.length += 1;
				return true;
			}
			if (this.fState != ch) {
				this.fState = EXIT;
				this.length += 1;
				return true;
			}
			return false;
		}

		protected boolean isValid(char ch) {
			return (ch == '\n') || (ch == '\r');
		}
	}

	static final class Other extends JavaBreakIterator.Run {
		protected boolean isValid(char ch) {
			return (!Character.isWhitespace(ch))
					&& (!Character.isJavaIdentifierPart(ch));
		}
	}

	protected static abstract class Run {

		protected int length;

		public Run() {
			init();
		}

		protected boolean consume(char ch) {
			if (isValid(ch)) {
				this.length += 1;
				return true;
			}
			return false;
		}

		protected abstract boolean isValid(char paramChar);

		protected void init() {
			this.length = 0;
		}
	}

	static final class Whitespace extends JavaBreakIterator.Run {
		protected boolean isValid(char ch) {
			return (Character.isWhitespace(ch)) && (ch != '\n') && (ch != '\r');
		}
	}
}