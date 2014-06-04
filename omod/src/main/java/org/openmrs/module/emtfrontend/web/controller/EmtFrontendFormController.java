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
import org.openmrs.module.emtfrontend.Constants;
import org.openmrs.module.emtfrontend.Emt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EmtFrontendFormController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@RequestMapping(value = "/module/emtfrontend/emtfrontendConfig.form", method = RequestMethod.GET)
	private String showConfig() {
		return "/module/emtfrontend/emtfrontendConfig";
	}

	@RequestMapping(value = "/module/emtfrontend/configure.form", method = RequestMethod.POST)
	public String onSubmit(@RequestParam("clinicDays") String days, @RequestParam("clinicStart") String start, @RequestParam("clinicEnd") String end) {
		Properties prop = new Properties();
		OutputStream output = null;
		try {
			output = new FileOutputStream(Constants.RUNTIME_DIR + "/emt.properties");
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

	@RequestMapping(value = "/module/emtfrontend/generatePDF.form", method = RequestMethod.GET)
	private void generatePDF(HttpServletRequest request,
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
					output.format(input.parse(end)), Constants.RUNTIME_DIR + "/emt.log",
					tempFilename);

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
			String tempFilename) {
		String[] args = { start, end, log, tempFilename };
		Emt.main(args);
	}

	private void invokeJarFromCustomCloassloader() {
		// http://www.coderanch.com/t/529764/java/java/run-jar-java-application
	}

	private void invokeExternalProcess() throws IOException {
		// most likely not clever as external processes forked from java require
		// again
		// the same amount of assigned memory, thus doubling the -Xmx settings.
		String s = Constants.INSTALL_DIR + "/EmrMonitoringTool/generate-example-report.sh";
		Process pro2 = Runtime.getRuntime().exec(s);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				pro2.getInputStream()));
	}

	private void returnPdf(HttpServletResponse response, File pdfFile,
			String filenameToReturn) throws FileNotFoundException, IOException {
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
			input = new FileInputStream(Constants.RUNTIME_DIR + "/emt.properties");
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
