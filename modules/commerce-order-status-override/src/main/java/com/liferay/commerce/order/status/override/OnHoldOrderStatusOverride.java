package com.liferay.commerce.order.status.override;

import com.liferay.commerce.constants.CommerceOrderConstants;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.order.engine.CommerceOrderEngine;
import com.liferay.commerce.order.status.CommerceOrderStatus;
import com.liferay.commerce.order.status.CommerceOrderStatusRegistry;
import com.liferay.commerce.service.CommerceOrderService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(property = { "commerce.order.status.key=" + OnHoldOrderStatusOverride.KEY,
		"commerce.order.status.priority:Integer=" + OnHoldOrderStatusOverride.PRIORITY, "service.ranking:Integer=100"

}, service = CommerceOrderStatus.class)
public class OnHoldOrderStatusOverride implements CommerceOrderStatus {

	public static final int KEY = CommerceOrderConstants.ORDER_STATUS_ON_HOLD;

	public static final int PRIORITY = -1;

	/**
	 * Called on transition change for order status
	 *
	 * @param commerceOrder
	 * @param userId
	 * @return commerceOrder
	 */
	@Override
	public CommerceOrder doTransition(CommerceOrder commerceOrder, long userId, boolean secure) throws PortalException {

		if (commerceOrder.getOrderStatus() == KEY) {
			return commerceOrderEngine.transitionCommerceOrder(commerceOrder,
					CommerceOrderConstants.ORDER_STATUS_PROCESSING, userId, false);
		}

		commerceOrder.setOrderStatus(KEY);

		return commerceOrderService.updateCommerceOrder(commerceOrder);
	}

	/**
	 * Get key
	 *
	 * @return key in int
	 */
	@Override
	public int getKey() {
		return KEY;
	}

	/**
	 * Get Label
	 *
	 * @param locale
	 * @return order status label
	 */
	@Override
	public String getLabel(Locale locale) {
		return LanguageUtil.get(locale, CommerceOrderConstants.getOrderStatusLabel(KEY));
	}

	/**
	 * Get priority
	 *
	 * @return priority in int
	 */
	@Override
	public int getPriority() {
		return PRIORITY;
	}

	/**
	 * Check transition criteria met or not based on next status will be enable
	 *
	 * @param commerceOrder
	 * @return true or false
	 */
	@Override
	public boolean isTransitionCriteriaMet(CommerceOrder commerceOrder) throws PortalException {
		return false;
	}

	/**
	 * Check the status is valid for current status
	 *
	 * @param commerceOrder
	 * @return true or false
	 */
	@Override
	public boolean isValidForOrder(CommerceOrder commerceOrder) throws PortalException {
		return false;
	}

	@Reference
	private CommerceOrderEngine commerceOrderEngine;

	@Reference
	private CommerceOrderService commerceOrderService;

	@Reference
	private CommerceOrderStatusRegistry commerceOrderStatusRegistry;

}