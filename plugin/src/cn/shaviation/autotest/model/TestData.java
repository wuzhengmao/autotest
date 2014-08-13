package cn.shaviation.autotest.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestData {

	private List<TestDataEntry> entries;

	private int index;

	public TestData() {

	}

	public TestData(TestDataEntry... entries) {
		this.entries = new ArrayList<TestDataEntry>(Arrays.asList(entries));
	}

	public List<TestDataEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<TestDataEntry> entries) {
		this.entries = entries;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
