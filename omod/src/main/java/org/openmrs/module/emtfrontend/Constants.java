package org.openmrs.module.emtfrontend;

import java.text.SimpleDateFormat;

public class Constants {

	// would be nice to get it from the maven build
	public static final String EMT_VERSION = "0.4-SNAPSHOT";

	// for log file and properties
	public static String RUNTIME_DIR = "/home/hc-admin";
//	public static String RUNTIME_DIR = "/Users/xian/projects/pih-rwanda/test";
	
	public static String INSTALL_DIR = "/home/hc-admin/EmrMonitoringTool";
	
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
	public static SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");
	public static SimpleDateFormat shortDf = new SimpleDateFormat("yyyyMMdd");

	public static int heartbeatCronjobIntervallInMinutes = 15;
	public static int firstHeartbeatCronjobStartsAtMinute = 1;
	public static int openmrsHeartbeatCronjobIntervallInMinutes = 15;
	public static int firstOpenmrsHeartbeatCronjobStartsAtMinute = 2;
}
