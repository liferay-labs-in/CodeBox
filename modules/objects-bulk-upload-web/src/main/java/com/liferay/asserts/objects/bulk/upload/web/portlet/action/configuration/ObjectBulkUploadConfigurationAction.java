package com.liferay.asserts.objects.bulk.upload.web.portlet.action.configuration;

import com.liferay.asserts.objects.bulk.upload.web.constants.ObjectsBulkUploadPortletKeys;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.system.SystemObjectDefinitionManagerRegistry;
import com.liferay.portal.kernel.portlet.ConfigurationAction;
import com.liferay.portal.kernel.portlet.DefaultConfigurationAction;
import com.liferay.portal.kernel.util.ParamUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.servlet.http.HttpServletRequest;

import static com.liferay.asserts.objects.bulk.upload.web.util.configuration.ObjectBulkUploadConfigurationUtil._getObjectDefinition;
import static com.liferay.asserts.objects.bulk.upload.web.util.configuration.ObjectBulkUploadConfigurationUtil._getRESTContextPath;

/**
 * @author Akhash R
 */
@Component(
        property = "javax.portlet.name=" + ObjectsBulkUploadPortletKeys.OBJECTSBULKUPLOAD,
        service = ConfigurationAction.class
)
public class ObjectBulkUploadConfigurationAction extends DefaultConfigurationAction{

    @Override
    public String getJspPath(HttpServletRequest httpServletRequest) {
        return "/configuration.jsp";
    }

    @Override
    public void processAction(
            PortletConfig portletConfig, ActionRequest actionRequest,
            ActionResponse actionResponse)
            throws Exception {

        String objectId = ParamUtil.getString(actionRequest, "objectId");
        ObjectDefinition objectDefinition = _getObjectDefinition(objectId);

        setPreference(
                actionRequest, "objectId",
                objectId);
        setPreference(
                actionRequest, "restContextPath",
                _getRESTContextPath(objectDefinition, _systemObjectDefinitionManagerRegistry)
        );
        setPreference(
                actionRequest, "objectName",
               objectDefinition.getLabel(objectDefinition.getDefaultLanguageId())
        );

        super.processAction(portletConfig, actionRequest, actionResponse);
    }

    @Reference
    private SystemObjectDefinitionManagerRegistry
            _systemObjectDefinitionManagerRegistry;

}
