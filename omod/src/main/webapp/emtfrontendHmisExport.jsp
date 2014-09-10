<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<h2><spring:message code="emtfrontend.title" /> <spring:message code="emtfrontend.hmisexport" /></h2>

<table>
<form method="get" action="/openmrs/module/emtfrontend/exportHmisCsv.form">
<tr>
	<td>Select date within relevant month:</td>
	<td><input type="startDate" id="startDate" name="startDate" size="10" onClick="showCalendar(this)" value="" /></td>
</tr>
<tr>
	<td><input type="submit" value="Generate HMIS CSV Export"/></td>			
</tr>
</form>
</table>

<br/><br/>
Notes: For complete and accurate HMIS data, a past month need to be chosen. If the current month is selected, data elements might be missing. 
<br/><br/>
A CSV file is generated -without any progress indication- and returned to your browser.
<br/><br/>
Caution: No validation for input and error reporting of output on this page yet.
<br/><br/>
The FOSAID of the location for the HMIS export is taken from the Default Location of the current user's profile. Ensure this is configured accordingly.
<%@ include file="/WEB-INF/template/footer.jsp"%>
