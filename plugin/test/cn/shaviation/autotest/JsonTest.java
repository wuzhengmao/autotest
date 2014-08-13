package cn.shaviation.autotest;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import cn.shaviation.autotest.model.TestData;
import cn.shaviation.autotest.model.TestDataDef;
import cn.shaviation.autotest.model.TestDataEntry;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonTest {

	@Test
	public void test() throws Exception {
		TestData data1 = new TestData(new TestDataEntry[] {
				new TestDataEntry("1", "100"), new TestDataEntry("2", "200"),
				new TestDataEntry("3", "300") });
		data1.setIndex(1);
		TestData data2 = new TestData(new TestDataEntry[] {
				new TestDataEntry("a", "100"), new TestDataEntry("b", "200"),
				new TestDataEntry("c", "300") });
		data2.setIndex(2);
		TestDataDef def = new TestDataDef();
		def.setId("test");
		def.setName("测试");
		def.setDataList(Arrays.asList(new TestData[] {data1, data2}));
		def.setAuthor("Mingy");
		def.setCreationTime(new Date());
		def.setLastUpdateTime(def.getCreationTime());
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.setSerializationInclusion(Include.NON_EMPTY);
		mapper.setDateFormat(new SimpleDateFormat("yyyyMMddHHmmssSSS"));
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		System.out.println(mapper.writeValueAsString(def));
	}
}
