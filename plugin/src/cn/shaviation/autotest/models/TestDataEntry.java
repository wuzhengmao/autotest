package cn.shaviation.autotest.models;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

public class TestDataEntry {

	public enum Type {
		Input, Output
	}

	@NotBlank(message = "{testDataEntry.key.NotBlank}")
	private String key;

	private String value;

	@NotNull(message = "{testDataEntry.type.NotNull}")
	private Type type = Type.Input;

	private String memo;

	public TestDataEntry() {

	}

	public TestDataEntry(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}
}
