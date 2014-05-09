<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<h2><spring:message code="emtfrontend.title" /> <spring:message code="emtfrontend.generate" /></h2>

<form method="get" action="/openmrs/module/emtfrontend/generatePDF.form">
	Start Date: <input type="startDate" id="startDate" name="startDate" size="10" onClick="showCalendar(this)" value="" />
	<br/>
	End Date: <input type="endDate" id="endDate" name="endDate" size="10" onClick="showCalendar(this)" value="" />  (including; for future dates all calculations are done up to today/now)
	<br/>
	<input type="submit" value="Generate PDF"/>			
</form>

<br/>
Note: A PDF file is generated -without any progress indication- and returned to your browser.
<br/>
Caution: No validation for input and error reporting of output on this page yet.
 
<%@ include file="/WEB-INF/template/footer.jsp"%>
