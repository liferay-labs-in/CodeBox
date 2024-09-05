package com.liferay.asserts.objects.bulk.upload.web.portlet.action;

import com.liferay.asserts.objects.bulk.upload.web.constants.ObjectsBulkUploadPortletKeys;
import com.liferay.list.type.model.ListTypeEntry;
import com.liferay.list.type.service.ListTypeEntryService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectDefinitionService;
import com.liferay.object.service.ObjectFieldService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.util.SearchUtil;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.liferay.asserts.objects.bulk.upload.web.util.ObjectFieldValidatorUtil._validateValues;

/**
 * Handles the upload of object templates via ResourceCommand.
 * This class processes JSON data, validates it according to business types, and generates success or error responses.
 *
 * @author Akhash R
 *
 */
@Component(
        property = {
                "javax.portlet.name=" + ObjectsBulkUploadPortletKeys.OBJECTSBULKUPLOAD,
                "mvc.command.name=/upload/object_template"
        },
        service = MVCResourceCommand.class
)
public class ObjectTemplateUploadMVCResourceCommand extends BaseMVCResourceCommand {

    @Reference
    private ObjectDefinitionService _objectDefinitionService;

    @Reference
    private ObjectFieldService _objectFieldService;

    @Reference
    private ListTypeEntryService _listTypeEntryService;

    private static Log _log = LogFactoryUtil.getLog(ObjectTemplateUploadMVCResourceCommand.class);

    /**
     * Processes the resource request to upload object templates.
     * Retrieves the object fields and validates the incoming JSON data.
     *
     * @param resourceRequest  the resource request
     * @param resourceResponse the resource response
     */
    @Override
    protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws Exception {
        long objectId = _getObjectId(resourceRequest);

        // Get the collection of object fields for the object definition
        Collection<ObjectField> objectFields = _getObjectFieldsPage(
                _objectDefinitionService.getObjectDefinition(objectId), resourceRequest).getItems();

        // Map object field names to their respective ObjectField instances
        Map<String, ObjectField> objectBusinessTypeMap = objectFields.stream()
                .collect(Collectors.toMap(ObjectField::getName, Function.identity()));

        // Retrieve the JSON data from the request
        String data = ParamUtil.getString(resourceRequest, "data");
        JSONArray jsonBody = JSONFactoryUtil.createJSONArray(data);

        JSONArray jsonErrorResponse = JSONFactoryUtil.createJSONArray(); // For storing validation errors
        JSONArray jsonSuccessResponse = _processJsonFields(jsonBody, objectBusinessTypeMap, jsonErrorResponse);

        // Prepare the response JSON with success or failure status
        JSONObject response = JSONFactoryUtil.createJSONObject();
        response.put("status", JSONUtil.isEmpty(jsonErrorResponse) ? "Success" : "Failed");
        response.put("data", JSONUtil.isEmpty(jsonErrorResponse) ? jsonSuccessResponse : jsonErrorResponse);

        // Send the JSON response
        resourceResponse.setContentType("application/json");
        try {
            resourceResponse.getWriter().write(response.toJSONString());
        } catch (IOException e) {
            _log.error("Error writing JSON response", e); // Log the error instead of printStackTrace
        }
    }

    /**
     * Processes JSON data and validates each field based on its business type.
     *
     * @param jsonBody             the JSON body containing field data
     * @param objectBusinessTypeMap map of object field names to ObjectField instances
     * @param jsonErrorResponse     JSON array to store any validation errors
     * @return a JSON array containing the success or error responses for each row
     */
    private JSONArray _processJsonFields(JSONArray jsonBody, Map<String, ObjectField> objectBusinessTypeMap, JSONArray jsonErrorResponse) {
        if (Validator.isNull(jsonBody) || jsonBody.length() == 0) {
            return JSONFactoryUtil.createJSONArray(); // Return empty array if no data
        }

        AtomicLong rowCounter = new AtomicLong(2); // Row counter starting from 2 (for Excel-style row numbers)

        // Process each JSON object (row) in the JSON array
        JSONArray jsonResponse = JSONFactoryUtil.createJSONArray();
        IntStream.range(0, jsonBody.length())
                .mapToObj(jsonBody::getJSONObject)
                .forEach(jsonField -> {
                    JSONObject jsonObjectResponse = JSONFactoryUtil.createJSONObject();
                    AtomicLong columnCounter = new AtomicLong(0);

                    // For each field in the row, validate the value based on the business type
                    jsonField.toMap().forEach((key, value) -> {
                        ObjectField objectField = objectBusinessTypeMap.get(key);
                        if (objectField != null) {
                            String stringValue = value != null ? value.toString() : StringPool.BLANK;
                            _handleBusinessType(objectField, jsonObjectResponse, stringValue, jsonErrorResponse, rowCounter.get(), columnCounter.get());
                        }
                        columnCounter.incrementAndGet();
                    });

                    jsonResponse.put(jsonObjectResponse); // Add the validated row to the response
                    rowCounter.incrementAndGet();
                });

        return jsonResponse;
    }

