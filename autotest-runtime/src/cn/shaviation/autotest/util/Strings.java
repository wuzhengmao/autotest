package cn.shaviation.autotest.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public abstract class Strings {

	public static final String EMPTY = "";

	public static boolean isEmpty(String str) {
		return str == null || str.isEmpty();
	}

	public static boolean isBlank(String str) {
		return str == null || str.trim().isEmpty();
	}

	public static boolean equals(Object value1, Object value2) {
		String str1 = Objects.toString(value1);
		String str2 = Objects.toString(value2);
		return str1.equals(str2);
	}

	public static String merge(Collection<?> objs, String delimiter) {
		StringBuilder sb = new StringBuilder();
		if (objs != null) {
			int i = 0;
			for (Object obj : objs) {
				if (i++ > 0) {
					sb.append(delimiter);
				}
				if (obj != null) {
					sb.append(obj.toString());
				}
			}
		}
		return sb.toString();
	}

	public static List<String> split(String text, String delimiter) {
		return split(text, delimiter, String.class, false, false);
	}

	public static <T> List<T> split(String text, String delimiter,
			Class<T> type, boolean ignoreNull, boolean ignoreDuplicate) {
		if (text == null) {
			return Collections.emptyList();
		}
		List<T> list = new ArrayList<T>();
		int i = 0, j;
		while ((j = text.indexOf(delimiter, i)) >= 0) {
			String str = text.substring(i, j);
			i = j + delimiter.length();
			T value = null;
			try {
				value = Objects.toObject(str, type);
			} catch (Exception e) {
			}
			if (!ignoreNull || value != null) {
				if (!ignoreDuplicate || !list.contains(value)) {
					list.add(value);
				}
			}
		}
		String str = text.substring(i);
		T value = null;
		try {
			value = Objects.toObject(str, type);
		} catch (Exception e) {
		}
		if (!ignoreNull || value != null) {
			if (!ignoreDuplicate || !list.contains(value)) {
				list.add(value);
			}
		}
		return list;
	}

	private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d*");

	public static boolean isNumber(String str) {
		return NUMBER_PATTERN.matcher(str).matches();
	}

	public static String replace(String inString, String oldPattern,
			String newPattern) {
		if (isEmpty(inString) || isEmpty(oldPattern) || newPattern == null) {
			return inString;
		}
		StringBuilder sb = new StringBuilder();
		int pos = 0; // our position in the old string
		int index = inString.indexOf(oldPattern);
		// the index of an occurrence we've found, or -1
		int patLen = oldPattern.length();
		while (index >= 0) {
			sb.append(inString.substring(pos, index));
			sb.append(newPattern);
			pos = index + patLen;
			index = inString.indexOf(oldPattern, pos);
		}
		sb.append(inString.substring(pos));
		// remember to append any characters to the right of a match
		return sb.toString();
	}

	public static String cleanPath(String path) {
		if (path == null) {
			return null;
		}
		String pathToUse = replace(path, "\\", "/");
		int prefixIndex = pathToUse.indexOf(":");
		String prefix = "";
		if (prefixIndex != -1) {
			prefix = pathToUse.substring(0, prefixIndex + 1);
			pathToUse = pathToUse.substring(prefixIndex + 1);
		}
		if (pathToUse.startsWith("/")) {
			prefix = prefix + "/";
			pathToUse = pathToUse.substring(1);
		}
		List<String> pathArray = split(pathToUse, "/");
		List<String> pathElements = new LinkedList<String>();
		int tops = 0;
		for (int i = pathArray.size() - 1; i >= 0; i--) {
			String element = pathArray.get(i);
			if (".".equals(element)) {
				// Points to current directory - drop it.
			} else if ("..".equals(element)) {
				// Registering top path found.
				tops++;
			} else {
				if (tops > 0) {
					// Merging path element with element corresponding to top
					// path.
					tops--;
				} else {
					// Normal path element found.
					pathElements.add(0, element);
				}
			}
		}
		// Remaining top paths need to be retained.
		for (int i = 0; i < tops; i++) {
			pathElements.add(0, "..");
		}
		return prefix + merge(pathElements, "/");
	}

	public static String applyRelativePath(String path, String relativePath) {
		int separatorIndex = path.lastIndexOf('/');
		if (separatorIndex != -1) {
			String newPath = path.substring(0, separatorIndex);
			if (!relativePath.startsWith("/")) {
				newPath += "/";
			}
			return newPath + relativePath;
		} else {
			return relativePath;
		}
	}

	public static String formatYMD(Date date) {
		return new SimpleDateFormat("yyyyMMdd").format(date);
	}

	public static String formatHMS(Date date) {
		return new SimpleDateFormat("HHmmss").format(date);
	}

	public static String encodeUrl(String url) {
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return url;
		}
	}

	public static String decodeUrl(String url) {
		try {
			return URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return url;
		}
	}

	public static String escapeJavaString(String str) {
		if (isEmpty(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder();
		int sz;
		sz = str.length();
		for (int i = 0; i < sz; i++) {
			char ch = str.charAt(i);
			// handle unicode
			if (ch > 0xfff) {
				sb.append("\\u" + hex(ch));
			} else if (ch > 0xff) {
				sb.append("\\u0" + hex(ch));
			} else if (ch > 0x7f) {
				sb.append("\\u00" + hex(ch));
			} else if (ch < 32) {
				switch (ch) {
				case '\b':
					sb.append('\\');
					sb.append('b');
					break;
				case '\n':
					sb.append('\\');
					sb.append('n');
					break;
				case '\t':
					sb.append('\\');
					sb.append('t');
					break;
				case '\f':
					sb.append('\\');
					sb.append('f');
					break;
				case '\r':
					sb.append('\\');
					sb.append('r');
					break;
				default:
					if (ch > 0xf) {
						sb.append("\\u00" + hex(ch));
					} else {
						sb.append("\\u000" + hex(ch));
					}
					break;
				}
			} else {
				switch (ch) {
				case '"':
					sb.append('\\');
					sb.append('"');
					break;
				case '\\':
					sb.append('\\');
					sb.append('\\');
					break;
				default:
					sb.append(ch);
					break;
				}
			}
		}
		return sb.toString();
	}

	private static String hex(char ch) {
		return Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
	}

	public static String unescapeJavaString(String str) {
		if (isEmpty(str)) {
			return str;
		}
		StringBuilder sb = new StringBuilder();
		int sz = str.length();
		StringBuilder unicode = new StringBuilder(4);
		boolean hadSlash = false;
		boolean inUnicode = false;
		for (int i = 0; i < sz; i++) {
			char ch = str.charAt(i);
			if (inUnicode) {
				// if in unicode, then we're reading unicode
				// values in somehow
				unicode.append(ch);
				if (unicode.length() == 4) {
					// unicode now contains the four hex digits
					// which represents our unicode character
					try {
						int value = Integer.parseInt(unicode.toString(), 16);
						sb.append((char) value);
						unicode.setLength(0);
						inUnicode = false;
						hadSlash = false;
					} catch (NumberFormatException nfe) {
						throw new RuntimeException(
								"Unable to parse unicode value: " + unicode,
								nfe);
					}
				}
				continue;
			}
			if (hadSlash) {
				// handle an escaped value
				hadSlash = false;
				switch (ch) {
				case '\\':
					sb.append('\\');
					break;
				case '\'':
					sb.append('\'');
					break;
				case '\"':
					sb.append('"');
					break;
				case 'r':
					sb.append('\r');
					break;
				case 'f':
					sb.append('\f');
					break;
				case 't':
					sb.append('\t');
					break;
				case 'n':
					sb.append('\n');
					break;
				case 'b':
					sb.append('\b');
					break;
				case 'u': {
					// uh-oh, we're in unicode country....
					inUnicode = true;
					break;
				}
				default:
					sb.append(ch);
					break;
				}
				continue;
			} else if (ch == '\\') {
				hadSlash = true;
				continue;
			}
			sb.append(ch);
		}
		if (hadSlash) {
			// then we're in the weird case of a \ at the end of the
			// string, let's output it anyway.
			sb.append('\\');
		}
		return sb.toString();
	}
}
