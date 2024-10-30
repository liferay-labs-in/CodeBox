package com.liferay.gsindia.util;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

/**
 * @author prakash
 *
 */
public class PaymentGatewayUtils {
	
	private static final Log logger = LogFactoryUtil.getLog(PaymentGatewayUtils.class);

	private PaymentGatewayUtils()
	{
		throw new IllegalStateException("Payment Gateway Utility class");
		 
	}
	/*
	 * Verify the razorpay signature with the generated signature
	 */
	public static boolean verifySignature(String paymentId,String orderId,String signature,String secretKey) {

		logger.info("PaymentGatewayUtils :::: verifySignature  >> PaymentId ::: "+paymentId);
		org.json.JSONObject options = new org.json.JSONObject();
			boolean isEqual = false;
			try {
				options.put("razorpay_payment_id", paymentId);
				options.put("razorpay_order_id", orderId);
				options.put("razorpay_signature", signature);
				isEqual = Utils.verifyPaymentSignature(options, secretKey);
			} 
			catch (RazorpayException e) {
				logger.error("Exception caused while Verifying Signature " + e.getMessage());
			}
		return isEqual;
	}
}
