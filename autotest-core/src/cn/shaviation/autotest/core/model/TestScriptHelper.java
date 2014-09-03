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

	public static Collection<String> validate(TestScript testScript) {
		Set<String> messages = new LinkedHashSet<String>();
		addMessages(messages, Validators.validate(testScript));
		if (testScript.getTestSteps() != null) {
			for (TestStep step : testScript.getTestSteps()) {
				addMessages(messages, Validators.validate(step));
				if (step.getParameters() != null) {
					for (Parameter param : step.getParameters()) {
						addMessages(messages, Validators.validate(param));
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
