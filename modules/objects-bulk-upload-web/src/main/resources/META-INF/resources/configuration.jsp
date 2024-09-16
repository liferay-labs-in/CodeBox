<%@ include file="init.jsp" %>

<%
    Map<Long,String> objectDefinitionMap = ObjectBulkUploadConfigurationUtil._getObjectDefinitionMap(ObjectDefinitionLocalServiceUtil.getObjectDefinitions(-1,-1));
%>


<liferay-portlet:actionURL portletConfiguration="<%= true %>" var="configurationActionURL" />

<liferay-frontend:edit-form
	action="<%= configurationActionURL %>"
	method="post"
	name="fm"
>
	<aui:input name="<%= Constants.CMD %>" type="hidden" value="<%= Constants.UPDATE %>" />

    <liferay-frontend:edit-form-body>

	    <liferay-portlet:renderURL portletConfiguration="<%= true %>" var="configurationRenderURL" />
	    <aui:input name="redirect" type="hidden" value="<%= configurationRenderURL %>" />

        <liferay-frontend:fieldset>
            <aui:select label="objects" name="objectId" value='<%= (String)portletPreferences.getValue("objectId", "") %>'>
                <% for (Map.Entry<Long, String> entry : objectDefinitionMap.entrySet()) { %>
                    <aui:option label="<%= entry.getValue() %>" value="<%= entry.getKey() %>" />
                <% } %>
            </aui:select>
        </liferay-frontend:fieldset>

	</liferay-frontend:edit-form-body>

	<liferay-frontend:edit-form-footer>
    		<liferay-frontend:edit-form-buttons />
    </liferay-frontend:edit-form-footer>
</liferay-frontend:edit-form>