package cn.shaviation.autotest.model;

import cn.shaviation.autotest.internal.jsr303.NotBlank;
import cn.shaviation.autotest.util.Objects;
import cn.shaviation.autotest.util.PropertyChangeSupportBean;

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
