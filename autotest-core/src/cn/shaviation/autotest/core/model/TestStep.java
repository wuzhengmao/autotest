package cn.shaviation.autotest.core.model;

import java.util.List;
import java.util.Set;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import cn.shaviation.autotest.core.internal.jsr303.Unique;

public class TestStep {

	public static enum Type {
		TestMethod, TestScript
	}

	private Set<Integer> dependentSteps;

	@NotNull(message = "{testStep.invokeType.NotNull}")
	private Type invokeType;

	@NotBlank(message = "{testStep.invokeTarget.NotBlank}")
	private String invokeTarget;

	private String testDataFile;

	@Min(value = 1, message = "{testStep.loopTimes.Min}")
	private int loopTimes = 1;

	@Unique(property = "key", message = "{parameter.key.Unique}")
	private List<Parameter> parameters;

	public Set<Integer> getDependentSteps() {
		return dependentSteps;
	}

	public void setDependentSteps(Set<Integer> dependentSteps) {
		this.dependentSteps = dependentSteps;
	}

	public Type getInvokeType() {
		return invokeType;
	}

	public void setInvokeType(Type invokeType) {
		this.invokeType = invokeType;
	}

	public String getInvokeTarget() {
		return invokeTarget;
	}

	public void setInvokeTarget(String invokeTarget) {
		this.invokeTarget = invokeTarget;
	}

	public String getTestDataFile() {
		return testDataFile;
	}

	public void setTestDataFile(String testDataFile) {
		this.testDataFile = testDataFile;
	}

	public int getLoopTimes() {
		return loopTimes;
	}

	public void setLoopTimes(int loopTimes) {
		this.loopTimes = loopTimes;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}
}
