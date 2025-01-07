package com.liferay.commerce.order.status.override;

import com.liferay.commerce.constants.CommerceOrderConstants;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.order.status.CommerceOrderStatus;
import com.liferay.commerce.service.CommerceOrderService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.commerce.order.status.override.constants.CommerceOrderStatusOverrideConstants;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Payal Thummar
 */
@Component(property = { "commerce.order.status.key=" + PendingCancellationOrderStatusOverride.KEY,
		"commerce.order.status.priority:Integer=" + PendingCancellationOrderStatusOverride.PRIORITY,
		"service.ranking:Integer=100" }, service = CommerceOrderStatus.class)
public class PendingCancellationOrderStatusOverride implements CommerceOrderStatus {

	public static final int KEY = CommerceOrderStatusOverrideConstants.ORDER_STATUS_PENDING_CANCELLATION;

	public static final int PRIORITY = 53;

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
		return CommerceOrderStatusOverrideConstants.ORDER_STATUS_PENDING_CANCELLATION_LABEL;
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
		boolean isTransitionMet = Boolean.FALSE;

		if ((commerceOrder.getOrderStatus() == CommerceOrderStatusOverrideConstants.ORDER_STATUS_AWAITING_PAYMENT)) {
			isTransitionMet = Boolean.TRUE;
		}
		return isTransitionMet;
	}

	/**
	 * Check the status is valid for current status
	 *
	 * @param commerceOrder
	 * @return true or false
	 */
	@Override
	public boolean isValidForOrder(CommerceOrder commerceOrder) throws PortalException {
		boolean isValid = Boolean.FALSE;
		if ((commerceOrder.getOrderStatus() == CommerceOrderConstants.ORDER_STATUS_CANCELLED
				|| commerceOrder.getOrderStatus() == CommerceOrderStatusOverrideConstants.ORDER_STATUS_PENDING_CANCELLATION)
			&&
				(!commerceOrder.getExpandoBridge().getAttribute("cancellationWithErp").equals("erp"))) {
			isValid = Boolean.TRUE;
		}
		return isValid;

	}

	@Reference
	private CommerceOrderService commerceOrderService;

}