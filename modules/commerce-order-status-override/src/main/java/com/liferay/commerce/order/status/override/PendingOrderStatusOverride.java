package com.liferay.commerce.order.status.override;

import com.liferay.commerce.constants.CommerceOrderConstants;
import com.liferay.commerce.constants.CommerceOrderPaymentConstants;
import com.liferay.commerce.constants.CommercePaymentMethodConstants;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.order.CommerceOrderValidatorRegistry;
import com.liferay.commerce.order.status.CommerceOrderStatus;
import com.liferay.commerce.payment.method.CommercePaymentMethod;
import com.liferay.commerce.payment.method.CommercePaymentMethodRegistry;
import com.liferay.commerce.service.CommerceOrderLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.WorkflowDefinitionLink;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.WorkflowDefinitionLinkLocalService;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowHandlerRegistryUtil;

import java.util.HashMap;
import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Payal
 */
@Component(property = { "commerce.order.status.key=" + CommerceOrderConstants.ORDER_STATUS_PENDING,
		"commerce.order.status.priority:Integer=" + PendingOrderStatusOverride.PRIORITY,
		"service.ranking:Integer=100" }, service = CommerceOrderStatus.class)
public class PendingOrderStatusOverride implements CommerceOrderStatus {

	public static final int KEY = CommerceOrderConstants.ORDER_STATUS_PENDING;

	public static final int PRIORITY = 30;

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

		commerceOrder = commerceOrderLocalService.updateCommerceOrder(commerceOrder);

		if (isWorkflowEnabled(commerceOrder)) {

			// Commerce order

			commerceOrder.setStatus(WorkflowConstants.STATUS_PENDING);

			// Workflow

			ServiceContext serviceContext = new ServiceContext();

			serviceContext.setScopeGroupId(commerceOrder.getGroupId());
			serviceContext.setUserId(userId);
			serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);

			commerceOrder = WorkflowHandlerRegistryUtil.startWorkflowInstance(commerceOrder.getCompanyId(),
					commerceOrder.getScopeGroupId(), userId, CommerceOrder.class.getName(),
					commerceOrder.getCommerceOrderId(), commerceOrder, serviceContext, new HashMap<>());
		}

		return commerceOrderLocalService.updateCommerceOrder(commerceOrder);
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
		return language.get(locale, CommerceOrderConstants.getOrderStatusLabel(KEY));
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
		CommercePaymentMethod commercePaymentMethod = commercePaymentMethodRegistry
				.getCommercePaymentMethod(commerceOrder.getCommercePaymentMethodKey());

		if (commercePaymentMethod == null) {
			isTransitionMet = Boolean.TRUE;
		}

		if (Validator.isNotNull(commercePaymentMethod) && Validator.isNotNull(commerceOrder)
			&& (commerceOrder.getPaymentStatus() == CommerceOrderPaymentConstants.STATUS_COMPLETED)
				|| (commercePaymentMethod.getPaymentType() == CommercePaymentMethodConstants.TYPE_OFFLINE)) {
				isTransitionMet = commerceOrderValidatorRegistry.isValid(LocaleUtil.getSiteDefault(), commerceOrder);
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
	private CommerceOrderLocalService commerceOrderLocalService;

	@Reference
	private CommerceOrderValidatorRegistry commerceOrderValidatorRegistry;

	@Reference
	private CommercePaymentMethodRegistry commercePaymentMethodRegistry;

	@Reference
	private Language language;

	@Reference
	private WorkflowDefinitionLinkLocalService workflowDefinitionLinkLocalService;
}