package cn.shaviation.autotest.core.model;

import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import cn.shaviation.autotest.core.internal.jsr303.Unique;
import cn.shaviation.autotest.core.util.Objects;
import cn.shaviation.autotest.core.util.PropertyChangeSupportBean;
import cn.shaviation.autotest.core.util.Strings;

public class TestStep extends PropertyChangeSupportBean {

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
		String oldValue = Strings.merge(this.dependentSteps, ",");
		String newValue = Strings.merge(dependentSteps, ",");
		if (!oldValue.equals(newValue)) {
			firePropertyChange("dependentSteps", this.dependentSteps,
					this.dependentSteps = dependentSteps);
		}
	}

	public Type getInvokeType() {
		return invokeType;
	}

	public void setInvokeType(Type invokeType) {
		if (!Objects.equals(this.invokeType, invokeType)) {
			firePropertyChange("invokeType", this.invokeType,
					this.invokeType = invokeType);
		}
	}

	public String getInvokeTarget() {
		return invokeTarget;
	}

	public void setInvokeTarget(String invokeTarget) {
		if (!Objects.equals(this.invokeTarget, invokeTarget)) {
			firePropertyChange("invokeTarget", this.invokeTarget,
					this.invokeTarget = invokeTarget);
		}
	}

	public String getTestDataFile() {
		return testDataFile;
	}

	public void setTestDataFile(String testDataFile) {
		if (!Objects.equals(this.testDataFile, testDataFile)) {
			firePropertyChange("testDataFile", this.testDataFile,
					this.testDataFile = testDataFile);
		}
	}

	public Integer getLoopTimes() {
		return loopTimes;
	}

	public void setLoopTimes(Integer loopTimes) {
		if (!Objects.equals(this.loopTimes, loopTimes)) {
			firePropertyChange("loopTimes", this.loopTimes,
					this.loopTimes = loopTimes);
		}
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		if (!Objects.equals(this.parameters, parameters)) {
			firePropertyChange("parameters", this.parameters,
					this.parameters = parameters);
		}
	}
}