    /**
     * Handles the validation of a field value based on its business type.
     *
     * @param objectField       the object field definition
     * @param jsonObjectResponse the JSON object to store the validated field
     * @param value             the value to be validated
     * @param jsonErrorResponse  the JSON array to store validation errors
     * @param row               the row number (for error tracking)
     * @param column            the column number (for error tracking)
     */
    private void _handleBusinessType(ObjectField objectField, JSONObject jsonObjectResponse, String value, JSONArray jsonErrorResponse, Long row, Long column) {
        String businessType = objectField.getBusinessType();

        switch (businessType) {
            case ObjectsBulkUploadPortletKeys.BUSINESS_TYPE_PICKLIST:
                _handlePickList(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column);
                break;
            case ObjectsBulkUploadPortletKeys.BUSINESS_TYPE_BOOLEAN:
                _handleBoolean(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column);
                break;
            case ObjectsBulkUploadPortletKeys.BUSINESS_TYPE_DECIMAL:
                _handleDecimal(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column);
                break;
            case ObjectsBulkUploadPortletKeys.BUSINESS_TYPE_PRECISION_DECIMAL:
                _handlePrecisionDecimal(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column);
                break;
            case ObjectsBulkUploadPortletKeys.BUSINESS_TYPE_DATE:
                _handleDate(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column);
                break;
            case ObjectsBulkUploadPortletKeys.BUSINESS_TYPE_DATETIME:
                _handleDateTime(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column);
                break;
            case ObjectsBulkUploadPortletKeys.BUSINESS_TYPE_INTEGER:
                _handleInteger(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column);
                break;
            case ObjectsBulkUploadPortletKeys.BUSINESS_TYPE_LONG_INTEGER:
                _handleLongInteger(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column);
                break;
            case ObjectsBulkUploadPortletKeys.BUSINESS_TYPE_RICH_TEXT:
            case ObjectsBulkUploadPortletKeys.BUSINESS_TYPE_LONG_TEXT:
            case ObjectsBulkUploadPortletKeys.BUSINESS_TYPE_TEXT:
            case ObjectsBulkUploadPortletKeys.BUSINESS_TYPE_MULTI_SELECT_PICKLIST:
            case ObjectsBulkUploadPortletKeys.BUSINESS_TYPE_ENCRYPTED:
                _handleText(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column);
                break;
            default:
                // No validation for other types
                break;
        }
    }

    /**
     * Validation for the text.
     */
    private void _handleText(ObjectField objectField, JSONObject jsonObjectResponse, String value, JSONArray jsonErrorResponse, Long row, Long column) {
        if (value == null || value.trim().isEmpty()) {
            _validateAndPutValue(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column, null);
            return;
        }

        _validateAndPutValue(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column, String.valueOf(value));
    }

    /**
     * Validation for the boolean.
     */
    private void _handleBoolean(ObjectField objectField, JSONObject jsonObjectResponse, String value, JSONArray jsonErrorResponse, Long row, Long column) {
        if (value == null || value.trim().isEmpty()) {
            _validateAndPutValue(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column, null);
            return;
        }

        _validateAndPutValue(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column, Boolean.parseBoolean(value));
    }

    /**
     * Validation for the integer.
     */
    private void _handleInteger(ObjectField objectField, JSONObject jsonObjectResponse, String value, JSONArray jsonErrorResponse, Long row, Long column) {
        if (value == null || value.trim().isEmpty()) {
            _validateAndPutValue(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column, null);
            return;
        }

        _validateAndPutValue(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column, Integer.valueOf(value));
    }

