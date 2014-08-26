package cn.shaviation.autotest.core.model;

import java.util.Date;
import java.util.List;

import org.hibernate.validator.constraints.NotBlank;

public class TestScript {

	@NotBlank(message = "{testScript.name.NotBlank}")
	private String name;

	private String description;

	private List<TestStep> testSteps;

	private String author;

	private Date lastUpdateTime;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<TestStep> getTestSteps() {
		return testSteps;
	}

	public void setTestSteps(List<TestStep> testSteps) {
		this.testSteps = testSteps;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(Date lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
}
