package cn.shaviation.autotest.model;

import java.util.List;

import cn.shaviation.autotest.internal.jsr303.NotBlank;
import cn.shaviation.autotest.internal.jsr303.Unique;
import cn.shaviation.autotest.util.Objects;
import cn.shaviation.autotest.util.PropertyChangeSupportBean;

public class TestDataGroup extends PropertyChangeSupportBean {

	@NotBlank(message = "{testDataGroup.name.NotBlank}")
	private String name;

	@Unique(property = "key", message = "{testDataEntry.key.Unique}")
	private List<TestDataEntry> entries;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (!Objects.equals(this.name, name)) {
			firePropertyChange("name", this.name, this.name = name);
		}
	}

	public List<TestDataEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<TestDataEntry> entries) {
		if (!Objects.equals(this.entries, entries)) {
			firePropertyChange("entries", this.entries, this.entries = entries);
		}
	}
}
