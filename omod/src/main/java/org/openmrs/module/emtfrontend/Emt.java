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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.xml.transform.TransformerException;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * TODO support /usr/local/etc/EmrMonitoringTool/emt-to-dhis-mapping.txt configurations using emt frontend plus flosid or server/site id; but these links must only be available for admin users alone
 */
public class Emt {
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
	private static SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");
	private static SimpleDateFormat shortDf = new SimpleDateFormat("yyyyMMdd");
	private static int heartbeatCronjobIntervallInMinutes = 15;
	private static int firstHeartbeatCronjobStartsAtMinute = 1;
	private static int openmrsHeartbeatCronjobIntervallInMinutes = 15;
	private static int firstOpenmrsHeartbeatCronjobStartsAtMinute = 2;

/**
 * Avoid using extra java libraries here since emtfrontend library is embedded in emt backend command line tool
 */
	public static void main(String[] args) {
		try {
			Date startDate = shortDf.parse(args[0]);
			Date endDate = shortDf.parse(args[1]);
			String dhisDataValuesFilePath = args[4];
			String installDirectory = dhisDataValuesFilePath.replace("dhis-emt-datasetValueSets.json", "");
			String openmrsAPPName = args[5];
			String dhisOrganizationUnitUid = args[6];
			SystemInfo systemInfo = new SystemInfo(args[7]);
			
			if (startDate.after(endDate)) {
				// swap start and end date if start date after end date
				Date tmp = startDate;
				startDate = endDate;
				endDate = tmp;
			}

			loadConfig(installDirectory);
			// add one day minus 1 second to end date to easily include end date
			// in
			// calculations
			Calendar c = Calendar.getInstance();
			c.setTime(endDate);
			c.add(Calendar.HOUR, 24);
			c.add(Calendar.SECOND, -1);
			endDate = c.getTime();

			String emtLog = args[2];
			String emtPatientLog = emtLog.replace("emt.log", "emt-patient.log");
			Integer[] viralLoadTestResults = extractViralLoadTestResultsCountFromLog(emtPatientLog);
			
			Emt emt = new Emt();
			emt.parseLog(startDate, endDate, emtLog);

			// todo set hours, minutes, seconds of start and end dates to outer
			// ranges of period
			Emt emtThisWeek = new Emt();
			c = Calendar.getInstance();
			c.setTime(new Date());
			int i = c.get(Calendar.DAY_OF_WEEK) - c.getFirstDayOfWeek();
			c.add(Calendar.DATE, -i + 1);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			Date start = c.getTime();
			c.add(Calendar.DATE, 6);
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 59);
			Date end = c.getTime();
			emtThisWeek.parseLog(start, end, emtLog);
			String thisWeekUptime = emtThisWeek.systemUptime(start, end).print() + " ("
					+ df.format(start) + " - " + df.format(end) + ")";
			Emt emtPreviousWeek = new Emt();
			
			c = Calendar.getInstance();
			c.setTime(new Date());
			i = c.get(Calendar.DAY_OF_WEEK) - c.getFirstDayOfWeek();
			c.add(Calendar.DATE, -i - 7 + 1);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			start = c.getTime();
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 59);
			c.add(Calendar.DATE, 6);
			end = c.getTime();
			emtPreviousWeek.parseLog(start, end, emtLog);
			String previousWeekUptime = emtPreviousWeek
					.systemUptime(start, end).print()
					+ " ("
					+ df.format(start)
					+ " - "
					+ df.format(end) + ")";

