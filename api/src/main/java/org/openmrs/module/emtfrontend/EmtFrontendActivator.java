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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ModuleActivator;
import org.openmrs.util.OpenmrsConstants;

/**
 * This class contains the logic that is run every time this module is either
 * started or shutdown
 */
public class EmtFrontendActivator implements ModuleActivator {

	private Log log = LogFactory.getLog(this.getClass());

	public void contextRefreshed() {
		// TODO check if os is ubuntu, may be use
		// OpenmrsConstants.UNIX_BASED_OPERATING_SYSTEM or
		// OpenmrsConstants.WINDOWS_BASED_OPERATING_SYSTEM, etc
		log.info("DHIS Connector Module refreshed");
		if(!(OpenmrsConstants.UNIX_BASED_OPERATING_SYSTEM && System.getProperty("os.name").toLowerCase().equals("linux"))) {
			try {
				log.error("EMT FrontEnd Module Requires linux/ubuntu to start");
				throw new Exception("Module Requires linux/ubuntu to start");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void started() {
		log.info("EMR Monitoring Tool Frontend module is started");
	}

	public void stopped() {
		log.info("EMR Monitoring Tool Frontend module is stopped");
	}

	public void willRefreshContext() {
	}

	public void willStart() {
	}

	public void willStop() {
	}
}
