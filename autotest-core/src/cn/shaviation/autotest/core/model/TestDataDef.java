package cn.shaviation.autotest.core.model;

import java.util.Date;
import java.util.List;

import org.hibernate.validator.constraints.NotBlank;

import cn.shaviation.autotest.core.internal.jsr303.Unique;
import cn.shaviation.autotest.core.util.Objects;
import cn.shaviation.autotest.core.util.PropertyChangeSupportBean;

public class TestDataDef extends PropertyChangeSupportBean {

	@NotBlank(message = "{testDataDef.name.NotBlank}")
	private String name;

	private String description;

	private String author;

	private Date lastUpdateTime;

	@Unique(property = "name", message = "{testDataGroup.name.Unique}")
	private List<TestDataGroup> dataList;

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

	public List<TestDataGroup> getDataList() {
		return dataList;
	}

	public void setDataList(List<TestDataGroup> dataList) {
		if (!Objects.equals(this.dataList, dataList)) {
			firePropertyChange("dataList", this.dataList,
					this.dataList = dataList);
		}
	}
}
