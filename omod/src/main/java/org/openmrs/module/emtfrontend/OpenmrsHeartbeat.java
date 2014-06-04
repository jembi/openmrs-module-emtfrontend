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
		if (st.hasMoreTokens())
			// todo, deal with responding et al
			st.nextToken();
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
}
