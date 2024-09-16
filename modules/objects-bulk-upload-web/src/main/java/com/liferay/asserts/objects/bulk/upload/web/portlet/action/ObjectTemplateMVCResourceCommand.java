package com.liferay.asserts.objects.bulk.upload.web.portlet.action;

import com.liferay.asserts.objects.bulk.upload.web.constants.ObjectsBulkUploadPortletKeys;
import com.liferay.list.type.model.ListTypeEntry;
import com.liferay.list.type.service.ListTypeEntryService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectDefinitionService;
import com.liferay.object.service.ObjectFieldService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.util.SearchUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class handles generating an Excel template for bulk uploading Liferay Object data.
 * It dynamically creates columns based on ObjectField configurations and applies data validations.
 * <p>
 * The Excel sheet will have dropdowns for picklists, date fields, and other constraints.
 * Hidden sheets are used to store data for dropdowns.
 *
 * @author Akhash R
 *
 */

@Component(
        property = {
                "javax.portlet.name=" + ObjectsBulkUploadPortletKeys.OBJECTSBULKUPLOAD,
                "mvc.command.name=/download/object_template"
        },
        service = MVCResourceCommand.class
)
public class ObjectTemplateMVCResourceCommand extends BaseMVCResourceCommand {

    @Reference
    private ObjectDefinitionService _objectDefinitionService;

    @Reference
    private ObjectFieldService _objectFieldService;

    @Reference
    private ListTypeEntryService _listTypeEntryService;

    private static final int MAX_ROWS = 1000;

    private static final List<String> SKIP_LABELS = Arrays.asList(
            "creator", "createDate", "externalReferenceCode", "id", "modifiedDate", "status");

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    private static Log _log = LogFactoryUtil.getLog(ObjectTemplateMVCResourceCommand.class);

    /**
     * Handles the main resource request for generating and downloading the Excel template.
     * It creates the template based on object fields and applies validations for various field types.
     *
     * @param resourceRequest  the resource request
     * @param resourceResponse the resource response
     */
    @Override
    protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws Exception {
        long objectId = _getObjectId(resourceRequest); // Retrieve objectId from the request
        Collection<ObjectField> objectFields = _getObjectFieldsPage(
                _objectDefinitionService.getObjectDefinition(objectId), resourceRequest).getItems();

        try (Workbook workbook = new XSSFWorkbook()) {  // Create a new Excel workbook
            Sheet mainSheet = workbook.createSheet("Template");
            Sheet hiddenSheet = workbook.createSheet("HiddenData"); // Hidden sheet for dropdown data
            DataValidationHelper dvHelper = mainSheet.getDataValidationHelper();

            // Create headers and apply validations for each field
            _createHeaderRowAndApplyValidations(objectFields, workbook, mainSheet, hiddenSheet, dvHelper);

            _hideSheet(workbook, hiddenSheet);  // Hide the hidden sheet

            // Set the content type and trigger file download
            resourceResponse.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            resourceResponse.setProperty("Content-Disposition", "attachment; filename=template.xlsx");

            try (OutputStream out = resourceResponse.getPortletOutputStream()) {
                workbook.write(out);  // Write the workbook to the output stream
                out.flush();
            }
        } catch (Exception e) {
            _log.error("Error generating Excel template", e);
            throw e;
        }
    }

    /**
     * Creates a header row in the main Excel sheet based on the ObjectField properties.
     * For each object field, this method applies the relevant data validation depending on its business type.
     *
     * @param objectFields   the collection of ObjectField that defines the structure of the Excel sheet
     * @param workbook       the workbook that contains both the main and hidden sheets
     * @param mainSheet      the sheet where the header and data validations are applied
     * @param hiddenSheet    the hidden sheet used for storing dropdown list data
     * @param dvHelper       helper for applying data validations
     */
    private void _createHeaderRowAndApplyValidations(
            Collection<ObjectField> objectFields, Workbook workbook, Sheet mainSheet, Sheet hiddenSheet, DataValidationHelper dvHelper) throws PortalException, ParseException {

        // Create a header row in the main sheet
        Row headerRow = mainSheet.createRow(0);
        int columnIndex = 0;   // To track column index in the main sheet
        int hiddenSheetRow = 0; // To track row index in the hidden sheet for dropdown data


        for (ObjectField objectField : objectFields) {
            String label = objectField.getName();

            // Skip certain fields (e.g., creator, id) based on the SKIP_LABELS list
            if (SKIP_LABELS.contains(label)) {
                continue;  // Skip certain fields like "creator" or "id"
            }

            // Create a new cell in the header row for this object field
            Cell headerCell = headerRow.createCell(columnIndex);
            headerCell.setCellValue(label);  // Set the header cell value

            // Apply appropriate validation based on field type
            _applyValidation(objectField, workbook, mainSheet, hiddenSheet, dvHelper, columnIndex, hiddenSheetRow);
            columnIndex++;
        }
    }

