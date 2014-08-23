package cn.shaviation.autotest.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class IOUtils {

	public static String toString(InputStream is, String charset)
			throws IOException {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream(4096);
			byte[] buffer = new byte[1024];
			int i;
			while ((i = is.read(buffer)) >= 0) {
				if (i > 0) {
					os.write(buffer, 0, i);
				}
			}
			return os.toString(charset);
		} finally {
			is.close();
		}
	}
}
