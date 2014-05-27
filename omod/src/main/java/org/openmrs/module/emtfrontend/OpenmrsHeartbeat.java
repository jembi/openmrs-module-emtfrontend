package org.openmrs.module.emtfrontend;

import java.text.ParseException;
import java.util.Date;
import java.util.StringTokenizer;

public class OpenmrsHeartbeat {
	public Date date = null;
	public int totalEncounters = -1;
	public int totalObs = -1;
	public int totalUsers = -1;
	
	public OpenmrsHeartbeat(String timestamp, StringTokenizer st)
			throws ParseException {
		date = Emt.sdf.parse(timestamp);
		// responding
		if (st.hasMoreTokens())
			st.nextToken();
		if (st.hasMoreTokens())
			totalEncounters = Integer.parseInt(st.nextToken());
		if (st.hasMoreTokens())
			totalObs = Integer.parseInt(st.nextToken());
		if (st.hasMoreTokens())
			totalUsers = Integer.parseInt(st.nextToken());
	}
}
