package org.openmrs.module.emtfrontend;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class Heartbeat implements Heartbeatable {
	private Date date = null;
	public int numberProcessors = -1;
	public double loadAverage1Min = -1;
	public double loadAverage5Min = -1;
	public double loadAverage15Min = -1;
	public int totalMemory = -1;
	public int freeMemory = -1;
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
