package cn.shaviation.autotest;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import cn.shaviation.autotest.models.TestDataDef;
import cn.shaviation.autotest.models.TestDataEntry;
import cn.shaviation.autotest.models.TestDataGroup;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonTest {

	@Test
	public void test() throws Exception {
		TestDataGroup data1 = new TestDataGroup(new TestDataEntry[] {
				new TestDataEntry("1", "100"), new TestDataEntry("2", "200"),
				new TestDataEntry("3", "300") });
		data1.setName("group 1");
		TestDataGroup data2 = new TestDataGroup(new TestDataEntry[] {
				new TestDataEntry("a", "100"), new TestDataEntry("b", "200"),
				new TestDataEntry("c", "300") });
		data2.setName("group 2");
		TestDataDef def = new TestDataDef();
		def.setName("测试");
		def.setDataList(Arrays.asList(new TestDataGroup[] { data1, data2 }));
		def.setAuthor("Mingy");
		def.setLastUpdateTime(new Date());
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.setSerializationInclusion(Include.NON_EMPTY);
		mapper.setDateFormat(new SimpleDateFormat("yyyyMMddHHmmssSSS"));
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		System.out.println(mapper.writeValueAsString(def));
	}
}
