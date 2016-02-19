<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<h2><spring:message code="emtfrontend.title" /> <spring:message code="emtfrontend.generate" /></h2>

<table>
<form method="POST">
<tr>
	<td>Start Date:</td>
	<td><input type="startDate" id="startDate" name="startDate" size="10" onClick="showCalendar(this)" value="" /></td>
</tr>
<tr>
	<td>End Date:</td>
	<td><input type="endDate" id="endDate" name="endDate" size="10" onClick="showCalendar(this)" value="" /> </td>
	<td>(including; for future dates all calculations are done up to today/now)</td>
</tr>
<tr>
	<td><input type="submit" value="Generate PDF"/></td>			
</tr>
</form>
</table>

<br/><br/>
Note: A PDF file is generated -without any progress indication- and returned to your browser.
<br/><br/>
Caution: No validation for input and error reporting of output on this page yet.
 
<%@ include file="/WEB-INF/template/footer.jsp"%>
