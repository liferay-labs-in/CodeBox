package com.liferay.commerce.order.status.override;

import com.liferay.commerce.constants.CommerceOrderConstants;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.order.status.CommerceOrderStatus;
import com.liferay.commerce.service.CommerceOrderService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.commerce.order.status.override.constants.CommerceOrderStatusOverrideConstants;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Payal
 */
@Component(property = { "commerce.order.status.key=" + PartiallyShippedOrderStatusOverride.KEY,
		"commerce.order.status.priority:Integer=" + PartiallyShippedOrderStatusOverride.PRIORITY,
		"service.ranking:Integer=100" }, service = CommerceOrderStatus.class)
public class PartiallyShippedOrderStatusOverride implements CommerceOrderStatus {

	public static final int KEY = CommerceOrderConstants.ORDER_STATUS_PARTIALLY_SHIPPED;

	public static final int PRIORITY = 60;

	/**
	 * Called on transition change for order status
	 *
	 * @param commerceOrder
	 * @param userId
	 * @return commerceOrder
	 */
	@Override
	public CommerceOrder doTransition(CommerceOrder commerceOrder, long userId, boolean secure) throws PortalException {

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
	 * Check the status is valid for current status
	 *
	 * @param commerceOrder
	 * @return true or false
	 */
	@Override
	public boolean isValidForOrder(CommerceOrder commerceOrder) throws PortalException {
		boolean isValid = Boolean.TRUE;
		
		if (commerceOrder.getOrderStatus() == CommerceOrderConstants.ORDER_STATUS_CANCELLED
				|| commerceOrder
						.getOrderStatus() == CommerceOrderStatusOverrideConstants.ORDER_STATUS_PENDING_CANCELLATION)
			isValid = Boolean.FALSE;

		return isValid;

	}
	
	/**
	 * Check transition criteria met or not based on next status will be enable
	 *
	 * @param commerceOrder
	 * @return true or false
	 */
	@Override
	public boolean isTransitionCriteriaMet(CommerceOrder commerceOrder) throws PortalException {
		boolean isTransitionMet = Boolean.FALSE;
		
		if ((commerceOrder.getOrderStatus() == CommerceOrderStatusOverrideConstants.ORDER_STATUS_CONFIRMED)) {
			isTransitionMet = Boolean.TRUE;
		}
		
		return isTransitionMet;

	}

	@Reference
	private CommerceOrderService commerceOrderService;

}