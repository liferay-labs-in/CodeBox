package com.liferay.asserts.objects.bulk.upload.web.util;

import com.liferay.list.type.model.ListTypeEntry;
import com.liferay.list.type.service.ListTypeEntryLocalServiceUtil;

import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.constants.ObjectFieldValidationConstants;
import com.liferay.object.exception.ObjectEntryValuesException;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectFieldSetting;
import com.liferay.object.service.ObjectFieldSettingLocalServiceUtil;
import com.liferay.petra.string.StringPool;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.List;

/**
 * @author Akhash R
 */
public class ObjectFieldValidatorUtil {

    /**
     * Validates the entry value against the provided objectField.
     *
     * @param entry       The entry value to validate.
     * @param objectField The field information to validate against.
     * @throws PortalException If validation fails due to missing required fields,
     *                         incorrect data types, or size violations.
     */
    public static void _validateValues(String entry, ObjectField objectField) throws PortalException {
        if (objectField == null) {
            return; // No validation if the field is null
        }

        // Check if the field is required and the entry is either null or empty
        if ((Validator.isNull(entry) || entry.isEmpty()) && objectField.isRequired()) {
            throw new ObjectEntryValuesException.Required(objectField.getName());
        }

        String entryValueString = GetterUtil.getString(entry);

        // Validate based on the business type of the object field
        switch (objectField.getBusinessType()) {
            case ObjectFieldConstants.BUSINESS_TYPE_BOOLEAN:
                _validateBoolean(entryValueString, objectField);
                break;

            case ObjectFieldConstants.BUSINESS_TYPE_ENCRYPTED:
                _validateTextMaxLength280(objectField, entryValueString);
                break;

            case ObjectFieldConstants.BUSINESS_TYPE_LONG_TEXT:
                _validateTextMaxLength(65000, entryValueString, objectField.getObjectFieldId(), objectField.getName());
                break;

            case ObjectFieldConstants.BUSINESS_TYPE_MULTISELECT_PICKLIST:
                _validateMultiSelectPickList(entryValueString, objectField);
                break;

            case ObjectFieldConstants.BUSINESS_TYPE_PICKLIST:
                _validateListTypeEntryKey(entryValueString, objectField);
                break;

            default:
                // Validate based on DB type if no business type is applicable
                switch (objectField.getDBType()) {
                    case ObjectFieldConstants.DB_TYPE_INTEGER:
                        _validateInteger(entryValueString, objectField);
                        break;

                    case ObjectFieldConstants.DB_TYPE_LONG:
                        _validateLong(entryValueString, objectField);
                        break;

                    case ObjectFieldConstants.DB_TYPE_STRING:
                        _validateTextMaxLength280(objectField, GetterUtil.getString(entry));
                        break;
                }
                break;
        }
    }

    /**
     * Validates a boolean entry.
     *
     * @param entryValueString The string value of the entry to validate.
     * @param objectField      The field metadata.
     * @throws PortalException If the field is required and the boolean value is false.
     */
    public static void _validateBoolean(String entryValueString, ObjectField objectField) throws PortalException {
        if (!GetterUtil.getBoolean(entryValueString) && objectField.isRequired()) {
            throw new ObjectEntryValuesException.Required(objectField.getName());
        }
    }

    /**
     * Validates an integer entry.
     *
     * @param entryValueString The string value of the integer to validate.
     * @param objectField      The field metadata.
     * @throws PortalException If the integer is invalid or exceeds the allowed size.
     */
    public static void _validateInteger(String entryValueString, ObjectField objectField) throws PortalException {
        if (!entryValueString.isEmpty()) {
            int value = GetterUtil.getInteger(entryValueString);
            if (!StringUtil.equals(String.valueOf(value), entryValueString)) {
                throw new ObjectEntryValuesException.ExceedsIntegerSize(9, objectField.getName());
            }
        } else if (objectField.isRequired()) {
            throw new ObjectEntryValuesException.Required(objectField.getName());
        }
    }

