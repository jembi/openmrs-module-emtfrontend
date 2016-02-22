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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.module.ModuleFactory;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;

public class Constants {

	// would be nice to get it from the maven build
	public static final String EMT_VERSION = getEMTVersion();

	// for log file and properties
	public static String OPENMRS_DATA_DIRECTORY = getOpenMRSDataDirectory();
	public static String RUNTIME_DIR = getEMTInstallationDirectory();
	public static String INSTALL_DIR = OPENMRS_DATA_DIRECTORY + "EmrMonitoringTool" + File.separator;
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
	public static SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");
	public static SimpleDateFormat shortDf = new SimpleDateFormat("yyyyMMdd");

	public static int heartbeatCronjobIntervallInMinutes = 15;
	public static int firstHeartbeatCronjobStartsAtMinute = 1;
	public static int openmrsHeartbeatCronjobIntervallInMinutes = 15;
	public static int firstOpenmrsHeartbeatCronjobStartsAtMinute = 2;

	/*
	 * May have used OpenmrsUtil.getApplicationDataDirectory() but needed to
	 * include to extra libraries
	 */
	private static String getOpenMRSDataDirectory() {
		String dir = "";
		File installConfig;
		try {
			installConfig = new File(OpenmrsUtil.getDirectoryInApplicationDataDirectory("EmrMonitoringTool").getCanonicalPath() + File.separator  + "." + WebConstants.WEBAPP_NAME + "-emt-config.properties");
			if (!installConfig.exists()) {
				if((new File("/var/lib/OpenMRS").exists())) {
					dir = "/var/lib/OpenMRS" + File.separator;//default
				} else if((new File(System.getProperty("user.home") + File.separator + ".OpenMRS").exists())) {
					dir = System.getProperty("user.home") + File.separator + ".OpenMRS" + File.separator;
				}
			} else {
				BufferedReader br;
				try {
					br = new BufferedReader(new FileReader(installConfig));
	
					String line = null;
					while ((line = br.readLine()) != null) {
						if(line.startsWith("openmrs_data_directory=")) {
							dir = line.split("=")[1];
							break;
						}
					}
	
					br.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
	
			}
		} catch (APIException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return dir;
	}
	
	private static String getEMTInstallationDirectory() {
		return "/usr/local/etc/EmrMonitoringTool";
	}

	private static String getEMTVersion() {
		String version = "1.0";
		
		if(ModuleFactory.getModuleById("emtfrontend") != null && StringUtils.isNotBlank(ModuleFactory.getModuleById("emtfrontend").getVersion())) {
			version = ModuleFactory.getModuleById("emtfrontend").getVersion();
		}
		
		return version;
	}
}