    /**
     * Applies the correct data validation for a given ObjectField, depending on its business type.
     * It handles various types like picklists, booleans, decimals, dates, and integers.
     *
     * @param objectField     the object field for which validation needs to be applied
     * @param workbook        the workbook containing the main and hidden sheets
     * @param mainSheet       the main sheet where validation is applied
     * @param hiddenSheet     the hidden sheet used for dropdown values
     * @param dvHelper        the data validation helper to create validation rules
     * @param columnIndex     the index of the column in the main sheet
     * @param hiddenSheetRow  the index of the row in the hidden sheet for dropdown values
     */
    private void _applyValidation(
            ObjectField objectField,Workbook workbook, Sheet mainSheet, Sheet hiddenSheet, DataValidationHelper dvHelper,
            int columnIndex, int hiddenSheetRow) throws PortalException, ParseException {

        // Switch between various business types and apply relevant validation
        switch (objectField.getBusinessType()) {
            case ObjectsBulkUploadPortletKeys.BUSINESS_TYPE_PICKLIST:
                _createDropdown(workbook, mainSheet, hiddenSheet, _getListEntry(objectField), columnIndex, hiddenSheetRow);
                break;
            case ObjectsBulkUploadPortletKeys.BUSINESS_TYPE_BOOLEAN:
                _createBooleanDropdown(mainSheet, columnIndex);
                break;
            case ObjectsBulkUploadPortletKeys.BUSINESS_TYPE_DECIMAL:
                _addDecimalValidation(mainSheet, dvHelper, columnIndex);
                break;
            case ObjectsBulkUploadPortletKeys.BUSINESS_TYPE_PRECISION_DECIMAL:
                _addBigDecimalValidation(mainSheet, dvHelper, columnIndex);
                break;
            case ObjectsBulkUploadPortletKeys.BUSINESS_TYPE_DATE:
                _addDateValidation(mainSheet, dvHelper, columnIndex);
                break;
            case ObjectsBulkUploadPortletKeys.BUSINESS_TYPE_DATETIME:
                _addDateTimeValidation(mainSheet, dvHelper, columnIndex);
                break;
            case ObjectsBulkUploadPortletKeys.BUSINESS_TYPE_INTEGER:
                _addIntegerValidation(mainSheet, dvHelper, columnIndex);
                break;
            case ObjectsBulkUploadPortletKeys.BUSINESS_TYPE_LONG_INTEGER:
                _addLongIntegerValidation(mainSheet, dvHelper, columnIndex);
                break;
            default:
                // No validation for other types
                break;
        }
    }

    /**
     * Hides the hidden sheet in the Excel workbook to keep dropdown data invisible to the end-user.
     *
     * @param workbook    the workbook that contains the hidden sheet
     * @param hiddenSheet the sheet to be hidden
     */
    private void _hideSheet(Workbook workbook, Sheet hiddenSheet) {
        // Hides the hidden sheet that holds dropdown data
        workbook.setSheetHidden(workbook.getSheetIndex(hiddenSheet), true);
    }

    /**
     * Applies data validation for the boolean.
     */
    private void _createBooleanDropdown(Sheet sheet, int columnIndex) {
        DataValidationHelper dvHelper = sheet.getDataValidationHelper();
        DataValidationConstraint dvConstraint = dvHelper.createExplicitListConstraint(new String[]{String.valueOf(Boolean.TRUE), String.valueOf(Boolean.FALSE)});

        _addValidation(sheet, dvHelper, dvConstraint, columnIndex);
    }

    /**
     * Applies data validation for the dropdown.
     */
    private void _createDropdown(Workbook workbook, Sheet sheet, Sheet hiddenSheet, String[] options, int mainSheetColumn, int hiddenSheetRow) {
        for (int i = 0; i < options.length; i++) {
            Row row = hiddenSheet.getRow(hiddenSheetRow + i);
            if (row == null) row = hiddenSheet.createRow(hiddenSheetRow + i);
            row.createCell(mainSheetColumn).setCellValue(options[i]);
        }

        String colLetter = CellReference.convertNumToColString(mainSheetColumn);
        String range = "HiddenData!$" + colLetter + "$" + (hiddenSheetRow + 1) + ":$" + colLetter + "$" + (hiddenSheetRow + options.length);
        Name namedRange = workbook.createName();
        namedRange.setNameName("Options_" + mainSheetColumn); // Ensure the name is unique
        namedRange.setRefersToFormula(range);

        DataValidationHelper dvHelper = sheet.getDataValidationHelper();
        DataValidationConstraint dvConstraint = dvHelper.createFormulaListConstraint("Options_" + mainSheetColumn);

        _addValidation(sheet, dvHelper, dvConstraint, mainSheetColumn);
    }

