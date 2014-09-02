package cn.shaviation.autotest.core.model;

import org.hibernate.validator.constraints.NotBlank;

import cn.shaviation.autotest.core.util.Objects;
import cn.shaviation.autotest.core.util.PropertyChangeSupportBean;

public class Parameter extends PropertyChangeSupportBean {

	@NotBlank(message = "{parameter.key.NotBlank}")
	private String key;

	private String value;

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

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		if (!Objects.equals(this.memo, memo)) {
			firePropertyChange("memo", this.memo, this.memo = memo);
		}
	}
}
