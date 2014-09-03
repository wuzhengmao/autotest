package cn.shaviation.autotest.core.model;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;

import cn.shaviation.autotest.core.util.Validators;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public abstract class TestDataHelper {

	private static final ObjectMapper objectMapper = new ObjectMapper()
			.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
			.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
			.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
			.configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true)
			.enable(SerializationFeature.INDENT_OUTPUT)
			.setSerializationInclusion(Include.NON_EMPTY)
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.setDateFormat(new SimpleDateFormat("yyyyMMddHHmmssSSS"));

	public static TestDataDef parse(String json) throws IOException {
		return objectMapper.readValue(json, TestDataDef.class);
	}

	public static String serialize(TestDataDef testDataDef) throws IOException {
		return objectMapper.writeValueAsString(testDataDef);
	}

	public static TestDataDef createDefault() {
		TestDataDef testDataDef = new TestDataDef();
		testDataDef.setName("New Test Data");
		testDataDef.setAuthor(System.getProperty("user.name"));
		testDataDef.setLastUpdateTime(new Date());
		return testDataDef;
	}

	public static Collection<String> validate(TestDataDef testDataDef) {
		Set<String> messages = new LinkedHashSet<String>();
		addMessages(messages, Validators.validate(testDataDef));
		if (testDataDef.getDataList() != null) {
			for (TestDataGroup group : testDataDef.getDataList()) {
				addMessages(messages, Validators.validate(group));
				if (group.getEntries() != null) {
					for (TestDataEntry entry : group.getEntries()) {
						addMessages(messages, Validators.validate(entry));
					}
				}
			}
		}
		return messages;
	}

	private static <T> void addMessages(Set<String> messages,
			Set<ConstraintViolation<T>> violations) {
		for (ConstraintViolation<T> violation : violations) {
			messages.add(violation.getMessage());
		}
	}
}
