/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emtfrontend.web.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleUtil;
import org.openmrs.module.emtfrontend.Constants;
import org.openmrs.module.emtfrontend.Emt;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

//TODO Could actually expose "$OPENMRS_APP_NAME"-emt-config.properties configurations to be editable at user interface level
@Controller
public class EmtFrontendFormController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	private static String INSTALL_DIR = OpenmrsUtil.getApplicationDataDirectory().endsWith(File.separator) ? OpenmrsUtil.getApplicationDataDirectory() :  OpenmrsUtil.getApplicationDataDirectory() + File.separator;

	@RequestMapping(value = "/module/emtfrontend/emtfrontendConfig.form", method = RequestMethod.GET)
	private String showConfig(ModelMap model) {
		model.addAttribute("emrConfig", INSTALL_DIR + "EmrMonitoringTool/emt.properties");
		return "/module/emtfrontend/emtfrontendConfig";
	}

	@RequestMapping(value = "/module/emtfrontend/configure.form", method = RequestMethod.POST)
	public String onSubmit(@RequestParam("clinicDays") String days, @RequestParam("clinicStart") String start, @RequestParam("clinicEnd") String end) {
		Properties prop = new Properties();
		OutputStream output = null;
		try {
			output = new FileOutputStream(INSTALL_DIR + "EmrMonitoringTool/emt.properties");
			// set the properties value
			prop.setProperty("clinicDays", days);
			prop.setProperty("clinicStart", start);
			prop.setProperty("clinicEnd", end);
			prop.store(output, null);
			return "redirect:/admin/index.htm";
		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return "redirect:/admin/index.htm";
	}

	@RequestMapping(value = "/module/emtfrontend/emtfrontendLink.form", method = RequestMethod.GET)
	private String showForm() {
		return "/module/emtfrontend/emtfrontendForm";
	}
	
	@RequestMapping(value = "/module/emtfrontend/emtfrontendDHIS.form", method = RequestMethod.GET)
	public void renderEmtfrontendDHIS(ModelMap model) {
		model.addAttribute("message", "");
	}
	
	@RequestMapping(value = "/module/emtfrontend/emtfrontendDHIS.form", method = RequestMethod.POST)
	public void exportEmtfrontendDHIS(ModelMap model) {
		String cmd = Constants.RUNTIME_DIR + "/shell-backend/push-data-to-dhis.sh";
		
		try {
			Runtime.getRuntime().exec(cmd);
			model.addAttribute("message", "Successfully Updated DHIS Remote Server");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/module/emtfrontend/emtfrontendLink.form", method = RequestMethod.POST)
	public void generatePDF(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			SimpleDateFormat input = new SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat output = new SimpleDateFormat("yyyyMMdd");

			String start = request.getParameter("startDate");
			String end = request.getParameter("endDate");
			log.info(start);
			log.info(end);

			// get temp file just to get the unique name
			File f = File.createTempFile("temp", null);
			String tempFilename = f.getAbsolutePath();
			f.delete();

			// invokeExternalProcess();
			// invokeJarFromCustomCloassloader();
			invokeNormalEmt(output.format(input.parse(start)),
					output.format(input.parse(end)), INSTALL_DIR + "EmrMonitoringTool/emt.log",
					tempFilename, INSTALL_DIR + "dhis-emt-datasetValueSets.json");

			File pdfFile = new File(tempFilename);
			// send back as PDF via HTTP
			returnPdf(
					response,
					pdfFile,
					"emt-" + output.format(input.parse(start)) + "-"
							+ output.format(input.parse(end)));
			pdfFile.delete();
		} catch (Exception e) {
			log.error(e);
		}
	}

	private void invokeNormalEmt(String start, String end, String log,
			String tempFilename, String dhisDatasetValuesets) {
		String[] args = { start, end, log, tempFilename, dhisDatasetValuesets, WebConstants.WEBAPP_NAME, null, "oVersion:" + OpenmrsConstants.OPENMRS_VERSION + ";oModulesFolderPath:" + ModuleUtil.getModuleRepository() };
		Emt.main(args);
	}

	private void returnPdf(HttpServletResponse response, File pdfFile,
			String filenameToReturn) throws FileNotFoundException, IOException {
		if(!filenameToReturn.endsWith(".pdf")) {
			filenameToReturn += ".pdf";
		}
		response.setContentType("application/pdf");
		response.addHeader("Content-Disposition", "attachment; filename="
				+ filenameToReturn);
		response.setContentLength((int) pdfFile.length());

		FileInputStream fileInputStream = null;
		OutputStream responseOutputStream = null;
		try {
			fileInputStream = new FileInputStream(pdfFile);
			responseOutputStream = response.getOutputStream();
			int bytes;
			while ((bytes = fileInputStream.read()) != -1) {
				responseOutputStream.write(bytes);
			}
		} finally {
			if (fileInputStream != null)
				fileInputStream.close();
		}
	}

	private void returnCsv(HttpServletResponse response, File csvFile,
			String filenameToReturn) throws FileNotFoundException, IOException {
		response.setContentType("application/csv");
		response.addHeader("Content-Disposition", "attachment; filename="
				+ filenameToReturn);
		response.setContentLength((int) csvFile.length());

		FileInputStream fileInputStream = null;
		OutputStream responseOutputStream = null;
		try {
			fileInputStream = new FileInputStream(csvFile);
			responseOutputStream = response.getOutputStream();
			int bytes;
			while ((bytes = fileInputStream.read()) != -1) {
				responseOutputStream.write(bytes);
			}
		} finally {
			if (fileInputStream != null)
				fileInputStream.close();
		}
	}

	@RequestMapping(value = "/module/emtfrontend/emtfrontendHmisExport.form", method = RequestMethod.GET)
	private String showHmisExport() {
		return "/module/emtfrontend/emtfrontendHmisExport";
	}

	@RequestMapping(value = "/module/emtfrontend/exportHmisCsv.form", method = RequestMethod.GET)
	private void exportHmisCsv(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			SimpleDateFormat input = new SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat output = new SimpleDateFormat("yyyyMM");

			String date = request.getParameter("startDate");
			log.info(date);

			// get temp file just to get the unique name
			File f = File.createTempFile("temp", null);
			String tempFilename = f.getAbsolutePath();
			f.delete();

			String defaultLocationId = Context.getUserContext().getAuthenticatedUser().getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION);
			String fosaid = "Unable to obtain FOSAID";
			if (defaultLocationId != null && !"".equals(defaultLocationId)) {
				Location l = Context.getLocationService().getLocation(Integer.parseInt(defaultLocationId));
				String locationDescription = l.getDescription();
				fosaid = locationDescription.split(":")[1].trim().split(" ")[0].trim();		
			}
			log.info(fosaid);
			invokeNormalHmisExport(output.format(input.parse(date)) + "02",
					INSTALL_DIR + "EmrMonitoringTool/emt.log", fosaid,
					tempFilename);

			File csvFile = new File(tempFilename);
			// send back as PDF via HTTP
			returnCsv(
					response,
					csvFile,
					"hmis-" + output.format(input.parse(date)));
			csvFile.delete();
		} catch (Exception e) {
			log.error(e);
		}
	}

	private void invokeNormalHmisExport(String date, String log, String fosaid,
			String tempFilename) {
		String[] args = { date, log, fosaid, tempFilename };
		Emt.hmisExport(args);
	}

	/**
	 * This class returns the form backing object. This can be a string, a
	 * boolean, or a normal java pojo. The bean name defined in the
	 * ModelAttribute annotation and the type can be just defined by the return
	 * type of this method
	 */
	@ModelAttribute("theConfig")
	protected Config formBackingObject(HttpServletRequest request)
			throws Exception {
		Properties prop = new Properties();
		InputStream input = null;

		Config c = null;
		try {
			input = new FileInputStream(INSTALL_DIR + "EmrMonitoringTool/emt.properties");
			prop.load(input);
			String days = prop.getProperty("clinicDays", "Mo,Tu,We,Th,Fr");
			String start = prop.getProperty("clinicStart", "800");
			String end = prop.getProperty("clinicEnd", "1700");
			c = new Config(days, start, end);
		} catch (IOException ex) {
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
		return c;
	}

	public class Config {
		public String days;
		String start;
		String end;

		public Config(String days, String start, String end) {
			this.days = days;
			this.start = start;
			this.end = end;
		}

		public String getDays() {
			return days;
		}

		public void setDays(String days) {
			this.days = days;
		}

		public String getStart() {
			return start;
		}

		public void setStart(String start) {
			this.start = start;
		}

		public String getEnd() {
			return end;
		}

		public void setEnd(String end) {
			this.end = end;
		}
	}
}
