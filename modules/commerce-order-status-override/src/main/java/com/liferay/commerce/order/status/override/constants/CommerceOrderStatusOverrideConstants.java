package com.liferay.commerce.order.status.override.constants;

import com.liferay.commerce.constants.CommerceOrderConstants;
import com.liferay.petra.string.StringPool;

public class CommerceOrderStatusOverrideConstants {

	private CommerceOrderStatusOverrideConstants() {
	}

	public static final int ORDER_STATUS_PLACED = 31;
	public static final int ORDER_STATUS_AWAITING_PAYMENT = 32;
	public static final int ORDER_STATUS_CONFIRMED = 55;
	public static final int ORDER_STATUS_PENDING_CANCELLATION = 53;
	public static final int ORDER_STATUS_PAID = 51;

	public static final String ORDER_STATUS_PLACED_LABEL = "Placed";
	public static final String ORDER_STATUS_AWAITING_PAYMENT_LABEL = "Awaiting Payment/Blocked";
	public static final String ORDER_STATUS_CONFIRMED_LABEL = "Confirmed";
	public static final String ORDER_STATUS_PENDING_CANCELLATION_LABEL = "Pending Cancellation";
	public static final String ORDER_STATUS_COMPLETED_LABEL = "Invoiced";
	public static final String ORDER_STATUS_SHIPPED_LABEL = "Shipped";
	public static final String ORDER_STATUS_CANCELED_LABEL = "Canceled";
	public static final String ORDER_STATUS_CANCELLED_LABEL = "Cancelled";
	public static final String ORDER_STATUS_PARTIALLY_SHIPPED_LABEL = "Partially Shipped";
	public static final String ORDER_STATUS_PAID_LABEL = "Paid";

	public static final String ORDER_STATUS_PLACED_ICON = "order-confirmed.svg";
	public static final String ORDER_STATUS_PAID_ICON = "paid.svg";
	public static final String ORDER_STATUS_AWAITING_PAYMENT_ICON = "await-order.svg";
	public static final String ORDER_STATUS_CONFIRMED_ICON = "out-delivery.svg";
	public static final String ORDER_STATUS_PENDING_CANCELLATION_ICON = "delivered.svg";
	public static final String ORDER_STATUS_COMPLETED_ICON = "invoice-order.svg";
	public static final String ORDER_STATUS_SHIPPED_ICON = "shipped.svg";
	public static final String ORDER_STATUS_PARTIALLY_SHIPPED_ICON = "out-delivery.svg";
	public static final String ORDER_STATUS_CALCELLED_ICON = "cancel-order.svg";

	/**
	 * Gets the orderStatus icon url.
	 *
	 * @param orderStatus
	 * @return icon
	 */
	public static String getOrderStatusIcon(String orderStatus) {

		if (orderStatus.equalsIgnoreCase(ORDER_STATUS_PLACED_LABEL)) {
			return ORDER_STATUS_PLACED_ICON;
		} else if (orderStatus.equalsIgnoreCase(ORDER_STATUS_PENDING_CANCELLATION_LABEL)) {
			return ORDER_STATUS_PENDING_CANCELLATION_ICON;
		} else if (orderStatus.equalsIgnoreCase(ORDER_STATUS_SHIPPED_LABEL)) {
			return ORDER_STATUS_SHIPPED_ICON;
		} else if (orderStatus.equalsIgnoreCase(ORDER_STATUS_PARTIALLY_SHIPPED_LABEL)) {
			return ORDER_STATUS_PARTIALLY_SHIPPED_ICON;
		} else if (orderStatus.equalsIgnoreCase(ORDER_STATUS_COMPLETED_LABEL)) {
			return ORDER_STATUS_COMPLETED_ICON;
		} else if (orderStatus.equalsIgnoreCase(ORDER_STATUS_CANCELED_LABEL)) {
			return ORDER_STATUS_CALCELLED_ICON;
		} else if (orderStatus.equalsIgnoreCase(ORDER_STATUS_AWAITING_PAYMENT_LABEL)) {
			return ORDER_STATUS_AWAITING_PAYMENT_ICON;
		} else if (orderStatus.equalsIgnoreCase(ORDER_STATUS_CONFIRMED_LABEL)) {
			return ORDER_STATUS_CONFIRMED_ICON;
		} else if (orderStatus.equalsIgnoreCase(ORDER_STATUS_PAID_LABEL)){
			return ORDER_STATUS_PAID_ICON;
		}
		return StringPool.BLANK;
	}

