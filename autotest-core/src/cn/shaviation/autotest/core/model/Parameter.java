package cn.shaviation.autotest.core.model;

import org.hibernate.validator.constraints.NotBlank;

public class Parameter {

	@NotBlank(message = "{parameter.key.NotBlank}")
	private String key;

	private String value;

	private String memo;

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

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}
}
