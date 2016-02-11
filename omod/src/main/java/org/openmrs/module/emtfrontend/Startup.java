package org.openmrs.module.emtfrontend;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Startup {
	public Date date = null;
	public boolean dirty = false;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");

	public Startup(String timestamp, boolean dirty) throws ParseException {
		date = sdf.parse(timestamp);
		this.dirty = dirty;
	}

}
