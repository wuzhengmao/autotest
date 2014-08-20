package cn.shaviation.autotest.models;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;

import cn.shaviation.autotest.util.Strings;
import cn.shaviation.autotest.util.Validators;

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

	public static String validate(TestDataDef testDataDef) {
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
		return !messages.isEmpty() ? Strings.merge(messages, "\n") : null;
	}

	private static <T> void addMessages(Set<String> messages,
			Set<ConstraintViolation<T>> violations) {
		for (ConstraintViolation<T> violation : violations) {
			messages.add(violation.getMessage());
		}
	}

}
