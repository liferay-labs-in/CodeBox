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
@Component(property = {
		"commerce.order.status.key=" + CommerceOrderStatusOverrideConstants.ORDER_STATUS_AWAITING_PAYMENT,
		"commerce.order.status.priority:Integer=" + AwaitingPaymentOrderStatusOverride.PRIORITY,
		"service.ranking:Integer=100" }, service = CommerceOrderStatus.class)

public class AwaitingPaymentOrderStatusOverride implements CommerceOrderStatus {

	public static final int PRIORITY = 50;

	/**
	 * Called on transition change for order status
	 *
	 * @param commerceOrder
	 * @param userId
	 * @return commerceOrder
	 */
	@Override
	public CommerceOrder doTransition(CommerceOrder commerceOrder, long userId, boolean secure) throws PortalException {

		commerceOrder.setOrderStatus(CommerceOrderStatusOverrideConstants.ORDER_STATUS_AWAITING_PAYMENT);
		return commerceOrderService.updateCommerceOrder(commerceOrder);
	}


	/**
	 * Get key
	 *
	 * @return key in int
	 */
	@Override
	public int getKey() {
		return CommerceOrderStatusOverrideConstants.ORDER_STATUS_AWAITING_PAYMENT;
	}

	/**
	 * Get Label
	 *
	 * @param locale
	 * @return order status label
	 */
	@Override
	public String getLabel(Locale locale) {
		return LanguageUtil.get(locale, CommerceOrderStatusOverrideConstants.ORDER_STATUS_AWAITING_PAYMENT_LABEL);
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
		
		if (!commerceOrder.isOpen() && commerceOrder.isApproved()
				&& (commerceOrder.getOrderStatus() != CommerceOrderConstants.ORDER_STATUS_PROCESSING)) {
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

		if (commerceOrder.getOrderStatus() == CommerceOrderStatusOverrideConstants.ORDER_STATUS_PLACED) {
			isTransitionMet = Boolean.TRUE;
		}
		
		return isTransitionMet;

	}


	@Reference
	private CommerceOrderService commerceOrderService;
}