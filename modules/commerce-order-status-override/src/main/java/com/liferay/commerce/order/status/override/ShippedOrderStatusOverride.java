
package com.liferay.commerce.order.status.override;

import com.liferay.commerce.constants.CommerceOrderConstants;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderItem;
import com.liferay.commerce.order.status.CommerceOrderStatus;
import com.liferay.commerce.service.CommerceOrderService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.commerce.order.status.override.constants.CommerceOrderStatusOverrideConstants;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(property = { "commerce.order.status.key=" + CommerceOrderConstants.ORDER_STATUS_SHIPPED,
		"commerce.order.status.priority:Integer=" + ShippedOrderStatusOverride.PRIORITY,
		"service.ranking:Integer=100" }, service = CommerceOrderStatus.class)
public class ShippedOrderStatusOverride implements CommerceOrderStatus {
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
		commerceOrder.setOrderStatus(CommerceOrderConstants.ORDER_STATUS_SHIPPED);

		return commerceOrderService.updateCommerceOrder(commerceOrder);
	}

	/**
	 * Get key
	 *
	 * @return key in int
	 */
	@Override
	public int getKey() {
		return CommerceOrderConstants.ORDER_STATUS_SHIPPED;
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
				CommerceOrderConstants.getOrderStatusLabel(CommerceOrderConstants.ORDER_STATUS_SHIPPED));
	}

	/**
	 * verify the order is in complete state or not
	 *
	 * @param commerceOrder
	 * @return true or false
	 */
	@Override
	public boolean isComplete(CommerceOrder commerceOrder) {
		boolean isCompleteStatus = Boolean.FALSE;

		if ((commerceOrder.getOrderStatus() == CommerceOrderConstants.ORDER_STATUS_SHIPPED)
				|| (commerceOrder.getOrderStatus() == CommerceOrderConstants.ORDER_STATUS_COMPLETED)) 
		{
			isCompleteStatus = Boolean.TRUE;
		}
		
		return isCompleteStatus;
	}

	/**
	 * Check transition criteria met or not based on next status will be enable
	 *
	 * @param commerceOrder
	 * @return true or false
	 */
	@Override
	public boolean isTransitionCriteriaMet(CommerceOrder commerceOrder) throws PortalException {

		boolean allOrderItemsShipped = true;

		if ((commerceOrder.getOrderStatus() == CommerceOrderStatusOverrideConstants.ORDER_STATUS_CONFIRMED))
			return allOrderItemsShipped;

		for (CommerceOrderItem shippedCommerceOrderItem : commerceOrder.getCommerceOrderItems()) {

			if ((shippedCommerceOrderItem.getShippedQuantity().compareTo(shippedCommerceOrderItem.getQuantity()) < 0)
					&& shippedCommerceOrderItem.isShippable()) {
				allOrderItemsShipped = Boolean.FALSE;
			}

		}

		if (((commerceOrder.getOrderStatus() == CommerceOrderConstants.ORDER_STATUS_CANCELLED) || (commerceOrder
				.getOrderStatus() == CommerceOrderStatusOverrideConstants.ORDER_STATUS_PENDING_CANCELLATION))
				&& allOrderItemsShipped) {
			allOrderItemsShipped = Boolean.FALSE;
		}

		if (commerceOrder.getOrderStatus() == CommerceOrderConstants.ORDER_STATUS_PROCESSING) {
			allOrderItemsShipped = Boolean.FALSE;
		}
		return allOrderItemsShipped;
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
						.getOrderStatus() == CommerceOrderStatusOverrideConstants.ORDER_STATUS_PENDING_CANCELLATION
				|| commerceOrder.getOrderStatus() == CommerceOrderConstants.ORDER_STATUS_PARTIALLY_SHIPPED)
			isValid = Boolean.FALSE;

		return isValid;

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

	@Reference
	private CommerceOrderService commerceOrderService;
}