			Emt emtPreviousMonth = new Emt();
			c = Calendar.getInstance();
			c.setTime(new Date());
			c.set(Calendar.DAY_OF_MONTH, 1);
			c.add(Calendar.MONTH, -1);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			start = c.getTime();
			c.add(Calendar.MONTH, 1);
			c.add(Calendar.DAY_OF_YEAR, -1);
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 59);
			end = c.getTime();
			emtPreviousMonth.parseLog(start, end, emtLog);
			String previousMonthUptime = emtPreviousMonth.systemUptime(start,
					end).print()
					+ " ("
					+ df.format(start)
					+ " - "
					+ df.format(end)
					+ ")";
			List<String> s = emt.report(startDate, endDate, thisWeekUptime,
					previousWeekUptime, previousMonthUptime, dhisDataValuesFilePath, installDirectory, openmrsAPPName, dhisOrganizationUnitUid, viralLoadTestResults, systemInfo);
			System.out.println(s);
			String emtPdfOutput = args[3];
			emt.generatePdfReport(s, startDate, endDate, emtPdfOutput);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Just a hacky way to meet Wayne's expectations by 12/Feb/2016
	 * 
	 * @param dhisDataValuesFilePath
	 * @param startDate
	 * @param endDate
	 * @param previousMonthUptime 
	 * @param previousWeekUptime 
	 * @param thisWeekUptime 
	 * @param startupCount 
	 * @param openmrsUptime 
	 * @param heartbeats 
	 */
	private void generateDHISDataValueSets(String dhisDataValuesFilePath, Date startDate, Date endDate, int obsTotal, int encounterTotal, int totalUsers, int totalPatientActive, int totalPatientNew, int totalVisits, int startupCount, int thisWeekUptime, int previousWeekUptime, int previousMonthUptime, int openmrsUptimePercentage, String openmrsAPPName, String dhisOrganizationUnitUid, Integer[] viralLoadTestResults, SystemInfo systemInfo) {
		/*	DATA ELEMENTS:
		 	name ___ uid
			Encounters ___ RYe2tuO9njZ
			Observations ___ NorJph8rRjt
			Users ___ GKi8zBGuC3p
			Patients-Active ___ hk0HYxaBPtz
			Patients-New ___ aGdN2xl9nUj
			Visits ___ nqGCy0uyzm8
		 */
		/*	ORG UNITS/ TODO OpenMRS Locations match
		 	" + dhisOrganizationUnitUid +  ": configured at dhis side {facility Gashora CS which is Rwanda-East-Bugesera District-Nyamata Sub District-Gashora and has a uid of " + dhisOrganizationUnitUid +  "}
		 */
		//20160101 00:00:000 to 20160102 00:00:000
		Calendar cal = Calendar.getInstance();
		Date today = new Date();
		SimpleDateFormat dFormat = new SimpleDateFormat("yyyyMMdd");
		Date oneDayAgoDate = null;
		String start01 = "", start02 = "";
		File mappingsFile = new File("/usr/local/etc/EmrMonitoringTool/emt-to-dhis-mapping.txt");
		
		//TODO restructure or refactor this file as; http://dhis2.github.io/dhis2-docs/master/en/developer/html/dhis2_developer_manual_full.html#d6543e3472
		if (mappingsFile.exists()) {
			if (openmrsAPPName.equals("") || openmrsAPPName == null) {
				openmrsAPPName = "openmrs";
			}
			cal.setTime(today);//TODO must it be hard coded to one day range alone!!! or we use start and end dates
			cal.add(Calendar.DAY_OF_YEAR, -1);
			oneDayAgoDate = cal.getTime();
			if (Integer.toString(clinicStart).length() == 3) {
				start01 = "0";
			}
			if (Integer.toString(clinicStop).length() == 3) {
				start02 = "0";
			}
			String clinicHours = start01 + clinicStart + " - " + start02 + clinicStop;
			String period = dFormat.format(oneDayAgoDate) + " to " + dFormat.format(today);
			String systemIdDataElement = "{ \"dataElement\": \"" + getDataElementUidFor("DATA-ELEMENT_systemId")
					+ "\", \"period\": \"" + dFormat.format(today) + "\", \"orgUnit\": \"" + dhisOrganizationUnitUid
					+ "\", \"value\": \"" + systemId + "\"}";
			String primaryClinicDaysDataElement = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_primaryCareDays") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": \"" + clinicDays + "\"}";
			String primaryClinicHoursDataElement = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_primaryCareHours") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": \"" + clinicHours + "\"}";
			String openMRSAppNameDataElement = "{ \"dataElement\": \"" + getDataElementUidFor("DATA-ELEMENT_openmrsAppName")
					+ "\", \"period\": \"" + dFormat.format(today) + "\", \"orgUnit\": \"" + dhisOrganizationUnitUid
					+ "\", \"value\": \"" + openmrsAPPName + "\"}";
			String encounterDataElement = "{ \"dataElement\": \"" + getDataElementUidFor("DATA-ELEMENT_totalEncounters")
					+ "\", \"period\": \"" + dFormat.format(today) + "\", \"orgUnit\": \"" + dhisOrganizationUnitUid
					+ "\", \"value\": " + encounterTotal + "}";
			String obsDataElement = "{ \"dataElement\": \"" + getDataElementUidFor("DATA-ELEMENT_totalObservations")
					+ "\", \"period\": \"" + dFormat.format(today) + "\", \"orgUnit\": \"" + dhisOrganizationUnitUid
					+ "\", \"value\": " + obsTotal + "}";
			String userDataElement = "{ \"dataElement\": \"" + getDataElementUidFor("DATA-ELEMENT_totalUsers")
					+ "\", \"period\": \"" + dFormat.format(today) + "\", \"orgUnit\": \"" + dhisOrganizationUnitUid
					+ "\", \"value\": " + totalUsers + "}";
			String patientActiveDataElement = "{ \"dataElement\": \"" + getDataElementUidFor("DATA-ELEMENT_totalPatientsActive")
					+ "\", \"period\": \"" + dFormat.format(today) + "\", \"orgUnit\": \"" + dhisOrganizationUnitUid
					+ "\", \"value\": " + totalPatientActive + "}";
			String patientNewDataElement = "{ \"dataElement\": \"" + getDataElementUidFor("DATA-ELEMENT_totalPatientsNew")
					+ "\", \"period\": \"" + dFormat.format(today) + "\", \"orgUnit\": \"" + dhisOrganizationUnitUid
					+ "\", \"value\": " + totalPatientNew + "}";
			String visitsDataElement = "{ \"dataElement\": \"" + getDataElementUidFor("DATA-ELEMENT_totalVisits")
					+ "\", \"period\": \"" + dFormat.format(today) + "\", \"orgUnit\": \"" + dhisOrganizationUnitUid
					+ "\", \"value\": " + totalVisits + "}";
			String systemStartupsDataElement = "{ \"dataElement\": \"" + getDataElementUidFor("DATA-ELEMENT_systemStartupCounts")
					+ "\", \"period\": \"" + dFormat.format(today) + "\", \"orgUnit\": \"" + dhisOrganizationUnitUid
					+ "\", \"value\": " + startupCount + "}";
			String upTimeThisWeekDataElement = "{ \"dataElement\": \"" + getDataElementUidFor("DATA-ELEMENT_systemUptime-thisWeek")
					+ "\", \"period\": \"" + dFormat.format(today) + "\", \"orgUnit\": \"" + dhisOrganizationUnitUid
					+ "\", \"value\": " + thisWeekUptime + "}";
			String upTimeLastWeekDataElement = "{ \"dataElement\": \"" + getDataElementUidFor("DATA-ELEMENT_systemUptime-lastWeek")
					+ "\", \"period\": \"" + dFormat.format(today) + "\", \"orgUnit\": \"" + dhisOrganizationUnitUid
					+ "\", \"value\": " + previousWeekUptime + "}";
			String upTimeLastMonthDataElement = "{ \"dataElement\": \"" + getDataElementUidFor("DATA-ELEMENT_systemUptime-lastMonth")
					+ "\", \"period\": \"" + dFormat.format(today) + "\", \"orgUnit\": \"" + dhisOrganizationUnitUid
					+ "\", \"value\": " + previousMonthUptime + "}";
			String freeMemoryDataElement = "{ \"dataElement\": \"" + getDataElementUidFor("DATA-ELEMENT_freeMemory")
					+ "\", \"period\": \"" + dFormat.format(today) + "\", \"orgUnit\": \"" + dhisOrganizationUnitUid
					+ "\", \"value\": " + lastMemoryCapture()[0] + "}";
			String totalMemoryDataElement = "{ \"dataElement\": \"" + getDataElementUidFor("DATA-ELEMENT_totalMemory")
					+ "\", \"period\": \"" + dFormat.format(today) + "\", \"orgUnit\": \"" + dhisOrganizationUnitUid
					+ "\", \"value\": " + lastMemoryCapture()[1] + "}";
			int usedMemo = lastMemoryCapture()[1] - lastMemoryCapture()[0];
			String usedMemoryDataElement = "{ \"dataElement\": \"" + getDataElementUidFor("DATA-ELEMENT_usedMemory")
					+ "\", \"period\": \"" + dFormat.format(today) + "\", \"orgUnit\": \"" + dhisOrganizationUnitUid
					+ "\", \"value\": " + usedMemo + "}";
			String totalOpenMRSUptimeDataElement = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_openmrsUptime") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": " + openmrsUptimePercentage
					+ "}";
			String viralLoadTestResults_everDataElement = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_patientsViralLoadTestResults_ever") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": " + viralLoadTestResults[0]
					+ "}";
			String viralLoadTestResults_last6MonthsDataElement = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_patientsViralLoadTestResults_last6Months") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": " + viralLoadTestResults[1]
					+ "}";
			String viralLoadTestResults_lastYearDataElement = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_patientsViralLoadTestResults_LastYear") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": " + viralLoadTestResults[2]
					+ "}";
			String systemInfo_operatingSystem = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_systemInfo_operatingSystemName") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": \"" + systemInfo.operatingSystem
					+ "\"}";
			String systemInfo_operatingSystemArch = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_systemInfo_operatingSystemArch") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": \"" + systemInfo.operatingSystemArch
					+ "\"}";
			String systemInfo_operatingSystemVersion = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_systemInfo_operatingSystemVersion") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": \"" + systemInfo.operatingSystemVersion
					+ "\"}";
			String systemInfo_javaVersion = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_systemInfo_javaVersion") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": \"" + systemInfo.javaVersion
					+ "\"}";
			String systemInfo_javaVendor = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_systemInfo_javaVendor") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": \"" + systemInfo.javaVendor
					+ "\"}";
			String systemInfo_jvmVersion = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_systemInfo_jvmVersion") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": \"" + systemInfo.jvmVersion
					+ "\"}";
			String systemInfo_jvmVendor = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_systemInfo_jvmVendor") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": \"" + systemInfo.jvmVendor
					+ "\"}";
			String systemInfo_javaRuntimeName = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_systemInfo_javaRuntimeName") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": \"" + systemInfo.javaRuntimeName
					+ "\"}";
			String systemInfo_javaRuntimeVersion = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_systemInfo_javaRuntimeVersion") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": \"" + systemInfo.javaRuntimeVersion
					+ "\"}";
			String systemInfo_userName = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_systemInfo_userName") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": \"" + systemInfo.userName
					+ "\"}";
			String systemInfo_systemLanguage = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_systemInfo_systemLanguage") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": \"" + systemInfo.systemLanguage
					+ "\"}";
			String systemInfo_systemTimezone = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_systemInfo_systemTimezone") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": \"" + systemInfo.systemTimezone
					+ "\"}";
			String systemInfo_systemDateTime = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_systemInfo_systemDateTime") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": \"" + systemInfo.getSystemDateTime()
					+ "\"}";
			String systemInfo_fileSystemEncoding = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_systemInfo_fileSystemEncoding") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": \"" + systemInfo.fileSystemEncoding
					+ "\"}";
			String systemInfo_userDirectory = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_systemInfo_userDirectory") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": \"" + systemInfo.userDirectory
					+ "\"}";
			String systemInfo_tempDirectory = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_systemInfo_tempDirectory") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": \"" + systemInfo.tempDirectory
					+ "\"}";
			String systemInfo_openMRSVersion = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_systemInfo_openMRSVersion") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": \"" + systemInfo.getOpenMRSVersion()
					+ "\"}";
			String systemInfo_installedModules = "{ \"dataElement\": \""
					+ getDataElementUidFor("DATA-ELEMENT_systemInfo_installedModules") + "\", \"period\": \"" + dFormat.format(today)
					+ "\", \"orgUnit\": \"" + dhisOrganizationUnitUid + "\", \"value\": \"" + systemInfo.getinstalledModulesString()
					+ "\"}";
			
			String json = "{\"dataValues\": [\n  " + systemIdDataElement + ",\n  " + openMRSAppNameDataElement + ",\n  "
					+ primaryClinicDaysDataElement + ",\n  " + primaryClinicHoursDataElement + ",\n  "
					+ encounterDataElement + ",\n  " + obsDataElement + ",\n  " + userDataElement + ",\n  "
					+ patientActiveDataElement + ",\n  " + patientNewDataElement + ",\n  " + visitsDataElement + ",\n  "
					+ viralLoadTestResults_everDataElement + ",\n  " + viralLoadTestResults_last6MonthsDataElement
					+ ",\n  " + viralLoadTestResults_lastYearDataElement + ",\n  " + systemStartupsDataElement + ",\n  "
					+ upTimeThisWeekDataElement + ",\n  " + upTimeLastWeekDataElement + ",\n  "
					+ upTimeLastMonthDataElement + ",\n  " + freeMemoryDataElement + ",\n  " + totalMemoryDataElement
					+ ",\n  " + totalOpenMRSUptimeDataElement + ",\n  " + usedMemoryDataElement + ",\n  "
					+ systemInfo_operatingSystem + ",\n  " + systemInfo_operatingSystemArch + ",\n  " + systemInfo_operatingSystemVersion + ",\n  "
					+ systemInfo_javaVersion + ",\n  " + systemInfo_javaVendor + ",\n  "
					+ systemInfo_jvmVersion + ",\n  " + systemInfo_jvmVendor + ",\n  "
					+ systemInfo_javaRuntimeName + ",\n  " + systemInfo_javaRuntimeVersion + ",\n  "
					+ systemInfo_userName + ",\n  " + systemInfo_systemLanguage + ",\n  "
					+ systemInfo_systemTimezone + ",\n  " + systemInfo_fileSystemEncoding + ",\n  "
					+ systemInfo_systemDateTime + ",\n  " + systemInfo_userDirectory + ",\n  "
					+ systemInfo_tempDirectory + ",\n  " + systemInfo_openMRSVersion + ",\n  "
					+ systemInfo_installedModules + "\n ]\n}";
			File dhisDataJson = new File(dhisDataValuesFilePath);
			try {
				FileOutputStream fop = new FileOutputStream(dhisDataJson);

				// if file doesn't exists, then create it
				if (!dhisDataJson.exists()) {
					dhisDataJson.createNewFile();
				}

				// get the content in bytes
				byte[] contentInBytes = json.getBytes();

				fop.write(contentInBytes);
				fop.flush();
				fop.close();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
	}
	
	private String getDataElementUidFor(String emtDataElementCode) {
		String dataElementUid = null;
		String emtMappingsFilePath = "/usr/local/etc/EmrMonitoringTool/emt-to-dhis-mapping.txt";
		File emtMappingsFile = new File(emtMappingsFilePath);
		FileInputStream fis;
		BufferedReader br;
		
		if (emtMappingsFile.exists() && emtDataElementCode != null && !emtDataElementCode.equals("") && emtDataElementCode.startsWith("DATA-ELEMENT_")) {
			try {
				fis = new FileInputStream(emtMappingsFile);
				br = new BufferedReader(new InputStreamReader(fis));
				String line = null;
				
				while ((line = br.readLine()) != null) {
					if(line.startsWith(emtDataElementCode)) {
						dataElementUid = line.replace(emtDataElementCode + "=", "");
					}
				}

				br.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
		return dataElementUid;
	}

	public static void hmisExport(String[] args) {
		try {
			Date date = shortDf.parse(args[0]);

			loadConfig(args[1].replace("emt.log", ""));
			// add one day minus 1 second to end date to easily include end date
			// in
			// calculations
			Calendar c = Calendar.getInstance();
			c = Calendar.getInstance();
			c.setTime(date);
			c.set(Calendar.DAY_OF_MONTH, 1);
			//c.add(Calendar.MONTH, -1);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			Date startDate = c.getTime();
			c.add(Calendar.MONTH, 1);
			c.add(Calendar.DAY_OF_YEAR, -1);
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 59);
			Date endDate = c.getTime();

			String emtLog = args[1];
			Emt emt = new Emt();
			emt.parseLog(startDate, endDate, emtLog);

			String emtCsvOutput = args[3];
			String fosaidLocation = args[2];
			emt.generateHmisCsvExport(startDate, endDate, fosaidLocation, emtCsvOutput);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void generateHmisCsvExport(Date startDate, Date endDate,
			String fosaidLocation, String emtCsvOutput) {
		try {
			OutputStream os = new FileOutputStream(emtCsvOutput);
			PrintWriter w = new PrintWriter(os);
			SimpleDateFormat output = new SimpleDateFormat("yyyyMM");
			String period = output.format(startDate);
			w.println("Server uptime percent," + period + "," + fosaidLocation + "," + systemUptime(startDate, endDate).print().split(" ")[0]);
			w.println("Server system starts," + period + "," + fosaidLocation + "," + startupCount);
			w.println("Server crashes," + period + "," + fosaidLocation + "," + startupsWithoutShutdowns);
			w.close();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void loadConfig(String installDirectory) {
		Properties prop = new Properties();
		InputStream input = null;
		
		try {
			input = new FileInputStream(installDirectory
					+ "/emt.properties");
			prop.load(input);
			clinicDays = prop.getProperty("clinicDays", "Mo,Tu,We,Th,Fr");
			clinicStart = Integer
					.parseInt(prop.getProperty("clinicStart", "8"));
			clinicStop = Integer.parseInt(prop.getProperty("clinicEnd", "17"));
		} catch (IOException ex) {
			System.out.println("Warning: " + installDirectory
					+ "/emt.properties not found. Assuming defaults");
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	Date now = new Date();
	String systemId = "";
	public static String clinicDays = "Mo,Tu,We,Th,Fr";
	public static int clinicStart = 800;
	public static int clinicStop = 1700;

	int startupCount = 0;
	int shutdownCount = 0;
	int startupsWithoutShutdowns = 0;
	List<Startup> startups = new ArrayList<Startup>();
	List<Heartbeatable> heartbeats = new ArrayList<Heartbeatable>();
	List<Heartbeatable> openmrsHeartbeats = new ArrayList<Heartbeatable>();


	@SuppressWarnings("resource")
	private void parseLog(Date startDate, Date endDate, String emtLog)
			throws FileNotFoundException {
		File emt = new File(emtLog);
		Scanner scanner = new Scanner(emt);
		
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			try {
				StringTokenizer st = new StringTokenizer(line, ";", false);
				// timestamp
				String timestamp = "";
				if (st.hasMoreTokens()) {
					timestamp = st.nextToken();
					if (Helper.inPeriod(startDate, endDate, timestamp)) {
						// system id
						if (st.hasMoreTokens()) {
							systemId = st.nextToken();
						}
						// event type
						if (st.hasMoreTokens()) {
							String type = st.nextToken().trim();
							if ("STARTUP".equals(type)) {
								startupCount++;
								if (st.hasMoreTokens()) {
									String s = st.nextToken();
									if ("DIRTY".equals(s)) {
										startupsWithoutShutdowns++;
										startups.add(new Startup(timestamp,
												true));
									} else {
										startups.add(new Startup(timestamp,
												false));
									}
								} else {
									startups.add(new Startup(timestamp, false));
								}
							} else if ("SHUTDOWN".equals(type)) {
								shutdownCount++;
							} else if ("HEARTBEAT".equals(type)) {
								Heartbeat hb = new Heartbeat(timestamp, st);
								heartbeats.add(hb);
							} else if ("OPENMRS-HEARTBEAT".equals(type)) {
								OpenmrsHeartbeat hb = new OpenmrsHeartbeat(
										timestamp, st);
								openmrsHeartbeats.add(hb);
							} else if ("EMT-INSTALL".equals(type)
									|| "EMT-CONFIGURE".equals(type)) {

							} else {
								System.out.println("Unknown type '" + type
										+ "' found, ignoring");
							}
						}
					}
				}
			} catch (Exception e) {
				System.out.println("Error (" + e.getMessage() + ", "
						+ e.getCause() + ") parsing line: " + line);
				e.printStackTrace();
			}
		}
	}

	private List<String> report(Date startDate, Date endDate,
			String thisWeekUptime, String previousWeekUptime,
			String previousMonthUptime, String dhisDataValuesFilePath, String installDirectory, String openmrsAPPName, String dhisOrganizationUnitUid, Integer[] viralLoadTestResults, SystemInfo systemInfo) {
		
		Uptime uptime = systemUptime(startDate, endDate);
		Uptime openmrsUptime = openmrsUptime(startDate, endDate);
		if (openmrsUptime.percentage > uptime.percentage) {
			openmrsUptime = uptime;
		}
		List<String> ss = new ArrayList<String>();
		int numberOfEncounters = totalEncounters(false) - totalEncounters(true);
		
		ss.add("Current date and time: " + new Date());
		ss.add("");
		ss.add("\nSystem ID: " + systemId);
		ss.add("\nLast EMT installation date: <to be implemented>");
		ss.add("\nEMT version: " + emtVersion());
		ss.add("\nPrimary Clinic Days: " + clinicDays);
		ss.add("\nPrimary Clinic Hours: " + clinicStart + " - " + clinicStop);
		ss.add("");
		ss.add("\nStart date: " + df.format(startDate));
		ss.add("\nEnd date: " + df.format(endDate) + " (including)");
		ss.add("");
		ss.add("\nPercentage of system uptime (1): "
				+ uptime.print());
		ss.add("\n  This week: " + thisWeekUptime);
		ss.add("\n  Last week: " + previousWeekUptime);
		ss.add("\n  Last month: " + previousMonthUptime);
		ss.add("");
		ss.add("\nNumber of system starts (2): " + startupCount);
		ss.add("\nTimes of last system starts (2): " + lastSystemRestarts());
		ss.add("\nNumber of system starts without preceding shutdown (aka crashes) (2): "
				+ startupsWithoutShutdowns);
		ss.add("\nTimes of last system crashes (approximation) (2): <to be implemented>");
		ss.add("");
		ss.add("\nHighest average 5 minutes system loads (number of processors) (2): "
				+ highestAverage5minLoads());
		ss.add("\nLowest amounts of free memory in MB (2): "
				+ lowestFreeMemory());
		ss.add("");
		ss.add("\nPercentage of OpenMRS uptime (1): "
				+ openmrsUptime.print());
		ss.add("\nNumber of Encounters (3) - (4): "
				+ numberOfEncounters + " - "
				+ totalEncounters(false));
		ss.add("\nNumber of Obs (3) - (4): "
				+ (totalObs(false) - totalObs(true)) + " - " + totalObs(false));
		ss.add("\nNumber of users (3) - (4): "
				+ (totalUsers(false) - totalUsers(true)) + " - "
				+ totalUsers(false));
		ss.add("\nNumber of active patients (3) - (4): "
				+ (totalActivePatients(false) - totalActivePatients(true)) + " - "
				+ totalActivePatients(false));
		ss.add("\nNumber of new patients (3) - (4): "
				+ (totalNewPatients(false) - totalNewPatients(true)) + " - "
				+ totalNewPatients(false));
		ss.add("\nNumber of visits (3) - (4): "
				+ (totalVisits(false) - totalVisits(true)) + " - "
				+ totalVisits(false));
		ss.add("\nLast local OpenMRS backup (5): " + lastOpenMRSBackup(installDirectory));
		
		ss.add("\nNumber of Patients with Viral Load Test Result (ever): " + viralLoadTestResults[0]);
		ss.add("\nNumber of Patients with Viral Load Test Result within last six months: " + viralLoadTestResults[1]);
		ss.add("\nNumber of Patients with Viral Load Test Result within last year: " + viralLoadTestResults[2]);
		
		ss.add("");
		ss.add("\n____");
		ss.add("");
		ss.add("\n(1) during clinic hours between start and end date");
		ss.add("\n(2) between start and end date (incl. outside of clinic hours)");
		ss.add("\n(3) new during period in OpenMRS database (not voided or retired)");
		ss.add("\n(4) total ever in OpenMRS database (not voided or retired)");
		ss.add("\n(5) in " + installDirectory + "backups");
		ss.add("");
		ss.add("\n");
		ss.add("Operating System: " + systemInfo.operatingSystem);
		ss.add("\nOperating System Arch: " + systemInfo.operatingSystemArch);
		ss.add("\nOperating System Version: " + systemInfo.operatingSystemVersion);
		ss.add("\nJava Version: " + systemInfo.javaVersion);
		ss.add("\nJava Vendor: " + systemInfo.javaVendor);
		ss.add("\nJvm Version: " + systemInfo.jvmVersion);
		ss.add("\nJvm Vendor: " + systemInfo.jvmVendor);
		ss.add("\nJava Runtime Name: " + systemInfo.javaRuntimeName);
		ss.add("\nJava Runtime Version: " + systemInfo.javaRuntimeVersion);
		ss.add("\nUser Name: " + systemInfo.userName);
		ss.add("\nSystem Language: " + systemInfo.systemLanguage);
		ss.add("\nSystem Timezone: " + systemInfo.systemTimezone);
		ss.add("\nSystem DateTime: " + systemInfo.getSystemDateTime());
		ss.add("\nFile System Encoding: " + systemInfo.fileSystemEncoding);
		ss.add("\nUser Directory: " + systemInfo.userDirectory);
		ss.add("\nTemp Directory: " + systemInfo.tempDirectory);
		ss.add("\nOperating System: " + systemInfo.operatingSystem);
		ss.add("\nOpenMRS Platform Version: " + systemInfo.getOpenMRSVersion());
		ss.add("\nOpenMRS Loaded Modules: " + systemInfo.getinstalledModulesString());
		
		//TODO update after hearing from @Christian
		int obsTotal = totalObs(true);
		int encounterTotal = totalEncounters(true);
		int totalUsers = totalUsers(true);
		int totalPatientActive = totalActivePatients(true);
		int totalPatientNew = totalNewPatients(true);
		int totalVisits = totalVisits(true);
		
		if(dhisDataValuesFilePath != null && !dhisDataValuesFilePath.equals("") && dhisOrganizationUnitUid != null && !dhisOrganizationUnitUid.equals("")) {
			generateDHISDataValueSets(dhisDataValuesFilePath, startDate, endDate, obsTotal, encounterTotal, totalUsers, totalPatientActive, totalPatientNew, totalVisits, startupCount, (int) Math.round(Double.parseDouble(thisWeekUptime.split(" %")[0])), (int) Math.round(Double.parseDouble(previousWeekUptime.split(" %")[0])), (int) Math.round(Double.parseDouble(previousMonthUptime.split(" %")[0])), (int) Math.round(openmrsUptime.percentage), openmrsAPPName, dhisOrganizationUnitUid, viralLoadTestResults, systemInfo);
		}
		return ss;
	}

	private String emtVersion() {
		return "1.3-SNAPSHOT";
	}

	private int totalEncounters(boolean atStart) {
		if (atStart && openmrsHeartbeats.size() > 0) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(0)).totalEncounters;
		} else if (openmrsHeartbeats.size() > 1) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(openmrsHeartbeats.size() - 1)).totalEncounters;
		}
		return -1;
	}

	private int totalObs(boolean atStart) {
		if (atStart && openmrsHeartbeats.size() > 0) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(0)).totalObs;
		} else if (openmrsHeartbeats.size() > 1) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(openmrsHeartbeats.size() - 1)).totalObs;
		}
		return -1;
	}

	private int totalUsers(boolean atStart) {
		if (atStart && openmrsHeartbeats.size() > 0) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(0)).totalUsers;
		} else if (openmrsHeartbeats.size() > 1) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(openmrsHeartbeats.size() - 1)).totalUsers;
		}
		return -1;
	}

	private int totalActivePatients(boolean atStart) {
		if (atStart && openmrsHeartbeats.size() > 0) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(0)).activePatients;
		} else if (openmrsHeartbeats.size() > 1) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(openmrsHeartbeats.size() - 1)).activePatients;
		}
		return -1;
	}

	private int totalNewPatients(boolean atStart) {
		if (atStart && openmrsHeartbeats.size() > 0) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(0)).newPatients;
		} else if (openmrsHeartbeats.size() > 1) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(openmrsHeartbeats.size() - 1)).newPatients;
		}
		return -1;
	}

	private int totalVisits(boolean atStart) {
		if (atStart && openmrsHeartbeats.size() > 0) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(0)).visits;
		} else if (openmrsHeartbeats.size() > 1) {
			return ((OpenmrsHeartbeat) openmrsHeartbeats.get(openmrsHeartbeats.size() - 1)).visits;
		}
		return -1;
	}

	private void generatePdfReport(List<String> lines, Date startDate,
			Date endDate, String outputFilename) throws IOException,
			COSVisitorException, TransformerException {
		PDDocument document = new PDDocument();

		// metadata
		PDDocumentInformation info = document.getDocumentInformation();
		info.setAuthor("OpenMRS EMT");
		info.setTitle("EMT Report");
		// info.setSubject(subject);
		info.setProducer("EMR Monitoring Tool");
		info.setCreator(System.getProperty("user.name"));
		info.setCreationDate(Calendar.getInstance());
		document.setDocumentInformation(info);

		// content
		PDPage page = new PDPage(PDPage.PAGE_SIZE_A4);
		document.addPage(page);
		PDFont font = PDType1Font.HELVETICA;
		PDPageContentStream contentStream = new PDPageContentStream(document,
				page);

		if (lines.size() == 0) {
			contentStream.endText();
			contentStream.close();
			return;
		}
		final double fontHeight = font.getFontDescriptor().getFontBoundingBox()
				.getHeight() / 1000 * 12 * 0.865;

		contentStream.beginText();
		contentStream.setFont(font, 12);
		contentStream.appendRawCommands(fontHeight + " TL\n");
		contentStream.moveTextPositionByAmount(50, 750);
		for (int i = 0; i < lines.size(); i++) {
			contentStream.drawString(lines.get(i));
			if (i < lines.size() - 1) {
				contentStream.appendRawCommands("T*\n");
			}
		}
		contentStream.endText();
		contentStream.close();

		// Save the results and ensure that the document is properly closed:
		document.save(outputFilename);
		document.close();
	}

	private Uptime systemUptime(Date startDate, Date endDate) {
		Uptime uptime = new Uptime();
		uptime.calcHeartbearts(startDate, endDate, heartbeats, heartbeatCronjobIntervallInMinutes, firstHeartbeatCronjobStartsAtMinute);
		uptime.calcPercentage();
		return uptime;
	}

	private String lowestFreeMemory() {
		Collections.sort(heartbeats, new Comparator<Heartbeatable>() {
			public int compare(Heartbeatable hb1, Heartbeatable hb2) {
				return ((Integer) ((Heartbeat) hb1).freeMemory)
						.compareTo((Integer) ((Heartbeat) hb2).freeMemory);
			}
		});
		String s = "";
		s += heartbeats.size() > 0 ? ((Heartbeat) heartbeats.get(0)).freeMemory : "";
		s += " " + (heartbeats.size() > 1 ? ((Heartbeat) heartbeats.get(1)).freeMemory : "");
		s += " " + (heartbeats.size() > 2 ? ((Heartbeat) heartbeats.get(2)).freeMemory : "");
		return s;
	}
	
	private int[] lastMemoryCapture() {
		int[] memo = new int[2];
		
		Collections.sort(heartbeats, new Comparator<Heartbeatable>() {
			public int compare(Heartbeatable hb1, Heartbeatable hb2) {
				return ((Integer) ((Heartbeat) hb1).freeMemory)
						.compareTo((Integer) ((Heartbeat) hb2).freeMemory);
			}
		});
		int freeMemo = heartbeats.size() > 0 ? ((Heartbeat) heartbeats.get(heartbeats.size() - 1)).freeMemory : -1;
		int totalMemo = heartbeats.size() > 0 ? ((Heartbeat) heartbeats.get(heartbeats.size() - 1)).totalMemory : -1;
		
		memo[0] = freeMemo;
		memo[1] = totalMemo;
		return memo;
	}

	private String lastSystemRestarts() {
		Collections.sort(startups, new Comparator<Startup>() {
			public int compare(Startup s1, Startup s2) {
				return ((Date) s1.date).compareTo((Date) s2.date);
			}
		});
		String s = "";
		s += startups.size() > 0 ? sdf.format(startups.get(0).date) : "";
		s += " "
				+ (startups.size() > 1 ? sdf.format(startups.get(1).date) : "");
		s += " "
				+ (startups.size() > 2 ? sdf.format(startups.get(2).date) : "");
		return s;
	}

	private String highestAverage5minLoads() {
		Collections.sort(heartbeats, new Comparator<Heartbeatable>() {
			public int compare(Heartbeatable hb1, Heartbeatable hb2) {
				return ((Double) ((Heartbeat) hb1).loadAverage5Min)
						.compareTo((Double) ((Heartbeat) hb2).loadAverage5Min);
			}
		});
		String s = "";
		s += heartbeats.size() > 0 ? ((Heartbeat) heartbeats.get(heartbeats.size() - 1)).loadAverage5Min
				: "";
		s += " "
				+ (heartbeats.size() > 1 ? ((Heartbeat) heartbeats
						.get(heartbeats.size() - 2)).loadAverage5Min : "");
		s += " "
				+ (heartbeats.size() > 2 ? ((Heartbeat) heartbeats
						.get(heartbeats.size() - 3)).loadAverage5Min : "");
		s += " ("
				+ (heartbeats.size() > 0 ? ((Heartbeat) heartbeats.get(0)).numberProcessors
						: "") + ")";
		return s;
	}

	private String lastOpenMRSBackup(String installDirectory) {
		File fs = new File(installDirectory + "backups");
		// File fs = new File("/tmp");
		File[] allFiles = fs.listFiles();
		if (allFiles != null && allFiles.length > 0) {
			Arrays.sort(allFiles, new Comparator<File>() {
				public int compare(File f1, File f2) {
					return ((Long) f1.lastModified()).compareTo((Long) f2
							.lastModified());
				}
			});
			String s = allFiles[allFiles.length - 1].getName()
					+ " ("
					+ sdf.format(new Date(allFiles[allFiles.length - 1]
							.lastModified())) + ")";
			return s;
		} else {
			return "<not found>";
		}
	}

	private Uptime openmrsUptime(Date startDate, Date endDate) {
		Uptime uptime = new Uptime();
		uptime.calcHeartbearts(startDate, endDate, openmrsHeartbeats, openmrsHeartbeatCronjobIntervallInMinutes, firstOpenmrsHeartbeatCronjobStartsAtMinute);
		uptime.calcPercentage();
		return uptime;
	}
	
	private static Integer[] extractViralLoadTestResultsCountFromLog(String logPath) throws IOException {
		Integer[] ever6monthsOneYearCounts = new Integer[3];
		File logFile = new File(logPath);
		
		if (logFile.exists()) {
			FileInputStream fis = new FileInputStream(logFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String noneReverdLine = null;
			ArrayList<String> list = new ArrayList<String>();
			while ((noneReverdLine = br.readLine()) != null) {
				list.add(noneReverdLine);
			}
			br.close();
			Collections.reverse(list);//read file from tail to get last entry
			for (String line : list) {
				if (line.startsWith("PATIENTS_WITH_VIRAL_LOAD_TEST_RESULTS(EVER,LAST6MONTHS,LASTYEAR);")) {
					String viralLoadTestResults = line.split(":::")[1];
					
					if (viralLoadTestResults != null && !viralLoadTestResults.equals("")) {
						Integer viralLoads_ever = viralLoadTestResults.split(";")[0] != null
								? Integer.parseInt(viralLoadTestResults.split(";")[0]) : 0;
						Integer viralLoads_6Months = viralLoadTestResults.split(";")[1] != null
								? Integer.parseInt(viralLoadTestResults.split(";")[1]) : 0;
						Integer viralLoads_oneYear = viralLoadTestResults.split(";")[2] != null
								? Integer.parseInt(viralLoadTestResults.split(";")[2]) : 0;
						ever6monthsOneYearCounts[0] = viralLoads_ever;
						ever6monthsOneYearCounts[1] = viralLoads_6Months;
						ever6monthsOneYearCounts[2] = viralLoads_oneYear;
					}
					break;
				}
			} 
		}
		return ever6monthsOneYearCounts;
	}
}
