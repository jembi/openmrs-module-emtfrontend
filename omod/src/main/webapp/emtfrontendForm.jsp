<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<h2><spring:message code="emtfrontend.replace.this.link.name" /></h2>

<form method="get" action="/openmrs/module/emtfrontend/generatePDF.form">
	Start Date: <input type="startDate" id="startDate" name="startDate" size="10" onClick="showCalendar(this)" value="" />
	<br/>
	End Date: <input type="endDate" id="endDate" name="endDate" size="10" onClick="showCalendar(this)" value="" />
	<br/>
	<input type="submit" value="Submit"/>			
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
