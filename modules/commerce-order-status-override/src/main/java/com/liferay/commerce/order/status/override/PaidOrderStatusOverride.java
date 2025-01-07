package com.liferay.commerce.order.status.override;


import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.order.status.CommerceOrderStatus;
import com.liferay.commerce.service.CommerceOrderService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.commerce.order.status.override.constants.CommerceOrderStatusOverrideConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Locale;

/**
 * @author Shrish
 */

@Component(property = { "commerce.order.status.key=" + CommerceOrderStatusOverrideConstants.ORDER_STATUS_PAID,
        "commerce.order.status.priority:Integer=" + PaidOrderStatusOverride.PRIORITY,
        "service.ranking:Integer=100" }, service = CommerceOrderStatus.class)
public class PaidOrderStatusOverride implements CommerceOrderStatus{

    public static final int KEY = CommerceOrderStatusOverrideConstants.ORDER_STATUS_PAID;

    public static final int PRIORITY = 51;

    private static final Log log = LogFactoryUtil.getLog(PaidOrderStatusOverride.class);

    @Override
    public CommerceOrder doTransition(CommerceOrder commerceOrder, long userId, boolean secure) throws PortalException {

        commerceOrder.setOrderStatus(CommerceOrderStatusOverrideConstants.ORDER_STATUS_PAID);

        return commerceOrderService.updateCommerceOrder(commerceOrder);
    }

    /**
     * Get key
     *
     * @return key in int
     */
    @Override
    public int getKey() {
        return CommerceOrderStatusOverrideConstants.ORDER_STATUS_PAID;
    }

    /**
     * Get Label
     *
     * @param locale
     * @return order status label
     */
    @Override
    public String getLabel(Locale locale) {
        return CommerceOrderStatusOverrideConstants.ORDER_STATUS_PAID_LABEL;
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

        if (commerceOrder.getOrderStatus() == CommerceOrderStatusOverrideConstants.ORDER_STATUS_AWAITING_PAYMENT) {
            isTransitionMet = Boolean.TRUE;
        }

        return isTransitionMet;

    }

    @Override
    public boolean isComplete(CommerceOrder commerceOrder) throws ConfigurationException {
        boolean isComplete = Boolean.FALSE;

        if (!commerceOrder.isOpen() && commerceOrder.isApproved()
                && (commerceOrder.getOrderStatus() != CommerceOrderStatusOverrideConstants.ORDER_STATUS_AWAITING_PAYMENT ||
                    commerceOrder.getOrderStatus() != CommerceOrderStatusOverrideConstants.ORDER_STATUS_PLACED
        )) {
            isComplete = Boolean.TRUE;
        }
        return isComplete;
    }

    @Override
    public boolean isValidForOrder(CommerceOrder commerceOrder) throws PortalException {
        boolean isValid = Boolean.FALSE;


        if ((commerceOrder.getAccountEntry().getExpandoBridge().getAttribute("Cashcustomer").equals(true) ||
                (commerceOrder.getAccountEntry().getExpandoBridge().getAttribute("Cashcustomer").equals(false))
                &&
                (commerceOrder.getExpandoBridge().getAttribute("cancellationWithErp").equals(StringPool.BLANK))
                &&
                (!commerceOrder.getExpandoBridge().getAttribute("PaymentOption").equals("onaccount")))){
            isValid = Boolean.TRUE;
        }
        return isValid;
    }


    @Reference
    private CommerceOrderService commerceOrderService;

}
