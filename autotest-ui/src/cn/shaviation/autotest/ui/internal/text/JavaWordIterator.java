package cn.shaviation.autotest.ui.internal.text;

import com.ibm.icu.text.BreakIterator;
import java.text.CharacterIterator;
import org.eclipse.core.runtime.Assert;

public class JavaWordIterator extends BreakIterator {
	
	private JavaBreakIterator fIterator;
	private int fIndex;

	public JavaWordIterator() {
		this.fIterator = new JavaBreakIterator();
		first();
	}

	public int first() {
		this.fIndex = this.fIterator.first();
		return this.fIndex;
	}

	public int last() {
		this.fIndex = this.fIterator.last();
		return this.fIndex;
	}

	public int next(int n) {
		int next = 0;
		do {
			next = next();

			n--;
		} while ((n > 0) && (next != -1));

		return next;
	}

	public int next() {
		this.fIndex = following(this.fIndex);
		return this.fIndex;
	}

	public int previous() {
		this.fIndex = preceding(this.fIndex);
		return this.fIndex;
	}

	public int preceding(int offset) {
		int first = this.fIterator.preceding(offset);
		if (isWhitespace(first, offset)) {
			int second = this.fIterator.preceding(first);
			if ((second != -1) && (!isDelimiter(second, first)))
				return second;
		}
		return first;
	}

	public int following(int offset) {
		int first = this.fIterator.following(offset);
		if (eatFollowingWhitespace(offset, first)) {
			int second = this.fIterator.following(first);
			if (isWhitespace(first, second))
				return second;
		}
		return first;
	}

	private boolean eatFollowingWhitespace(int offset, int exclusiveEnd) {
		if ((exclusiveEnd == -1) || (offset == -1)) {
			return false;
		}
		if (isWhitespace(offset, exclusiveEnd))
			return false;
		if (isDelimiter(offset, exclusiveEnd)) {
			return false;
		}
		return true;
	}

	private boolean isDelimiter(int offset, int exclusiveEnd) {
		if ((exclusiveEnd == -1) || (offset == -1)) {
			return false;
		}
		Assert.isTrue(offset >= 0);
		Assert.isTrue(exclusiveEnd <= getText().getEndIndex());
		Assert.isTrue(exclusiveEnd > offset);

		CharSequence seq = this.fIterator.fText;

		while (offset < exclusiveEnd) {
			char ch = seq.charAt(offset);
			if ((ch != '\n') && (ch != '\r'))
				return false;
			offset++;
		}

		return true;
	}

	private boolean isWhitespace(int offset, int exclusiveEnd) {
		if ((exclusiveEnd == -1) || (offset == -1)) {
			return false;
		}
		Assert.isTrue(offset >= 0);
		Assert.isTrue(exclusiveEnd <= getText().getEndIndex());
		Assert.isTrue(exclusiveEnd > offset);

		CharSequence seq = this.fIterator.fText;

		while (offset < exclusiveEnd) {
			char ch = seq.charAt(offset);
			if (!Character.isWhitespace(ch))
				return false;
			if ((ch == '\n') || (ch == '\r'))
				return false;
			offset++;
		}

		return true;
	}

	public int current() {
		return this.fIndex;
	}

	public CharacterIterator getText() {
		return this.fIterator.getText();
	}

	public void setText(CharSequence newText) {
		this.fIterator.setText(newText);
		first();
	}

	public void setText(CharacterIterator newText) {
		this.fIterator.setText(newText);
		first();
	}

	public void setText(String newText) {
		setText(newText);
	}
}