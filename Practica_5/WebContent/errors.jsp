
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@page isErrorPage="true" session="true" %>

<html>
<c:import url="views/head.jsp"></c:import>
<body>
	<c:import url="views/header.jsp"></c:import>

	<h1>Opps...</h1>
	<table>
		<tr valign="top">
			<td width="40%">
				<b>Error:</b>
			</td>
			<td>${pageContext.exception}</td>
		</tr>
		<tr valign="top">
			<td>
				<b>URI:</b>
			</td>
			<td>${pageContext.errorData.requestURI}</td>
		</tr>
		<tr valign="top">
			<td>
				<b>Status code:</b>
			</td>
			<td>${pageContext.errorData.statusCode}</td>
		</tr>
		<tr valign="top">
			<td>
				<b>Stack trace:</b>
			</td>
			<td>
				<c:forEach var="trace" items="${pageContext.exception.stackTrace}">
					<p>${trace}</p>
				</c:forEach>
			</td>
		</tr>
	</table>
	<c:import url="views/footer.jsp"></c:import>
</body>
</html>