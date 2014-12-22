package cn.shaviation.autotest.runner.spi.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import cn.shaviation.autotest.util.Strings;

public class DateExpressionResolverImpl extends AbstractExpressionResolver {

	private static final SimpleDateFormat SDF10 = new SimpleDateFormat(
			"yyyy-MM-dd");
	private static final SimpleDateFormat SDF8 = new SimpleDateFormat(
			"HH:mm:ss");
	private static final SimpleDateFormat SDF19 = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private static final SimpleDateFormat SDF23 = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");

	@Override
	public String subject() {
		return "$d";
	}

	public String date() {
		return SDF10.format(new Date());
	}

	public String time() {
		return SDF8.format(new Date());
	}

	public String datetime() {
		return SDF19.format(new Date());
	}

	public String timestamp() {
		return SDF23.format(new Date());
	}

	public String date(Date date) {
		return SDF10.format(date);
	}

	public String time(Date date) {
		return SDF8.format(date);
	}

	public String datetime(Date date) {
		return SDF19.format(date);
	}

	public String timestamp(Date date) {
		return SDF23.format(date);
	}

	public String format(String pattern) {
		return new SimpleDateFormat(pattern).format(new Date());
	}

	public String format(Date date, String pattern) {
		return new SimpleDateFormat(pattern).format(date);
	}

	public Date now() {
		return new Date();
	}

	public Date todate(String date) throws ParseException {
		return SDF19.parse(date);
	}

	public Date todate(String date, String pattern) throws ParseException {
		return new SimpleDateFormat(pattern).parse(date);
	}

	public Date diff(Date date, String delta) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		StringTokenizer st = new StringTokenizer(delta, "yMdhms", true);
		while (st.hasMoreTokens()) {
			String num = st.nextToken().trim();
			char c = st.nextToken().trim().charAt(0);
			if (!Strings.isEmpty(num)) {
				int n = Integer.parseInt(num);
				switch (c) {
				case 'y':
					cal.add(Calendar.YEAR, n);
					break;
				case 'M':
					cal.add(Calendar.MONTH, n);
					break;
				case 'd':
					cal.add(Calendar.DAY_OF_MONTH, n);
					break;
				case 'h':
					cal.add(Calendar.HOUR_OF_DAY, n);
					break;
				case 'm':
					cal.add(Calendar.MINUTE, n);
					break;
				case 's':
					cal.add(Calendar.SECOND, n);
					break;
				}
			}
		}
		return cal.getTime();
	}
}