	/**
	 * Gets the orderStatus label.
	 *
	 * @return orderStatus label
	 */
	public static String getOrderStatus(int status) {

		if (status == ORDER_STATUS_PLACED) {
			return ORDER_STATUS_PLACED_LABEL;
		} else if (status == CommerceOrderConstants.ORDER_STATUS_SHIPPED) {
			return ORDER_STATUS_SHIPPED_LABEL;
		} else if (status == CommerceOrderConstants.ORDER_STATUS_PARTIALLY_SHIPPED) {
			return ORDER_STATUS_PARTIALLY_SHIPPED_LABEL;
		} else if (status == CommerceOrderStatusOverrideConstants.ORDER_STATUS_AWAITING_PAYMENT) {
			return ORDER_STATUS_AWAITING_PAYMENT_LABEL;
		} else if (status == ORDER_STATUS_CONFIRMED) {
			return ORDER_STATUS_CONFIRMED_LABEL;
		} else if (status == ORDER_STATUS_PENDING_CANCELLATION) {
			return ORDER_STATUS_PENDING_CANCELLATION_LABEL;
		} else if (status == CommerceOrderConstants.ORDER_STATUS_COMPLETED) {
			return ORDER_STATUS_COMPLETED_LABEL;
		} else if (status == CommerceOrderConstants.ORDER_STATUS_CANCELLED) {
			return ORDER_STATUS_CANCELLED_LABEL;
		} else if (status == ORDER_STATUS_PAID){
			return ORDER_STATUS_PAID_LABEL;
		}

		return StringPool.BLANK;
	}

	/**
	 * Gets the orderStatus label.
	 *
	 * @return orderStatus label
	 */
	public static int getOrderStatus(String orderStatusLabel) {

		if (orderStatusLabel.equalsIgnoreCase(ORDER_STATUS_PLACED_LABEL)) {
			return ORDER_STATUS_PLACED;
		} else if (orderStatusLabel.equalsIgnoreCase(ORDER_STATUS_SHIPPED_LABEL)) {
			return CommerceOrderConstants.ORDER_STATUS_SHIPPED;
		} else if (orderStatusLabel.equalsIgnoreCase(ORDER_STATUS_PARTIALLY_SHIPPED_LABEL)) {
			return CommerceOrderConstants.ORDER_STATUS_PARTIALLY_SHIPPED;
		} else if (orderStatusLabel.equalsIgnoreCase(ORDER_STATUS_AWAITING_PAYMENT_LABEL)) {
			return CommerceOrderStatusOverrideConstants.ORDER_STATUS_AWAITING_PAYMENT;
		} else if (orderStatusLabel.equalsIgnoreCase(ORDER_STATUS_CONFIRMED_LABEL)) {
			return ORDER_STATUS_CONFIRMED;
		} else if (orderStatusLabel.equalsIgnoreCase(ORDER_STATUS_PENDING_CANCELLATION_LABEL)) {
			return ORDER_STATUS_PENDING_CANCELLATION;
		} else if (orderStatusLabel.equalsIgnoreCase(ORDER_STATUS_COMPLETED_LABEL)) {
			return CommerceOrderConstants.ORDER_STATUS_COMPLETED;
		} else if (orderStatusLabel.equalsIgnoreCase(ORDER_STATUS_CANCELLED_LABEL)
				|| orderStatusLabel.equalsIgnoreCase(ORDER_STATUS_CANCELED_LABEL)) {
			return CommerceOrderConstants.ORDER_STATUS_CANCELLED;
		} else if (orderStatusLabel.equalsIgnoreCase(ORDER_STATUS_PAID_LABEL)){
			return ORDER_STATUS_PAID;
		}
		return -1;
	}
	
	

}
