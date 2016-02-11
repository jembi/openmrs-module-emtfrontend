/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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
