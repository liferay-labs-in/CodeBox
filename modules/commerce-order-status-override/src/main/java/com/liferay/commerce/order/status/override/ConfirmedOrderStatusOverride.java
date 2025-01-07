package com.liferay.commerce.order.status.override;

import com.liferay.commerce.constants.CommerceOrderConstants;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.order.status.CommerceOrderStatus;
import com.liferay.commerce.service.CommerceOrderService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.commerce.order.status.override.constants.CommerceOrderStatusOverrideConstants;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Payal
 */
@Component(property = { "commerce.order.status.key=" + CommerceOrderStatusOverrideConstants.ORDER_STATUS_CONFIRMED,
		"commerce.order.status.priority:Integer=" + ConfirmedOrderStatusOverride.PRIORITY,
		"service.ranking:Integer=100" }, service = CommerceOrderStatus.class)

public class ConfirmedOrderStatusOverride implements CommerceOrderStatus {

	public static final int KEY = CommerceOrderStatusOverrideConstants.ORDER_STATUS_CONFIRMED;

	public static final int PRIORITY = 55;

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
		return CommerceOrderStatusOverrideConstants.ORDER_STATUS_CONFIRMED_LABEL;
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
	 * verify the order is in complete state or not
	 *
	 * @param commerceOrder
	 * @return true or false
	 */
	@Override
	public boolean isComplete(CommerceOrder commerceOrder) {
		boolean isComplete = Boolean.FALSE;
		if (commerceOrder.getOrderStatus() == CommerceOrderStatusOverrideConstants.ORDER_STATUS_CONFIRMED ||
				commerceOrder.getOrderStatus() == CommerceOrderConstants.ORDER_STATUS_PARTIALLY_SHIPPED ||
				commerceOrder.getOrderStatus() == CommerceOrderConstants.ORDER_STATUS_SHIPPED ||
				commerceOrder.getOrderStatus() == CommerceOrderConstants.ORDER_STATUS_COMPLETED
		) {
			isComplete = Boolean.TRUE;
		}
		return isComplete;

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

		if (commerceOrder.getOrderStatus() == CommerceOrderStatusOverrideConstants.ORDER_STATUS_PAID ||
		commerceOrder.getOrderStatus() == CommerceOrderStatusOverrideConstants.ORDER_STATUS_AWAITING_PAYMENT) {
			isTransitionMet = Boolean.TRUE;
		}
		return isTransitionMet;

	}

	@Override
	public boolean isValidForOrder(CommerceOrder commerceOrder) throws PortalException {
		boolean isValid = Boolean.FALSE;
		if (commerceOrder.getExpandoBridge().getAttribute("cancellationWithErp").equals(StringPool.BLANK)){
			isValid = Boolean.TRUE;
		}
		return isValid;
	}

	@Reference
	private CommerceOrderService commerceOrderService;
}