package com.liferay.gsindia.billdesk.configuration;

import com.liferay.portal.kernel.settings.definition.ConfigurationBeanDeclaration;
import org.osgi.service.component.annotations.Component;

@Component
public class LfrGsIndBilldeskConfigurationAction implements ConfigurationBeanDeclaration {

	@Override
	public Class<?> getConfigurationBeanClass() {
		// TODO Auto-generated method stub
		return BilldeskConfiguration.class;
	}

}
