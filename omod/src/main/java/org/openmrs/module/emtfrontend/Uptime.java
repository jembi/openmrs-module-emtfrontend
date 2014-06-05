package org.openmrs.module.emtfrontend;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Uptime {

	int heartBeatsDuringClinic = 0;
	int expectedHeartbeats = 0;
	
	public void calcHeartbearts(Date startDate,
			Date endDate, List<Heartbeatable> heartbeats, int cronjobIntervallInMinutes) {
		// count number of recorded heartbeats during clinic times
		for (Heartbeatable hb : heartbeats) {
			if (Helper.inPeriod(startDate, endDate, hb) && Helper.duringClinic(hb)) {
				heartBeatsDuringClinic++;
			}
		}

		// calculate number of expected heartbeats during clinic times
		Calendar date = Calendar.getInstance();
		date.setTime(startDate);
		// if specified end date is in future, take current time as end date;
		// else enddate
		Calendar end = Calendar.getInstance();
		if (endDate.before(new Date())) {
			end.setTime(endDate);
		}
		int uniqueClinicDays = 1;
		long lastUniqueDate = date.getTimeInMillis();
		while (date.before(end)) {
			if (Helper.duringClinic(date.getTime())) {
				expectedHeartbeats++;
				int diffInDays = (int) ((date.getTimeInMillis() - lastUniqueDate) / (1000 * 60 * 60 * 24));
				if (diffInDays > 0) {
					uniqueClinicDays++;
					lastUniqueDate = date.getTimeInMillis();
				}
			}
			date.add(Calendar.MINUTE, cronjobIntervallInMinutes);
		}

		// (over?) simplification to ignore first and last heartbeat of each clinic day 
		// (as they might not have happened depending on their start)
		expectedHeartbeats = expectedHeartbeats - uniqueClinicDays;
		if (expectedHeartbeats < heartBeatsDuringClinic) {
			expectedHeartbeats = heartBeatsDuringClinic;
		}
		
	}
		public String calcPercentage() {
			// calc uptime percentage
			if (expectedHeartbeats > 0) {
				double uptime = (double) heartBeatsDuringClinic
						/ (double) expectedHeartbeats * 100;
				BigDecimal bd = new BigDecimal(uptime);
				bd = bd.setScale(2, RoundingMode.HALF_UP);
				return "" + bd.doubleValue() + " %" + " (" + heartBeatsDuringClinic
						+ "/" + expectedHeartbeats + "*100)";
			} else {
				return "Not a number" + " (" + heartBeatsDuringClinic + "/"
						+ expectedHeartbeats + "*100)";
			}

		}
	
}
