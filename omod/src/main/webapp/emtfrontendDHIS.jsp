<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>
<h2><spring:message code="emtfrontend.generateDHIS"/></h2>

<div style="color:green;">
	${message}
</div>
<form method="POST">
	<input type="submit" value='<spring:message code="emtfrontend.generateDHIS" />'/>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
