package cn.shaviation.autotest.model;

import javax.validation.constraints.NotNull;

import cn.shaviation.autotest.internal.jsr303.NotBlank;
import cn.shaviation.autotest.util.Objects;
import cn.shaviation.autotest.util.PropertyChangeSupportBean;

public class TestDataEntry extends PropertyChangeSupportBean {

	public static enum Type {
		Input, Output
	}

	@NotBlank(message = "{testDataEntry.key.NotBlank}")
	private String key;

	private String value;

	@NotNull(message = "{testDataEntry.type.NotNull}")
	private Type type = Type.Input;

	private String memo;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		if (!Objects.equals(this.key, key)) {
			firePropertyChange("key", this.key, this.key = key);
		}
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		if (!Objects.equals(this.value, value)) {
			firePropertyChange("value", this.value, this.value = value);
		}
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		if (!Objects.equals(this.type, type)) {
			firePropertyChange("type", this.type, this.type = type);
		}
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		if (!Objects.equals(this.memo, memo)) {
			firePropertyChange("memo", this.memo, this.memo = memo);
		}
	}
}
