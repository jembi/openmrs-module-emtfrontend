package org.openmrs.module.emtfrontend;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Helper {
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
	
	public static boolean inPeriod(Date startDate, Date endDate, String line)
			throws ParseException {
		return inPeriod(startDate, endDate, sdf.parse(line));
	}

	public static boolean inPeriod(Date startDate, Date endDate, Date date) {
		return (startDate.before(date) && endDate.after(date));
	}

	public static boolean inPeriod(Date startDate, Date endDate, Heartbeatable hb) {
		return inPeriod(startDate, endDate, hb.date());
	}

	public static boolean duringClinic(Heartbeatable hb) {
		return duringClinic(hb.date());
	}

	public static boolean duringClinic(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		boolean matchingDay = false;
		switch (c.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.MONDAY:
			if (Emt.clinicDays.contains("Mo"))
				matchingDay = true;
			break;
		case Calendar.TUESDAY:
			if (Emt.clinicDays.contains("Tu"))
				matchingDay = true;
			break;
		case Calendar.WEDNESDAY:
			if (Emt.clinicDays.contains("We"))
				matchingDay = true;
			break;
		case Calendar.THURSDAY:
			if (Emt.clinicDays.contains("Th"))
				matchingDay = true;
			break;
		case Calendar.FRIDAY:
			if (Emt.clinicDays.contains("Fr"))
				matchingDay = true;
			break;
		case Calendar.SATURDAY:
			if (Emt.clinicDays.contains("Sa"))
				matchingDay = true;
			break;
		case Calendar.SUNDAY:
			if (Emt.clinicDays.contains("Su"))
				matchingDay = true;
			break;
		}

		boolean matchingTime = false;
		String hours = String.format("%02d", c.get(Calendar.HOUR_OF_DAY)); // preserve
																			// leading
																			// zeros
		String minutes = String.format("%02d", c.get(Calendar.MINUTE)); // preserve
																		// leading
																		// zeros
		// unsure if this simple time range check works in all cases
		int time = Integer.parseInt(hours + minutes);
		matchingTime = Emt.clinicStart <= time && Emt.clinicStop > time;
		return matchingDay && matchingTime;
	}


}
