package cn.shavation.autotest.runner;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;

import cn.shaviation.autotest.internal.runner.TestNodeImpl;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public abstract class TestExecutionHelper {

	private static final ObjectMapper objectMapper = new ObjectMapper()
			.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
			.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
			.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
			.configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true)
			.enable(SerializationFeature.INDENT_OUTPUT)
			.setSerializationInclusion(Include.NON_EMPTY)
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.setDateFormat(new SimpleDateFormat("yyyyMMddHHmmssSSS"));

	public static TestExecution parse(String json) throws IOException {
		return objectMapper.readValue(json, TestNodeImpl.class);
	}

	public static TestExecution parse(Reader reader) throws IOException {
		return objectMapper.readValue(reader, TestNodeImpl.class);
	}

	public static String serialize(TestExecution execution) throws IOException {
		return objectMapper.writeValueAsString(execution);
	}

	public static void serialize(Writer writer, TestExecution execution)
			throws IOException {
		objectMapper.writeValue(writer, execution);
	}
}