    /**
     * Validation for the long integer.
     */
    private void _handleLongInteger(ObjectField objectField, JSONObject jsonObjectResponse, String value, JSONArray jsonErrorResponse, Long row, Long column) {
        if (value == null || value.trim().isEmpty()) {
            _validateAndPutValue(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column, null);
            return;
        }

        _validateAndPutValue(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column, Long.valueOf(value));
    }

    /**
     * Validation for the decimal.
     */
    private void _handleDecimal(ObjectField objectField, JSONObject jsonObjectResponse, String value, JSONArray jsonErrorResponse, Long row, Long column) {
        if (value == null || value.trim().isEmpty()) {
            _validateAndPutValue(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column, null);
            return;
        }

        _validateAndPutValue(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column, new Float(value));
    }

    /**
     * Validation for the precision decimal.
     */
    private void _handlePrecisionDecimal(ObjectField objectField, JSONObject jsonObjectResponse, String value, JSONArray jsonErrorResponse, Long row, Long column) {
        if (value == null || value.trim().isEmpty()) {
            _validateAndPutValue(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column, null);
            return;
        }

        _validateAndPutValue(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column, new BigDecimal(value));
    }

    /**
     * Validation for the date.
     */
    private void _handleDate(ObjectField objectField, JSONObject jsonObjectResponse, String value, JSONArray jsonErrorResponse, Long row, Long column) {
        if (value == null || value.trim().isEmpty()) {
            _validateAndPutValue(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column, null);
            return;
        }

        try {
            LocalDate localDate = LocalDate.parse(value, DateTimeFormatter.ofPattern("M/d/yyyy"));
            String formattedDate = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            _validateAndPutValue(objectField, jsonObjectResponse, formattedDate, jsonErrorResponse, row, column, formattedDate);
        } catch (Exception e) {
            jsonErrorResponse.put(_getError(row, column, "Error parsing date: " + value));
        }
    }

    /**
     * Validation for the date and time.
     */
    private void _handleDateTime(ObjectField objectField, JSONObject jsonObjectResponse, String value, JSONArray jsonErrorResponse, Long row, Long column) {
        if (value == null || value.trim().isEmpty()) {
            _validateAndPutValue(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column, null);
            return;
        }

        try {
            DateTimeFormatter inputFormatter = new DateTimeFormatterBuilder()
                    .appendPattern("M/d/yyyy H:mm:ss")
                    .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
                    .toFormatter();

            LocalDateTime localDateTime = LocalDateTime.parse(value, inputFormatter);
            ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
            String formattedDate = DateTimeFormatter.ISO_INSTANT.format(zonedDateTime.toInstant());

            jsonObjectResponse.put(objectField.getName(), formattedDate);
        } catch (Exception e) {
            jsonErrorResponse.put(_getError(row, column, "Error parsing date and time: " + value));
        }
    }

    /**
     * Validation for the pick-list.
     */
    private void _handlePickList(ObjectField objectField, JSONObject jsonObjectResponse, String value, JSONArray jsonErrorResponse, Long row, Long column) {
        if (value == null || value.trim().isEmpty()) {
            _validateAndPutValue(objectField, jsonObjectResponse, value, jsonErrorResponse, row, column, null);
            return;
        }

        try {
            _validateValues(value, objectField);
            Map<String, String> listEntryMap = _getListEntryMap(objectField);

            jsonObjectResponse.put(objectField.getName(), _toPickListJsonObject(value, listEntryMap.get(value)));
        } catch (PortalException e) {
            jsonErrorResponse.put(_getError(row, column, e.getMessage()));
        }
    }

    /**
     * Validates the given value and then adds it to the JSON response object.
     * If validation fails, adds the error to the JSON error response.
     *
     * @param objectField        the ObjectField being processed
     * @param jsonObjectResponse the JSON object where the result will be added
     * @param value              the value to be validated and added
     * @param jsonErrorResponse  the JSON array where errors will be added, if any
     * @param row                the row number of the Excel sheet where the value is located
     * @param column             the column number of the Excel sheet where the value is located
     * @param parsedValue        the parsed value to be added (can be null)
     */
    private void _validateAndPutValue(ObjectField objectField, JSONObject jsonObjectResponse, String value, JSONArray jsonErrorResponse, Long row, Long column, Object parsedValue) {
        try {
            // Validate the value based on the object field
            _validateValues(value, objectField);

            // Add the parsed value to the JSON response if not null
            if (Validator.isNotNull(parsedValue)) {
                jsonObjectResponse.put(objectField.getName(), parsedValue);
            }
        } catch (PortalException e) {
            // Log validation error and add it to JSON error response
            _log.error("Validation error: " + e.getMessage(), e);
            jsonErrorResponse.put(_getError(row, column, e.getMessage()));
        }
    }

