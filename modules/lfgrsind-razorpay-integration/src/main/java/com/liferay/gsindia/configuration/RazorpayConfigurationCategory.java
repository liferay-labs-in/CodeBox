package com.liferay.gsindia.configuration;

import com.liferay.configuration.admin.category.ConfigurationCategory;

import org.osgi.service.component.annotations.Component;

/**
 * @author prakash
 *
 */
@Component(service = ConfigurationCategory.class)
public class RazorpayConfigurationCategory implements ConfigurationCategory {

	@Override
	public String getCategoryIcon() {
		return "chip";
	}

	@Override
	public String getCategoryKey() {
		return _KEY;
	}

	@Override
	public String getCategorySection() {
		return _CATEGORY_SET_KEY;
	}

	private static final String _CATEGORY_SET_KEY = "liferay-gs";

	private static final String _KEY = "liferay-gs";

}
