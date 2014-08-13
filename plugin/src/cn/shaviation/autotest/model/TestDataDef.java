package cn.shaviation.autotest.model;

import java.util.Date;
import java.util.List;

public class TestDataDef {

	private String id;
	
	private String name;
	
	private String description;
	
	private String author;
	
	private Date creationTime;
	
	private Date lastUpdateTime;
	
	private List<TestData> dataList;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(Date lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public List<TestData> getDataList() {
		return dataList;
	}

	public void setDataList(List<TestData> dataList) {
		this.dataList = dataList;
	}
}
