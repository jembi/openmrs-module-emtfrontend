package org.openmrs.module.emtfrontend;

import java.text.ParseException;
import java.util.Date;

public class Startup {
	public Date date = null;
	public boolean dirty = false;

	public Startup(String timestamp, boolean dirty) throws ParseException {
		date = Emt.sdf.parse(timestamp);
		this.dirty = dirty;
	}

}
