package org.openmrs.module.emtfrontend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class Constants {

	// would be nice to get it from the maven build
	public static final String EMT_VERSION = "0.5-SNAPSHOT";

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
		File installDir = new File(getEMTInstallationDirectory() + File.separator + ".emt-config.properties");

		if (!installDir.exists()) {
			if((new File("/var/lib/OpenMRS").exists())) {
				dir = "/var/lib/OpenMRS" + File.separator;//default
			} else if((new File(System.getProperty("user.home") + File.separator + ".OpenMRS").exists())) {
				dir = System.getProperty("user.home") + File.separator + ".OpenMRS" + File.separator;
			}
		} else {
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(installDir));

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
		return dir;
	}
	
	private static String getEMTInstallationDirectory() {
		return System.getProperty("user.home") + File.separator + "EmrMonitoringTool";
	}

}
