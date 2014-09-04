package cn.shaviation.autotest.model;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public abstract class TestScriptHelper {

	private static final ObjectMapper objectMapper = new ObjectMapper()
			.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
			.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
			.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
			.configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true)
			.enable(SerializationFeature.INDENT_OUTPUT)
			.setSerializationInclusion(Include.NON_EMPTY)
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.setDateFormat(new SimpleDateFormat("yyyyMMddHHmmssSSS"));

	public static TestScript parse(String json) throws IOException {
		return objectMapper.readValue(json, TestScript.class);
	}

	public static String serialize(TestScript testScript) throws IOException {
		return objectMapper.writeValueAsString(testScript);
	}

	public static TestScript createDefault() {
		TestScript testScript = new TestScript();
		testScript.setName("New Test Script");
		testScript.setAuthor(System.getProperty("user.name"));
		testScript.setLastUpdateTime(new Date());
		return testScript;
	}
}
