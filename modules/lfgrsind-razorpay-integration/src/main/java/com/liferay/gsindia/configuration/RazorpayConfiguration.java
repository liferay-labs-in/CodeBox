package com.liferay.gsindia.configuration;

import com.liferay.gsindia.constants.LfgrsindRazorpayIntegrationPortletKeys;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import aQute.bnd.annotation.metatype.Meta;

/**
 * @author prakash
 */

@ExtendedObjectClassDefinition(category = "liferay-gs", scope = ExtendedObjectClassDefinition.Scope.SYSTEM)
@Meta.OCD(id = LfgrsindRazorpayIntegrationPortletKeys.LFGRSINDRAZORPAYCONFIGURATION, name = "Razorpay Configuration")

public interface RazorpayConfiguration {
	@Meta.AD(description = "Enter the Razorpay Secret key(Required)", name = "secret-key", required = false)
	public String secretKey();	
	
	@Meta.AD(description = "Enter the Razorpay Key Id(Required)", name = "key-id", required = false)
	public String keyId();
}
