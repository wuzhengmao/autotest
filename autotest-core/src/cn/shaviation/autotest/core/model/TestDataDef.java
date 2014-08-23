package cn.shaviation.autotest.core.model;

import java.util.Date;
import java.util.List;

import org.hibernate.validator.constraints.NotBlank;

import cn.shaviation.autotest.core.internal.jsr303.Unique;

public class TestDataDef {

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
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(Date lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public List<TestDataGroup> getDataList() {
		return dataList;
	}

	public void setDataList(List<TestDataGroup> dataList) {
		this.dataList = dataList;
	}
}
