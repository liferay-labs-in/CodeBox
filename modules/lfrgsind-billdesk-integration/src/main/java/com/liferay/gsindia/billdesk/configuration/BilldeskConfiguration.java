package com.liferay.gsindia.billdesk.configuration;

import com.liferay.gsindia.billdesk.constants.LfrGsIndBilldeskIntegrationPortletKeys;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import aQute.bnd.annotation.metatype.Meta;

@ExtendedObjectClassDefinition(category = "LiferayGS", scope = ExtendedObjectClassDefinition.Scope.COMPANY)
@Meta.OCD(id=LfrGsIndBilldeskIntegrationPortletKeys.BILLDESKCONFIGURATION, name = "BillDesk Configuration")

public interface BilldeskConfiguration {

	@Meta.AD(required = false, description = "ClientID")
	public String clientId();
	
	@Meta.AD(required = false, description = "SecretKey")
	public String secretKey();
	
	@Meta.AD(required = false, description = "Webhook URL/CallBack URL")
	public String webHookURL();
	
	@Meta.AD(required = false, description = "Merchant Id")
	public String merchantId();
	
	@Meta.AD(required = false, description = "AccountNumber")
	public String accountNumber();
	
	@Meta.AD(required = false, description = "Create APIURL")
	public String createAPI();
	
	@Meta.AD(required = false, description = "Status APIURL")
	public String statusAPI();
	
	@Meta.AD(required = false, description = "Item Code")
	public String itemcode();
	
	@Meta.AD(required = false, description = "Currency")
	public String currency();
}
