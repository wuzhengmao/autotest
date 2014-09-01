package cn.shaviation.autotest.core.model;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import cn.shaviation.autotest.core.internal.jsr303.Unique;

public class TestStep {

	public static enum Type {
		Method, Script
	}

	private List<Integer> dependentSteps;

	@NotNull(message = "{testStep.invokeType.NotNull}")
	private Type invokeType;

	@NotBlank(message = "{testStep.invokeTarget.NotBlank}")
	private String invokeTarget;

	private String testDataFile;

	@NotNull(message = "{testStep.loopTimes.NotNull}")
	@Min(value = 1, message = "{testStep.loopTimes.Min}")
	private Integer loopTimes;

	@Unique(property = "key", message = "{parameter.key.Unique}")
	private List<Parameter> parameters;

	public List<Integer> getDependentSteps() {
		return dependentSteps;
	}

	public void setDependentSteps(List<Integer> dependentSteps) {
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

	public Integer getLoopTimes() {
		return loopTimes;
	}

	public void setLoopTimes(Integer loopTimes) {
		this.loopTimes = loopTimes;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}
}
