package com.liferay.gsindia.portlet;

import com.liferay.gsindia.configuration.RazorpayConfiguration;
import com.liferay.gsindia.constants.LfgrsindRazorpayIntegrationPortletKeys;
import com.liferay.gsindia.util.PaymentGatewayUtils;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import java.io.IOException;
import java.io.PrintWriter;

import javax.portlet.Portlet;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;

/**
 * @author prakash
 */
@Component(
		immediate = true,
		property = {
				"com.liferay.portlet.display-category=category.sample",
				"com.liferay.portlet.header-portlet-css=/css/main.css",
				"com.liferay.portlet.instanceable=true",
				"javax.portlet.display-name=LfgrsindRazorpayIntegration",
				"javax.portlet.init-param.template-path=/",
				"javax.portlet.init-param.view-template=/view.jsp",
				"javax.portlet.name=" + LfgrsindRazorpayIntegrationPortletKeys.LFGRSINDRAZORPAYINTEGRATION,
				"javax.portlet.resource-bundle=content.Language",
				"javax.portlet.security-role-ref=power-user,user"
		},
		service = Portlet.class
		)
public class LfgrsindRazorpayIntegrationPortlet extends MVCPortlet {
	private Log logger =LogFactoryUtil.getLog(LfgrsindRazorpayIntegrationPortlet.class);
	@Override
	public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException {
		logger.info("ServeResource Called");
		RazorpayConfiguration razorpayConfiguration = null;
		String keyId = null;
		String secretKey = null;
		try {

			//Fetch Razorpay Configuration for System Configuration
			razorpayConfiguration = ConfigurationProviderUtil.getSystemConfiguration(RazorpayConfiguration.class);
			if(Validator.isNotNull(razorpayConfiguration)) {
				keyId = razorpayConfiguration.keyId();
				secretKey = razorpayConfiguration.secretKey();
			}
		} catch (ConfigurationException e2) {
			logger.error("Error while fetching system configuration : "+e2.getMessage());
		}

		RazorpayClient razorpay = null;
		try {
			//razorpay = new RazorpayClient("rzp_test_3E3bgSHDzCcyY7","k0kHCLPJPq3Hi7vyM4z2vw9y");
			razorpay = new RazorpayClient(keyId,secretKey);
		} catch (RazorpayException e1) {
			logger.error("Error while in RazorPayClient : "+e1.getMessage());
		}

		if(ParamUtil.getString(resourceRequest,"action").equals("orderCreation"))
		{
			logger.info("OrderCreation Called");
			JSONArray responseJsonArray = JSONFactoryUtil.createJSONArray();
			JSONObject jsonFlagObject = null;
			PrintWriter out = resourceResponse.getWriter();

			//Generate UniqueId to be used as bookingId
			SequenceGenerator uniqueId = SequenceGenerator.getInstance(); 
			String bookingId = Long.toString(uniqueId.nextId());
			int amount = ParamUtil.getInteger(resourceRequest, "amount");

			org.json.JSONObject orderRequest = new org.json.JSONObject();
			orderRequest.put("amount", amount);
			orderRequest.put("currency", "INR");
			orderRequest.put("receipt", bookingId);

			Order order = null;

			try {
				String loginId = PortalUtil.getUser(resourceRequest).getScreenName();
				order = razorpay.Orders.create(orderRequest);
				org.json.JSONObject orderResponse = order.toJson();
				logger.debug("Order Details >> \n"+order);
				if(orderResponse.get("status").equals("created"))
				{
					//If Order is Created
					logger.info("Order Id Generated for loginId ::    "+loginId);
					jsonFlagObject = JSONFactoryUtil.createJSONObject();
					jsonFlagObject.put("status", "created");
					jsonFlagObject.put("orderId",orderResponse.get("id"));
					jsonFlagObject.put("amount_due", orderResponse.get("amount"));
					jsonFlagObject.put("currency",orderResponse.get("currency"));
					jsonFlagObject.put("createDate",orderResponse.get("created_at"));
					responseJsonArray.put(jsonFlagObject);

				}
				else {
					//Failed to Create Order
					logger.error("Order Id not generated from RazorPay");
					jsonFlagObject = JSONFactoryUtil.createJSONObject();
					jsonFlagObject.put("status", "false");
					responseJsonArray.put(jsonFlagObject);
				}
				out.print(responseJsonArray.toString());

			} catch (SystemException | PortalException | RazorpayException e) {
				logger.error("Error while creating orderId : "+e.getMessage());
				jsonFlagObject = JSONFactoryUtil.createJSONObject();
				jsonFlagObject.put("status", "false");
				responseJsonArray.put(jsonFlagObject);
				out.print(responseJsonArray.toString());
				e.printStackTrace();

			}	
			catch(Exception e)
			{
				logger.error("Error while creating orderId : "+e.getMessage());
				jsonFlagObject = JSONFactoryUtil.createJSONObject();
				jsonFlagObject.put("status", "false");
				responseJsonArray.put(jsonFlagObject);
				out.print(responseJsonArray.toString());
			}
		}
		// Verify Payment Signature after payment is successful
		else if (ParamUtil.getString(resourceRequest, "action").equals("verifySig")) {
			JSONArray responseJsonArray = JSONFactoryUtil.createJSONArray();
			JSONObject jsonFlagObject = null;
			PrintWriter out = resourceResponse.getWriter();

			String paymentId = ParamUtil.getString(resourceRequest, "paymentId");
			String orderId = ParamUtil.getString(resourceRequest, "orderId");
			String razorpaySignature = ParamUtil.getString(resourceRequest, "signature");

			if (StringUtils.isNotBlank(paymentId) && StringUtils.isNotBlank(razorpaySignature)
					&& StringUtils.isNotBlank(orderId)) {
				boolean isEqual = PaymentGatewayUtils.verifySignature(paymentId, orderId, razorpaySignature, secretKey);

				if(isEqual) {
					Payment payment = null;
					org.json.JSONObject paymentResponse =null;
					String status = null;
					try {
						String loginId = PortalUtil.getUser(resourceRequest).getScreenName();						logger.info("Payment Successful from authencited source ::: "+loginId);
						jsonFlagObject = JSONFactoryUtil.createJSONObject();
						payment = razorpay.Payments.fetch(paymentId);
						logger.info("Payment details : "+payment);
						paymentResponse = payment.toJson();

						status = paymentResponse.getString("status");
						if("captured".equals(paymentResponse.getString("status"))) {
							logger.info("Payment Status is == "+ status +"  ::: "+loginId);
							jsonFlagObject.put("paymentId", paymentResponse.get("id").toString());
							jsonFlagObject.put("status","true");
							jsonFlagObject.put("razorpayStatus", status);
							responseJsonArray.put(jsonFlagObject);
						}
						else {
							logger.info("Payment Status is == "+ status +", Considered as Failure  ::: "+loginId);
							jsonFlagObject.put("paymentId", paymentResponse.get("id").toString());
							jsonFlagObject.put("status","false");
							jsonFlagObject.put("razorpayStatus", status);
							responseJsonArray.put(jsonFlagObject);
						}

						out.print(responseJsonArray.toString());				
					}
					catch (Exception e) {
						jsonFlagObject = JSONFactoryUtil.createJSONObject();
						jsonFlagObject.put("paymentId", paymentResponse.get("id").toString());
						jsonFlagObject.put("status","false");
						jsonFlagObject.put("razorpayStatus", status);
						responseJsonArray.put(jsonFlagObject);
						out.print(responseJsonArray.toString());
						logger.error("Error Occured "+e.getMessage());
					}
				}
				else
				{
					Payment payment = null;
					org.json.JSONObject paymentResponse = null;
					try {
						String loginId = PortalUtil.getUser(resourceRequest).getScreenName();
						logger.info("Payment from non-authencited source -- Payment Failed  ::::"+loginId);
						payment = razorpay.Payments.fetch(paymentId);
						logger.debug("Payment Response from Razorpay : "+payment);
						paymentResponse = payment.toJson();
						String status = paymentResponse.getString("status");
						logger.info("Payment Status is == "+ status +"  ::: "+loginId);

						jsonFlagObject = JSONFactoryUtil.createJSONObject();
						jsonFlagObject.put("paymentId", paymentResponse.get("id").toString());
						jsonFlagObject.put("status","false");
						responseJsonArray.put(jsonFlagObject);
						out.print(responseJsonArray.toString());
					}
					catch(RazorpayException e)
					{
						logger.error("LfgrsindRazorpayIntegrationPortlet :: verifySig" + e.getMessage() );
						jsonFlagObject = JSONFactoryUtil.createJSONObject();
						jsonFlagObject.put("status","false");
						responseJsonArray.put(jsonFlagObject);
						out.print(responseJsonArray.toString());
					}
					catch(Exception e1)
					{
						logger.error("LfgrsindRazorpayIntegrationPortlet :: verifySig" + e1.getMessage());
						jsonFlagObject = JSONFactoryUtil.createJSONObject();
						jsonFlagObject.put("paymentId", paymentResponse.get("id").toString());
						jsonFlagObject.put("status","false");
						responseJsonArray.put(jsonFlagObject);
						out.print(responseJsonArray.toString());
					}
				}
			}
			else
			{
				logger.error("Payment Id / Order Id / Razorpay Signature Cannot be blank");
				jsonFlagObject = JSONFactoryUtil.createJSONObject();
				jsonFlagObject.put("status","false");
				responseJsonArray.put(jsonFlagObject);
				out.print(responseJsonArray.toString());
			}

		}
	}
}