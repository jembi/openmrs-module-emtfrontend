<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<h2><spring:message code="emtfrontend.title" /> <spring:message code="emtfrontend.config" /></h2>

<table>
<form method="post" action="/openmrs/module/emtfrontend/configure.form">
<tr>
	<td>Clinic days:</td>
	<td><input type="clinicDays" id="clinicDays" name="clinicDays" size="15" value="${theConfig.days}" /> </td>
	<td>(with 2 character English abbreviation, comma separated, no spaces; e.g. Mo,We,Fr)</td>  
</tr>
<tr>
	<td>Clinic Start hour:</td>
	<td><input type="clinicStart" id="clinicStart" name="clinicStart" size="2" value="${theConfig.start}" /></td>
	<td>(24 hours format, minutes including, no colon; e.g. 800)</td>
</tr>
<tr>
	<td>Clinic End hour:</td>
	<td><input type="clinicEnd" id="clinicEnd" name="clinicEnd" size="2" value="${theConfig.end}" /></td>
	<td>(24 hours format, minutes including, no colon; e.g. 1700)</td>
</tr>
<tr>
	<td><input type="submit" value="Save configuration"/></td>			
</tr>
</form>
</table>

<br/>
Note: Configurations are stored in file ${emrConfig}

<%@ include file="/WEB-INF/template/footer.jsp"%>
