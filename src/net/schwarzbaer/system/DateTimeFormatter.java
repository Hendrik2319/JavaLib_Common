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
		Vector<String> formatParts = new Vector<>(10);
		if (withTextDay) formatParts.add("%1$tA,");
		if (withDate) {
			if (dateIsLong) {
				formatParts.add("%1$te.");
				formatParts.add("%1$tb" );
				formatParts.add("%1$tY,");
			} else{
				formatParts.add("%1$td.");
				formatParts.add("%1$tm.");
				formatParts.add("%1$ty,");
			}
		}
		if (withTime) formatParts.add("%1$tT");
		if (withTimeZone) formatParts.add("[%1$tZ:%1$tz]");
		
		String format = String.join(" ", formatParts);
		return String.format(Locale.ENGLISH, format, cal);
	}
}
