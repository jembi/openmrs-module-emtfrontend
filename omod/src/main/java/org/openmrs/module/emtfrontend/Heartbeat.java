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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class Heartbeat implements Heartbeatable {
	private Date date = null;
	public int numberProcessors = 0;
	public double loadAverage1Min = 0;
	public double loadAverage5Min = 0;
	public double loadAverage15Min = 0;
	public int totalMemory = 0;
	public int freeMemory = 0;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");

	public Heartbeat(String timestamp, StringTokenizer st)
			throws ParseException {
		date = sdf.parse(timestamp);
		try {
			if (st.hasMoreTokens())
				loadAverage1Min = Double.parseDouble(st.nextToken());
			if (st.hasMoreTokens())
				loadAverage5Min = Double.parseDouble(st.nextToken());
			if (st.hasMoreTokens())
				loadAverage15Min = Double.parseDouble(st.nextToken());
			if (st.hasMoreTokens())
				numberProcessors = Integer.parseInt(st.nextToken());
			if (st.hasMoreTokens())
				totalMemory = Integer.parseInt(st.nextToken());
			if (st.hasMoreTokens())
				freeMemory = Integer.parseInt(st.nextToken());
		} catch (NumberFormatException p) {
			p.printStackTrace();
		}
	}

	public Date date() {
		return this.date;
	}
}
