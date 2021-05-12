package net.schwarzbaer.system;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

public class DateTimeFormatter {
	
	private Calendar cal;

	public DateTimeFormatter() {
		cal = Calendar.getInstance(TimeZone.getTimeZone("CET"), Locale.GERMANY);
	}

	public String getTimeStr(long millis, boolean withTextDay, boolean withDate, boolean dateIsLong, boolean withTime, boolean withTimeZone) {
		cal.setTimeInMillis(millis);
		return getTimeStr(cal, Locale.ENGLISH, withTextDay, withDate, dateIsLong, withTime, withTimeZone);
	}

	public static String getTimeStr(Calendar cal, boolean withTextDay, boolean withDate, boolean dateIsLong, boolean withTime, boolean withTimeZone) {
		return getTimeStr(cal, Locale.ENGLISH, withTextDay, withDate, dateIsLong, withTime, withTimeZone);
	}

	public static String getTimeStr(Calendar cal, Locale locale, boolean withTextDay, boolean withDate, boolean dateIsLong, boolean withTime, boolean withTimeZone) {
		Vector<String> formatParts = new Vector<>(10);
		if (withTextDay) formatParts.add("%1$tA,");
		if (withDate) {
			if (dateIsLong) {
				formatParts.add("%1$te.");
				formatParts.add("%1$tb" );
				formatParts.add("%1$tY,");
			} else{
				formatParts.add("%1$td.%1$tm.%1$ty,");
			}
		}
		if (withTime) formatParts.add("%1$tT");
		if (withTimeZone) formatParts.add("[%1$tZ:%1$tz]");
		
		String format = String.join(" ", formatParts);
		return String.format(locale, format, cal);
	}
	
	public static String getDurationStr(long duration_sec) {
		long s =  duration_sec      %60;
		long m = (duration_sec/60  )%60;
		long h =  duration_sec/3600;
		
		if (duration_sec < 60)
			return String.format("%d s", s);
		
		if (duration_sec < 3600)
			return String.format("%d:%02d min", m, s);
		
		return String.format("%d:%02d:%02d h", h, m, s);
	}
}