    /**
     * Retrieves a page of object fields for a given object definition.
     *
     * @param value the key value
     * @param listEntryMap map of list entry
     * @return the picklist json
     */
    private JSONObject _toPickListJsonObject(String value, String listEntryMap) {
        JSONObject pickListJson = JSONFactoryUtil.createJSONObject();

        pickListJson.put("key", value);
        pickListJson.put("name", _parseLabelFromXML(listEntryMap));

        return pickListJson;
    }

    /**
     * Retrieves a page of object fields for a given object definition.
     *
     * @param objectField the object definition
     * @return map of list entry
     * @throws PortalException if a portal exception occurred
     */
    private Map<String, String> _getListEntryMap(ObjectField objectField) throws PortalException {
        return _listTypeEntryService.getListTypeEntries(objectField.getListTypeDefinitionId(), -1, -1).stream()
                .collect(Collectors.toMap(ListTypeEntry::getKey, ListTypeEntry::getName));
    }

    /**
     * Retrieves a page of object fields for a given object definition.
     *
     * @param objectDefinition the object definition
     * @param resourceRequest the resource request
     * @return a page of object fields
     * @throws PortalException if a portal exception occurred
     */
    private Page<ObjectField> _getObjectFieldsPage(
            ObjectDefinition objectDefinition, ResourceRequest resourceRequest)
            throws Exception {

        ThemeDisplay themeDisplay = (ThemeDisplay) resourceRequest.getAttribute(WebKeys.THEME_DISPLAY);

        return SearchUtil.search(
                null,
                booleanQuery -> {
                },
                null, com.liferay.object.model.ObjectField.class.getName(),
                null, null,
                queryConfig -> queryConfig.setSelectedFieldNames(
                        Field.ENTRY_CLASS_PK),
                searchContext -> {
                    searchContext.setAttribute(Field.NAME, null);
                    searchContext.setAttribute("label", null);
                    searchContext.setAttribute(
                            "objectDefinitionId",
                            objectDefinition.getObjectDefinitionId());
                    searchContext.setCompanyId(themeDisplay.getCompanyId());
                },
                null,
                document -> _objectFieldService.getObjectField(
                        GetterUtil.getLong(document.get(Field.ENTRY_CLASS_PK)))
        );
    }

    /**
     * Retrieves the object ID from the request.
     *
     * @param labelXML the xml of the label
     * @return the parsed label
     */
    public static String _parseLabelFromXML(String labelXML) {
        if (labelXML == null || labelXML.isEmpty()) {
            return "";
        }

        try (StringReader stringReader = new StringReader(labelXML)) {
            InputSource inputSource = new InputSource(stringReader);
            Document document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(inputSource);

            XPath xPath = XPathFactory.newInstance().newXPath();
            Node node = (Node) xPath.evaluate("//Name[@language-id='en_US']", document, XPathConstants.NODE);

            if (node != null) {
                return node.getTextContent().trim();
            }
        } catch (Exception e) {
            _log.error("Error parsing XML: " + labelXML, e);
        }

        return "";
    }

    /**
     * Retrieves the object ID from the request.
     *
     * @param resourceRequest the resource request
     * @return the object ID
     */
    public static long _getObjectId(ResourceRequest resourceRequest) {
        return GetterUtil.getLong(resourceRequest.getPreferences().getValue("objectId", "0"));
    }

    /**
     * Adds validation error to the error response.
     *
     * @param errorText string containing validation errors
     * @param row the row number
     * @param column the column number
     */
    private static JSONObject _getError(Long row, Long column, String errorText) {
        JSONObject error = JSONFactoryUtil.createJSONObject();

        error.put("row", row);
        error.put("column", column);
        error.put("error", errorText);

        return error;
    }

}
