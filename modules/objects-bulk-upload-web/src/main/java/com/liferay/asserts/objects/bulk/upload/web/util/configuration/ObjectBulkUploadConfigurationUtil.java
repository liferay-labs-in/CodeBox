package com.liferay.asserts.objects.bulk.upload.web.util.configuration;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.system.JaxRsApplicationDescriptor;
import com.liferay.object.system.SystemObjectDefinitionManager;
import com.liferay.object.system.SystemObjectDefinitionManagerRegistry;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

/**
 * @author Akhash R
 */
public class ObjectBulkUploadConfigurationUtil {

    private static Log _log = LogFactoryUtil.getLog(ObjectBulkUploadConfigurationUtil.class);

    /**
     * Retrieves the ObjectDefinition for the given objectId.
     *
     * @param objectId The string representation of the object ID.
     * @return The ObjectDefinition associated with the given objectId.
     * @throws PortalException If the objectId is invalid or the ObjectDefinition cannot be retrieved.
     */
    public static ObjectDefinition _getObjectDefinition(String objectId) throws PortalException {
        try {
            // Parsing objectId and fetching the ObjectDefinition using the local service
            return ObjectDefinitionLocalServiceUtil.getObjectDefinition(Long.parseLong(objectId));
        } catch (NumberFormatException e) {
            _log.error("Error while getting object definition: " + objectId, e);
        }

        return null;
    }

    /**
     * Returns the REST context path for the given ObjectDefinition.
     * It determines if the object is a system object or a custom object and
     * retrieves the appropriate REST context path.
     *
     * @param serviceBuilderObjectDefinition The ObjectDefinition to retrieve the REST context path for.
     * @param systemObjectDefinitionManagerRegistry The registry to lookup system object definitions.
     * @return The REST context path for the object definition or null if unavailable.
     */
    public static String _getRESTContextPath(ObjectDefinition serviceBuilderObjectDefinition, SystemObjectDefinitionManagerRegistry systemObjectDefinitionManagerRegistry) {

        if (serviceBuilderObjectDefinition == null) {
            return null;
        }

        if (serviceBuilderObjectDefinition.isUnmodifiableSystemObject()) {
            SystemObjectDefinitionManager systemObjectDefinitionManager =
                    systemObjectDefinitionManagerRegistry.
                            getSystemObjectDefinitionManager(
                                    serviceBuilderObjectDefinition.getName());

            if (systemObjectDefinitionManager != null) {
                JaxRsApplicationDescriptor jaxRsApplicationDescriptor =
                        systemObjectDefinitionManager.
                                getJaxRsApplicationDescriptor();

                return "/o/" + jaxRsApplicationDescriptor.getRESTContextPath();
            }
        }
        else {
            return "/o" + serviceBuilderObjectDefinition.getRESTContextPath();
        }

        return null;
    }

    private ObjectBulkUploadConfigurationUtil() {
        throw new IllegalStateException("Utility class");
    }

}
