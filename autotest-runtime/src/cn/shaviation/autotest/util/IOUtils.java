package cn.shaviation.autotest.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class IOUtils {

	public static void copy(InputStream is, OutputStream os, int bufferSize)
			throws IOException {
		byte[] buffer = new byte[bufferSize];
		int i;
		while ((i = is.read(buffer)) >= 0) {
			if (i > 0) {
				os.write(buffer, 0, i);
			}
		}
		os.flush();
	}

	public static String toString(InputStream is, String charset)
			throws IOException {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream(4096);
			copy(is, os, 1024);
			return os.toString(charset);
		} finally {
			is.close();
		}
	}

	public static void saveFile(InputStream is, File file) throws IOException {
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(file);
			copy(is, os, 4096);
		} finally {
			is.close();
			if (os != null) {
				os.close();
			}
		}
	}
}
