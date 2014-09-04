package cn.shaviation.autotest.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TestStepIterator {

	public static interface ITestStepVisitor {

		boolean visit(TestStep testStep, int index);
	}

	private static final ITestStepVisitor EMPTY = new ITestStepVisitor() {
		@Override
		public boolean visit(TestStep testStep, int index) {
			return true;
		}
	};

	@SuppressWarnings("serial")
	public static class TestStepIteratorException extends Exception {

		private int index;

		public TestStepIteratorException(int index) {
			super();
			this.index = index;
		}

		public TestStepIteratorException(int index, String message) {
			super(message);
			this.index = index;
		}

		public int getIndex() {
			return index;
		}
	}

	public static void iterate(List<TestStep> testSteps,
			ITestStepVisitor visitor) throws TestStepIteratorException {
		Map<Integer, Boolean> context = new HashMap<Integer, Boolean>();
		for (int i = 0; i < testSteps.size(); i++) {
			if (!walk(testSteps, i + 1, context, visitor)) {
				break;
			}
		}
	}

	private static boolean walk(List<TestStep> testSteps, int index,
			Map<Integer, Boolean> context, ITestStepVisitor visitor)
			throws TestStepIteratorException {
		if (index < 1 || index > testSteps.size()) {
			throw new TestStepIteratorException(index, "index out of bounds");
		}
		if (!context.containsKey(index)) {
			context.put(index, null);
			TestStep step = testSteps.get(index - 1);
			boolean result = true;
			if (step.getDependentSteps() != null) {
				for (Integer i : step.getDependentSteps()) {
					result = walk(testSteps, i, context, visitor);
					if (!result) {
						break;
					}
				}
			}
			if (result) {
				result = visitor.visit(step, index);
			}
			context.put(index, result);
			return result;
		} else {
			Boolean result = context.get(index);
			if (result == null) {
				throw new TestStepIteratorException(index, "dead loop");
			}
			return result;
		}
	}

	public static void check(List<TestStep> testSteps)
			throws TestStepIteratorException {
		iterate(testSteps, EMPTY);
	}
}