    /**
     * Applies data validation.
     */
    private void _addValidation(Sheet sheet, DataValidationHelper dvHelper, DataValidationConstraint dvConstraint, int column) {
        CellRangeAddressList addressList = new CellRangeAddressList(1, MAX_ROWS, column, column);
        DataValidation validation = dvHelper.createValidation(dvConstraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }

    /**
     * Applies data validation for the decimal.
     */
    private void _addDecimalValidation(Sheet sheet, DataValidationHelper dvHelper, int column) {
        DataValidationConstraint dvConstraint = dvHelper.createDecimalConstraint(
                DataValidationConstraint.OperatorType.BETWEEN, "-99999999.99", "99999999.99");
        _addValidation(sheet, dvHelper, dvConstraint, column);
    }

    /**
     * Applies data validation for the big decimal.
     */
    private void _addBigDecimalValidation(Sheet sheet, DataValidationHelper dvHelper, int column) {
        DataValidationConstraint dvConstraint = dvHelper.createDecimalConstraint(
                DataValidationConstraint.OperatorType.BETWEEN, "-9999999999999999.9999", "9999999999999999.9999");
        _addValidation(sheet, dvHelper, dvConstraint, column);
    }

    /**
     * Applies data validation for the date.
     */
    private void _addDateValidation(Sheet sheet, DataValidationHelper dvHelper, int column) throws ParseException {
        DataValidationConstraint dvConstraint = dvHelper.createDateConstraint(DataValidationConstraint.OperatorType.BETWEEN,
                ""+DateUtil.getExcelDate(SDF.parse("01/01/1900")), ""+DateUtil.getExcelDate(SDF.parse("31/12/9999")), "");
        _addValidation(sheet, dvHelper, dvConstraint, column);
    }

    /**
     * Applies data validation for the date and time.
     */
    private void _addDateTimeValidation(Sheet sheet, DataValidationHelper dvHelper, int column) {

        Date minimumDate = new Date(1990, Calendar.JANUARY, 1);
        Date maximumDate = new Date(3000, Calendar.JANUARY, 1);

        DataValidationConstraint dvConstraint = dvHelper.createDateConstraint(
                DataValidationConstraint.ValidationType.DATE,
                minimumDate.toString(),
                maximumDate.toString(),
                "M/d/yyyy h:mm:ss"
        );
        _addValidation(sheet, dvHelper, dvConstraint, column);
    }

    /**
     * Applies data validation for the integer.
     */
    private void _addIntegerValidation(Sheet sheet, DataValidationHelper dvHelper, int column) {
        DataValidationConstraint dvConstraint = dvHelper.createIntegerConstraint(
                DataValidationConstraint.OperatorType.BETWEEN, "-2147483648", "2147483647");
        _addValidation(sheet, dvHelper, dvConstraint, column);
    }

    /**
     * Applies data validation for the long integer.
     */
    private void _addLongIntegerValidation(Sheet sheet, DataValidationHelper dvHelper, int column) {
        DataValidationConstraint dvConstraint = dvHelper.createIntegerConstraint(
                DataValidationConstraint.OperatorType.BETWEEN, "-9223372036854775808", "9223372036854775807");
        _addValidation(sheet, dvHelper, dvConstraint, column);
    }

    /**
     * Retrieves the list of entries (keys) for a picklist type field.
     * This method fetches entries from the ListTypeEntry service and returns them as an array of strings.
     * The entries are used to populate dropdowns in the Excel template.
     *
     * @param objectField the ObjectField for which picklist entries need to be fetched
     * @return an array of strings representing the picklist entries
     * @throws PortalException if there is an issue fetching the list entries
     */
    private String[] _getListEntry(ObjectField objectField) throws PortalException {
        // Fetches entries for picklist type fields
        return _listTypeEntryService.getListTypeEntries(objectField.getListTypeDefinitionId(), -1,-1).stream()
                .map(ListTypeEntry::getKey)
                .toArray(String[]::new);
    }

    /**
     * Retrieves the object ID from the request.
     *
     * @param resourceRequest the resource request
     * @return the object ID
     */
    private long _getObjectId(ResourceRequest resourceRequest) {
        return GetterUtil.getLong(resourceRequest.getPreferences().getValue("objectId", "0"));
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

}
