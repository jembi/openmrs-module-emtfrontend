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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

//import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * This class configured as controller using annotation and mapped with the URL of 'module/emtfrontend/emtfrontendLink.form'.
 */
@Controller
//@RequestMapping(value = "/module/emtfrontend/emtfrontendLink.form")
public class EmtFrontendFormController{
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/** Success form view name */
	private final String SUCCESS_FORM_VIEW = "/module/emtfrontend/emtfrontendForm";
	
	/**
	 * Initially called after the formBackingObject method to get the landing form name  
	 * @return String form view name
	 */
	@RequestMapping(value = "/module/emtfrontend/emtfrontendLink.form", method = RequestMethod.GET)
    private String showForm() { 
		return SUCCESS_FORM_VIEW;	}

	@RequestMapping(value = "/module/emtfrontend/generatePDF.form", method = RequestMethod.GET)
    private void generatePDF(HttpServletRequest request, HttpServletResponse response) {
		PDDocument p = null;
		
		  try {

			  
			  String s="/home/hc-admin/EmrMonitoringTool/generate-example-report.sh";   
			  Process pro2 = Runtime.getRuntime().exec(s); 
			  BufferedReader in = new BufferedReader(new InputStreamReader(pro2.getInputStream()));
			  
		File pdfFile = new File("/tmp/emt.pdf");

		response.setContentType("application/pdf");
		response.addHeader("Content-Disposition", "attachment; filename=abc.pdf");
		response.setContentLength((int) pdfFile.length());

		FileInputStream fileInputStream = new FileInputStream(pdfFile);
		OutputStream responseOutputStream = response.getOutputStream();
		int bytes;
		while ((bytes = fileInputStream.read()) != -1) {
			responseOutputStream.write(bytes);
		}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * All the parameters are optional based on the necessity  
	 * 
	 * @param httpSession
	 * @param anyRequestObject
	 * @param errors
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST)
	public String onSubmit(HttpSession httpSession,
	                               @ModelAttribute("anyRequestObject") Object anyRequestObject, BindingResult errors) {
		
		if (errors.hasErrors()) {
			// return error view
		}
		
		return SUCCESS_FORM_VIEW;
	}
	
	/**
	 * This class returns the form backing object. This can be a string, a boolean, or a normal java
	 * pojo. The bean name defined in the ModelAttribute annotation and the type can be just
	 * defined by the return type of this method
	 */
	@ModelAttribute("thePatientList")
	protected Collection<Patient> formBackingObject(HttpServletRequest request) throws Exception {
		// get all patients that have an identifier "101" (from the demo sample data)
		// see http://resources.openmrs.org/doc/index.html?org/openmrs/api/PatientService.html for
		// a list of all PatientService methods
		Collection<Patient> patients = Context.getPatientService().findPatients("101", false);
		ArrayList list = new ArrayList();
		list.add("a");
		list.add("b");
		
		// this object will be made available to the jsp page under the variable name
		// that is defined in the @ModuleAttribute tag
		return list;
	}
	
}
