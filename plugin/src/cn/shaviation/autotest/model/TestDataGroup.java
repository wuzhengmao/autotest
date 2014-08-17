package cn.shaviation.autotest.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hibernate.validator.constraints.NotBlank;

public class TestDataGroup {

	@NotBlank(message = "{testDataGroup.name.NotBlank}")
	private String name;

	private List<TestDataEntry> entries;

	public TestDataGroup() {

	}

	public TestDataGroup(TestDataEntry... entries) {
		this.entries = new ArrayList<TestDataEntry>(Arrays.asList(entries));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<TestDataEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<TestDataEntry> entries) {
		this.entries = entries;
	}
}
