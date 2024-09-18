package com.excel.feature.bulk.upload.picklist.portlet;

import com.excel.feature.bulk.upload.picklist.constants.ExcelFeaturePicklistBulkUploadPortletKeys;
import com.liferay.list.type.model.ListTypeDefinition;
import com.liferay.list.type.model.ListTypeEntry;
import com.liferay.list.type.service.ListTypeDefinitionLocalServiceUtil;
import com.liferay.list.type.service.ListTypeEntryLocalServiceUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.osgi.service.component.annotations.Component;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * @author Chanchal Singla
 */
@Component(
        property = {
                "com.liferay.portlet.display-category=category.sample",
                "com.liferay.portlet.header-portlet-css=/css/main.css",
                "com.liferay.portlet.instanceable=true",
                "javax.portlet.display-name=ExcelFeaturePicklistBulkUpload",
                "javax.portlet.init-param.template-path=/",
                "javax.portlet.init-param.view-template=/view.jsp",
                "javax.portlet.name=" + ExcelFeaturePicklistBulkUploadPortletKeys.EXCELFEATUREPICKLISTBULKUPLOAD,
                "javax.portlet.resource-bundle=content.Language",
                "javax.portlet.security-role-ref=power-user,user"
        },
        service = Portlet.class
)
public class ExcelFeaturePicklistBulkUploadPortlet extends MVCPortlet {
    private static final Log log = LogFactoryUtil.getLog(ExcelFeaturePicklistBulkUploadPortlet.class);

    private static final String EXCELFILEUPLOADFIELDNAME = "fileName";
    private static final String EXCELFILEUPLOADTABLENAME = "tableName";
    private static final String EXCEL_IMPORT_FAILED = "excel-import-failed";

    @Override
    public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException {
        uploadExcel(actionRequest, actionResponse);
    }

    private void uploadExcel(ActionRequest actionRequest, ActionResponse actionResponse) {
        boolean uploadSuccess = Boolean.FALSE;
        try {
            UploadPortletRequest uploadPortletRequest = PortalUtil.getUploadPortletRequest(actionRequest);
            File file = uploadPortletRequest.getFile(EXCELFILEUPLOADFIELDNAME);
            String tableName = ParamUtil.getString(actionRequest, EXCELFILEUPLOADTABLENAME);
            if (uploadPortletRequest.getSize(EXCELFILEUPLOADFIELDNAME) == 0) {
                SessionMessages.add(actionRequest, EXCEL_IMPORT_FAILED);
                throw new IOException("Upload File size is 0");
            }
            if (tableName.equals("EXCEL")) {
                uploadSuccess = readExcelFile(file, actionRequest);
                log.debug("Is data added successfully for UOM ? : " + uploadSuccess);
            }
        } catch (Exception e) {
            log.error("Exception while uploading excel " + e);
            SessionMessages.add(actionRequest, EXCEL_IMPORT_FAILED);
        }
    }

    private boolean readExcelFile(File file, ActionRequest actionRequest) {
        boolean isUploadSuccessfull = false;
        try {
            try (FileInputStream inputStream = new FileInputStream(file);) {
                Workbook wb = getWorkbook(inputStream, file.getPath());
                if (Validator.isNotNull(wb)) {
                    Sheet firstSheet = wb.getSheetAt(0);
                    Iterator<Row> iterator = firstSheet.iterator();
                    while (iterator.hasNext()) {
                        Row row = iterator.next();
                        if (row.getRowNum() == 0) {
                            continue;
                        }
                        iterateRowEntry(row, actionRequest);

                    }
                    isUploadSuccessfull = true;
                }
            }
            return isUploadSuccessfull;
        } catch (Exception e) {
            log.error("Exception while reading excel file " + e.getMessage());
        }
        return isUploadSuccessfull;
    }

    private static Workbook getWorkbook(FileInputStream inputStream, String excelFilePath) throws IOException {
        Workbook workbook = null;
        if (excelFilePath.endsWith("xlsx")) {
            workbook = new XSSFWorkbook(inputStream);
        } else if (excelFilePath.endsWith("xls")) {
            workbook = new HSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("The specified file is not Excel file");
        }
        return workbook;
    }

    private void iterateRowEntry(Row row, ActionRequest actionRequest) {
        try {
            ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
            Iterator<Cell> cellIterator = row.cellIterator();

            String picklistName = StringPool.BLANK;
            String keyVal = StringPool.BLANK;
            String nameVal = StringPool.BLANK;
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                if (0 == cell.getColumnIndex()) {
                    picklistName = cell.getStringCellValue();

                } else if (1 == cell.getColumnIndex()) {
                    keyVal = cell.getStringCellValue();

                } else if (2 == cell.getColumnIndex()) {
                    nameVal = cell.getStringCellValue();
                }
            }
            addOrUpdatePicklistEntry(picklistName, keyVal, nameVal, themeDisplay);

        } catch (Exception e) {
            log.error("Exception while iterating rows entry " + e);
        }
    }

    private void addOrUpdatePicklistEntry(String picklistName, String key, String name, ThemeDisplay themeDisplay) {
        try {
            if (isPicklistExist(picklistName, themeDisplay.getCompanyId()) && Validator.isNotNull(key) && Validator.isNotNull(name)) {
                long listTypeDefId = ListTypeDefinitionLocalServiceUtil.fetchListTypeDefinitionByExternalReferenceCode(picklistName, themeDisplay.getCompanyId()).getListTypeDefinitionId();
                Map<Locale, String> nameMap = new HashMap<>();
                nameMap.put(Locale.US, name);
                ListTypeEntry listTypeEntry = ListTypeEntryLocalServiceUtil.fetchListTypeEntry(listTypeDefId, key);
                if (Validator.isNull(listTypeEntry)) {
                    ListTypeEntryLocalServiceUtil.addListTypeEntry(PortalUUIDUtil.generate(), themeDisplay.getUserId(), listTypeDefId, key, nameMap);
                } else {
                    ListTypeEntryLocalServiceUtil.updateListTypeEntry(listTypeEntry.getExternalReferenceCode(), listTypeEntry.getListTypeEntryId(), nameMap);
                }
            }
        } catch (Exception e) {
            log.error("Exception while adding/updating picklist entry" + e.getMessage());
        }
    }

    private boolean isPicklistExist(String picklistName, long companyId) {
        try {
            ListTypeDefinition listTypeDefinition = ListTypeDefinitionLocalServiceUtil.getListTypeDefinitionByExternalReferenceCode(picklistName, companyId);
            if (Validator.isNotNull(listTypeDefinition)) {
                return Boolean.TRUE;
            }
        } catch (Exception e) {
            log.error("Exception while checking if picklist already exists " + e);
        }
        return Boolean.FALSE;
    }

}