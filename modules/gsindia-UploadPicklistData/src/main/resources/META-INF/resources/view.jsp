<%@ include file="/init.jsp" %>

<portlet:actionURL name="excelformsubmission" var="excelformsubmission"></portlet:actionURL>
<liferay-ui:tabs names="EXCELFORM" param="tabs2" refresh="<%=false%>" type="tabs nav-tabs-default">
    <liferay-ui:section>
       <div class="container-fluid-1280">
                <aui:form action="<%=excelformsubmission%>" name="excelform" enctype="multipart/form-data" method="post">
                <aui:fieldset cssClass='fieldset'>
                    <aui:input type="hidden" name="tableName" value="EXCEL">
                    </aui:input>
                    <aui:input type="file" name="fileName" size="75"
                        label="uploadExcel" helpMessage="load-excel-file">
                        <aui:validator name="acceptFiles">'xls,xlsx'</aui:validator>
                    </aui:input>
                </aui:fieldset>
                <p> Upload the Excel File with template of three columns (Picklist ERC, Key, Name)</p>
                <aui:button-row>
                    <aui:button type="submit"><liferay-ui:message key="submit-btn"/></aui:button>
                </aui:button-row>
            </aui:form>
        </div>
    </liferay-ui:section>
</liferay-ui:tabs>
