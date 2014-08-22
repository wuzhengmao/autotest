package cn.shaviation.autotest.text;

import java.text.CharacterIterator;
import org.eclipse.core.runtime.Assert;

public class SequenceCharacterIterator implements CharacterIterator {
	
	private int fIndex = -1;
	private final CharSequence fSequence;
	private final int fFirst;
	private final int fLast;

	private void invariant() {
		Assert.isTrue(this.fIndex >= this.fFirst);
		Assert.isTrue(this.fIndex <= this.fLast);
	}

	public SequenceCharacterIterator(CharSequence sequence) {
		this(sequence, 0);
	}

	public SequenceCharacterIterator(CharSequence sequence, int first)
			throws IllegalArgumentException {
		this(sequence, first, sequence.length());
	}

	public SequenceCharacterIterator(CharSequence sequence, int first, int last)
			throws IllegalArgumentException {
		if (sequence == null)
			throw new NullPointerException();
		if ((first < 0) || (first > last))
			throw new IllegalArgumentException();
		if (last > sequence.length())
			throw new IllegalArgumentException();
		this.fSequence = sequence;
		this.fFirst = first;
		this.fLast = last;
		this.fIndex = first;
		invariant();
	}

	public char first() {
		return setIndex(getBeginIndex());
	}

	public char last() {
		if (this.fFirst == this.fLast) {
			return setIndex(getEndIndex());
		}
		return setIndex(getEndIndex() - 1);
	}

	public char current() {
		if ((this.fIndex >= this.fFirst) && (this.fIndex < this.fLast)) {
			return this.fSequence.charAt(this.fIndex);
		}
		return 65535;
	}

	public char next() {
		return setIndex(Math.min(this.fIndex + 1, getEndIndex()));
	}

	public char previous() {
		if (this.fIndex > getBeginIndex()) {
			return setIndex(this.fIndex - 1);
		}
		return 65535;
	}

	public char setIndex(int position) {
		if ((position >= getBeginIndex()) && (position <= getEndIndex()))
			this.fIndex = position;
		else {
			throw new IllegalArgumentException();
		}
		invariant();
		return current();
	}

	public int getBeginIndex() {
		return this.fFirst;
	}

	public int getEndIndex() {
		return this.fLast;
	}

	public int getIndex() {
		return this.fIndex;
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException localCloneNotSupportedException) {
		}
		throw new InternalError();
	}
}