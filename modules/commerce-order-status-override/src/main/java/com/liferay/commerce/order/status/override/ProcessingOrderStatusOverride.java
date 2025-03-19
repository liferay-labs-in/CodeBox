package com.liferay.commerce.order.status.override;

import com.liferay.commerce.constants.CommerceOrderConstants;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.order.status.CommerceOrderStatus;
import com.liferay.commerce.service.CommerceOrderService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(property = { "commerce.order.status.key=" + CommerceOrderConstants.ORDER_STATUS_PROCESSING,
		"commerce.order.status.priority:Integer=" + ProcessingOrderStatusOverride.PRIORITY,
		"service.ranking:Integer=100" }, service = CommerceOrderStatus.class)

public class ProcessingOrderStatusOverride implements CommerceOrderStatus {

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

		commerceOrder.setOrderStatus(CommerceOrderConstants.ORDER_STATUS_PROCESSING);

		return commerceOrderService.updateCommerceOrder(commerceOrder);
	}

	/**
	 * Get key
	 *
	 * @return key in int
	 */
	@Override
	public int getKey() {
		return CommerceOrderConstants.ORDER_STATUS_PROCESSING;
	}

	/**
	 * Get Label
	 *
	 * @param locale
	 * @return order status label
	 */
	@Override
	public String getLabel(Locale locale) {
		return LanguageUtil.get(locale,
				CommerceOrderConstants.getOrderStatusLabel(CommerceOrderConstants.ORDER_STATUS_PROCESSING));
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
				&& (commerceOrder.getOrderStatus() != CommerceOrderConstants.ORDER_STATUS_PENDING)) {
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

		if ((commerceOrder.getOrderStatus() == CommerceOrderConstants.ORDER_STATUS_PENDING)
				|| (commerceOrder.getOrderStatus() == CommerceOrderConstants.ORDER_STATUS_ON_HOLD)) {
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
		return Boolean.FALSE;

	}

	@Reference
	private CommerceOrderService commerceOrderService;

}