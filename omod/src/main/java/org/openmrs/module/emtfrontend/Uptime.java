/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emtfrontend;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Uptime {

	int heartBeatsDuringClinic = 0;
	int expectedHeartbeats = 0;
	double percentage = 0;

	public void calcHeartbearts(Date startDate, Date endDate,
			List<Heartbeatable> heartbeats, int cronjobIntervallInMinutes,
			int firstCronjobStartsAtMinute) {
		// count number of recorded heartbeats during clinic times
		for (Heartbeatable hb : heartbeats) {
			if (Helper.inPeriod(startDate, endDate, hb)
					&& Helper.duringClinic(hb)) {
				heartBeatsDuringClinic++;
			}
		}

		// calculate number of expected heartbeats during clinic times
		Calendar date = Calendar.getInstance();
		date.setTime(startDate);
		date.set(Calendar.MINUTE, firstCronjobStartsAtMinute);
		// if specified end date is in future, take current time as end date;
		// else enddate
		Calendar end = Calendar.getInstance();
		if (endDate.before(new Date())) {
			end.setTime(endDate);
		}
		while (date.before(end)) {
			if (Helper.duringClinic(date.getTime())) {
				expectedHeartbeats++;
			}
			date.add(Calendar.MINUTE, cronjobIntervallInMinutes);
		}
	}

	public void calcPercentage() {
		// calc uptime percentage
		if (expectedHeartbeats > 0) {
			double uptime = (double) heartBeatsDuringClinic
					/ (double) expectedHeartbeats * 100;
			BigDecimal bd = new BigDecimal(uptime);
			bd = bd.setScale(2, RoundingMode.HALF_UP);
			percentage = bd.doubleValue();
			if (percentage > 100) {
				percentage = 100;
			}
		}
	}

	public String print() {
		if (expectedHeartbeats >= 0) {
			return "" + percentage + " %" + " (" + heartBeatsDuringClinic + "/"
					+ expectedHeartbeats + "*100)";
		} else {
			return "Not a number" + " (" + heartBeatsDuringClinic + "/"
					+ expectedHeartbeats + "*100)";
		}
	}
}
