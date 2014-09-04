package cn.shaviation.autotest.model;

import java.util.Date;
import java.util.List;

import cn.shaviation.autotest.internal.jsr303.NotBlank;
import cn.shaviation.autotest.internal.jsr303.TestDataDependence;
import cn.shaviation.autotest.util.Objects;
import cn.shaviation.autotest.util.PropertyChangeSupportBean;

public class TestScript extends PropertyChangeSupportBean {

	@NotBlank(message = "{testScript.name.NotBlank}")
	private String name;

	private String description;

	private String author;

	private Date lastUpdateTime;

	@TestDataDependence(message = "{testStep.dependentSteps.invalid}")
	private List<TestStep> testSteps;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (!Objects.equals(this.name, name)) {
			firePropertyChange("name", this.name, this.name = name);
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if (!Objects.equals(this.description, description)) {
			firePropertyChange("description", this.description,
					this.description = description);
		}
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		if (!Objects.equals(this.author, author)) {
			firePropertyChange("author", this.author, this.author = author);
		}
	}

	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(Date lastUpdateTime) {
		if (!Objects.equals(this.lastUpdateTime, lastUpdateTime)) {
			firePropertyChange("lastUpdateTime", this.lastUpdateTime,
					this.lastUpdateTime = lastUpdateTime);
		}
	}

	public List<TestStep> getTestSteps() {
		return testSteps;
	}

	public void setTestSteps(List<TestStep> testSteps) {
		if (!Objects.equals(this.testSteps, testSteps)) {
			firePropertyChange("testSteps", this.testSteps,
					this.testSteps = testSteps);
		}
	}
}