    /**
     * Validates a long entry.
     *
     * @param entryValueString The string value of the long to validate.
     * @param objectField      The field metadata.
     * @throws PortalException If the long is invalid, exceeds the allowed size, or is outside the valid range.
     */
    public static void _validateLong(String entryValueString, ObjectField objectField) throws PortalException {
        if (!entryValueString.isEmpty()) {
            long value = GetterUtil.getLong(entryValueString);
            if (!StringUtil.equals(String.valueOf(value), entryValueString)) {
                throw new ObjectEntryValuesException.ExceedsLongSize(16, objectField.getName());
            } else if (value > ObjectFieldValidationConstants.BUSINESS_TYPE_LONG_VALUE_MAX) {
                throw new ObjectEntryValuesException.ExceedsLongMaxSize(ObjectFieldValidationConstants.BUSINESS_TYPE_LONG_VALUE_MAX, objectField.getName());
            } else if (value < ObjectFieldValidationConstants.BUSINESS_TYPE_LONG_VALUE_MIN) {
                throw new ObjectEntryValuesException.ExceedsLongMinSize(ObjectFieldValidationConstants.BUSINESS_TYPE_LONG_VALUE_MIN, objectField.getName());
            }
        } else if (objectField.isRequired()) {
            throw new ObjectEntryValuesException.Required(objectField.getName());
        }
    }

    /**
     * Validates a multi-select picklist entry.
     *
     * @param entryValueString The string containing multiple picklist values separated by commas.
     * @param objectField      The field metadata.
     * @throws PortalException If any picklist value is invalid.
     */
    public static void _validateMultiSelectPickList(String entryValueString, ObjectField objectField) throws PortalException {
        List<String> listTypeEntryKeys = ListUtil.fromString(entryValueString, StringPool.COMMA_AND_SPACE);
        for (String listTypeEntryKey : listTypeEntryKeys) {
            _validateListTypeEntryKey(listTypeEntryKey, objectField);
        }
    }

    /**
     * Validates the length of a text entry with a maximum of 280 characters.
     *
     * @param objectField The field metadata.
     * @param value       The text value to validate.
     * @throws PortalException If the text exceeds the maximum length of 280 characters.
     */
    public static void _validateTextMaxLength280(ObjectField objectField, String value) throws PortalException {
        _validateTextMaxLength(280, value, objectField.getObjectFieldId(), objectField.getName());
    }

    /**
     * Validates the length of a text entry.
     *
     * @param defaultMaxLength  The default maximum length.
     * @param objectEntryValue  The text value to validate.
     * @param objectFieldId     The object field ID.
     * @param objectFieldName   The field name for error messages.
     * @throws PortalException  If the text exceeds the defined maximum length.
     */
    public static void _validateTextMaxLength(int defaultMaxLength, String objectEntryValue, long objectFieldId, String objectFieldName) throws PortalException {
        int maxLength = defaultMaxLength;

        // Fetch the maximum length setting for the field
        ObjectFieldSetting objectFieldSetting = ObjectFieldSettingLocalServiceUtil.fetchObjectFieldSetting(objectFieldId, "maxLength");
        if (objectFieldSetting != null) {
            maxLength = GetterUtil.getInteger(objectFieldSetting.getValue());
        }

        // Validate the length of the entry
        if (objectEntryValue.length() > maxLength) {
            throw new ObjectEntryValuesException.ExceedsTextMaxLength(maxLength, objectFieldName);
        }
    }

    /**
     * Validates a picklist entry key.
     *
     * @param listTypeEntryKey The picklist entry key to validate.
     * @param objectField      The field metadata.
     * @throws PortalException If the picklist entry is invalid.
     */
    public static void _validateListTypeEntryKey(String listTypeEntryKey, ObjectField objectField) throws PortalException {
        try {
            ListTypeEntry listTypeEntry = ListTypeEntryLocalServiceUtil.getListTypeEntry(objectField.getListTypeDefinitionId(), listTypeEntryKey);
            if (listTypeEntry == null && (Validator.isNotNull(listTypeEntryKey) || objectField.isRequired())) {
                throw new ObjectEntryValuesException.ListTypeEntry(objectField.getName());
            }
        } catch (PortalException e) {
            throw new ObjectEntryValuesException.ListTypeEntry(objectField.getName());
        }
    }

    private ObjectFieldValidatorUtil() {
        throw new IllegalStateException("Utility class");
    }

}
