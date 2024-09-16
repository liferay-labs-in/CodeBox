<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %><%@
taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %><%@
taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>


<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %><%@
taglib uri="http://liferay.com/tld/clay" prefix="clay" %><%@
taglib uri="http://liferay.com/tld/frontend" prefix="liferay-frontend" %>><%@
taglib uri="http://liferay.com/tld/util" prefix="liferay-util" %>

<%@page import="java.util.HashMap" %>
<%@page import="java.util.List" %>
<%@page import="java.util.Map" %>
<%@page import="com.liferay.object.service.ObjectDefinitionLocalServiceUtil" %>
<%@page import="com.liferay.asserts.objects.bulk.upload.web.util.configuration.ObjectBulkUploadConfigurationUtil" %>

<%@ page import="com.liferay.portal.kernel.util.Constants" %>

<!-- css file -->
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/main.css" />

<!-- Include the Bootstrap CSS and Font Awesome for icons -->
<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js"></script>

<portlet:defineObjects />

<liferay-theme:defineObjects />
