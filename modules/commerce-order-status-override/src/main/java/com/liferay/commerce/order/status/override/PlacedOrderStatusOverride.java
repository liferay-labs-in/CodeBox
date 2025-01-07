
package com.liferay.commerce.order.status.override;

import com.liferay.commerce.constants.CommerceOrderConstants;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.order.CommerceOrderValidatorRegistry;
import com.liferay.commerce.order.status.CommerceOrderStatus;
import com.liferay.commerce.payment.method.CommercePaymentMethodRegistry;
import com.liferay.commerce.service.CommerceOrderService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.WorkflowDefinitionLink;
import com.liferay.portal.kernel.service.WorkflowDefinitionLinkLocalService;
import com.liferay.commerce.order.status.override.constants.CommerceOrderStatusOverrideConstants;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Payal T
 */
@Component(property = { "commerce.order.status.key=" + CommerceOrderStatusOverrideConstants.ORDER_STATUS_PLACED,
		"commerce.order.status.priority:Integer=" + PlacedOrderStatusOverride.PRIORITY,
		"service.ranking:Integer=100" }, service = CommerceOrderStatus.class)
public class PlacedOrderStatusOverride implements CommerceOrderStatus {

	public static final int KEY = CommerceOrderStatusOverrideConstants.ORDER_STATUS_PLACED;

	public static final int PRIORITY = 31;

	/**
	 * Called on transition change for order status
	 *
	 * @param commerceOrder
	 * @param userId
	 * @return commerceOrder
	 */
	@Override
	public CommerceOrder doTransition(CommerceOrder commerceOrder, long userId, boolean secure) throws PortalException {

		commerceOrder.setOrderStatus(CommerceOrderStatusOverrideConstants.ORDER_STATUS_PLACED);

		return commerceOrderService.updateCommerceOrder(commerceOrder);
	}

	/**
	 * Get key
	 *
	 * @return key in int
	 */
	@Override
	public int getKey() {
		return CommerceOrderStatusOverrideConstants.ORDER_STATUS_PLACED;
	}

	/**
	 * Get Label
	 *
	 * @param locale
	 * @return order status label
	 */
	@Override
	public String getLabel(Locale locale) {
		return CommerceOrderStatusOverrideConstants.ORDER_STATUS_PLACED_LABEL;
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
		if (!commerceOrder.isOpen() && commerceOrder.isApproved()) {
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

		if (commerceOrder.getOrderStatus() == CommerceOrderConstants.ORDER_STATUS_PENDING) {
			isTransitionMet = Boolean.TRUE;
		}

		return isTransitionMet;

	}

	/**
	 * Check the workflow is enabled or not
	 *
	 * @param commerceOrder
	 * @return true or false
	 */
	@Override
	public boolean isWorkflowEnabled(CommerceOrder commerceOrder) throws PortalException {
		boolean isWorkflowEnabled = Boolean.FALSE;

		WorkflowDefinitionLink workflowDefinitionLink = workflowDefinitionLinkLocalService.fetchWorkflowDefinitionLink(
				commerceOrder.getCompanyId(), commerceOrder.getGroupId(), CommerceOrder.class.getName(), 0,
				CommerceOrderConstants.TYPE_PK_FULFILLMENT, true);

		if (workflowDefinitionLink != null) {
			isWorkflowEnabled = Boolean.TRUE;
		}

		return isWorkflowEnabled;

	}

	@Reference
	private CommerceOrderService commerceOrderService;

	@Reference
	private CommercePaymentMethodRegistry commercePaymentMethodRegistry;

	@Reference
	private WorkflowDefinitionLinkLocalService workflowDefinitionLinkLocalService;

	@Reference
	private CommerceOrderValidatorRegistry commerceOrderValidatorRegistry;

}
