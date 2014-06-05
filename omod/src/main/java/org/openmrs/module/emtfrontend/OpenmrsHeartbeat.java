package org.openmrs.module.emtfrontend;

import java.text.ParseException;
import java.util.Date;
import java.util.StringTokenizer;

public class OpenmrsHeartbeat implements Heartbeatable {
	public static int RESPONDING = 0;
	public static int RESPONDING_AFTER_ONE_MINUTE = 1;
	public static int NOT_RESPONDING = 2;
	
	private Date date = null;
	public int totalEncounters = -1;
	public int totalObs = -1;
	public int totalUsers = -1;
	public int responding = -1;
	
	public OpenmrsHeartbeat(String timestamp, StringTokenizer st)
			throws ParseException {
		date = Constants.sdf.parse(timestamp);
		if (st.hasMoreTokens()) {
			String s = st.nextToken();
			if ("responding".equals(s)) {
				responding = RESPONDING;
			} else if ("responding after 1 minute".equals(s)) {
				responding = RESPONDING_AFTER_ONE_MINUTE;
			} else if ("not responding".equals(s)) {
				responding = NOT_RESPONDING;
			} else {
				System.out.println("Unknown value for field OpenMRS responding: " + s);
			}
		}
		if (st.hasMoreTokens()) {
			String s = st.nextToken();
			if (s != null && !"".equals(s))
				totalEncounters = Integer.parseInt(s);
		}
		if (st.hasMoreTokens()) {
			String s = st.nextToken();
			if (s != null && !"".equals(s))
				totalObs = Integer.parseInt(s);
		}
		if (st.hasMoreTokens()) {
			String s = st.nextToken();
			if (s != null && !"".equals(s))
				totalUsers = Integer.parseInt(s);
		}
	}

	public Date date() {
		return this.date;
	}
}
