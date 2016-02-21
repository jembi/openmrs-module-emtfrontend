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

public class Startup {
	public Date date = null;
	public boolean dirty = false;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");

	public Startup(String timestamp, boolean dirty) throws ParseException {
		date = sdf.parse(timestamp);
		this.dirty = dirty;
	}

}
