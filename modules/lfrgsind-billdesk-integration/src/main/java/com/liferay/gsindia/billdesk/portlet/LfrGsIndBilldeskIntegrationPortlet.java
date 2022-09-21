package com.liferay.gsindia.billdesk.portlet;

import com.liferay.counter.kernel.service.CounterLocalServiceUtil;
import com.liferay.gsindia.billdesk.configuration.BilldeskConfiguration;
import com.liferay.gsindia.billdesk.constants.LfrGsIndBilldeskIntegrationPortletKeys;
import com.liferay.gsindia.billdesk.util.LfrgsIndBilldeskUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;

/**
 * @author Madhukar.kumar@liferay.com
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.display-category=category.sample",
		"com.liferay.portlet.header-portlet-css=/css/main.css",
		"com.liferay.portlet.instanceable=true",
		"javax.portlet.display-name=LfrGsIndBilldeskIntegration",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=" + LfrGsIndBilldeskIntegrationPortletKeys.LFRGSINDBILLDESKINTEGRATION,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user"
	},
	service = Portlet.class
)
public class LfrGsIndBilldeskIntegrationPortlet extends MVCPortlet {
	
	@Override
	public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws PortletException, IOException {
		BilldeskConfiguration _billDeskConfiguration=null;
		try {
			_billDeskConfiguration = ConfigurationProviderUtil.getSystemConfiguration(BilldeskConfiguration.class);
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
		}
		
		String token=null;
		String bdOrderId=null;
		String OrderInvoiceData = ParamUtil.getString(resourceRequest, "OrderData");
		System.out.println("Order data"+OrderInvoiceData);
		ThemeDisplay themeDisplay = (ThemeDisplay) resourceRequest.getAttribute(WebKeys.THEME_DISPLAY);
		User user=null;
		String paymentRecepientNo=null;
		Date paymentDate=new Date();
		
		try {
			user=UserLocalServiceUtil.getUserById(themeDisplay.getUserId());
		}
		 catch (PortalException e) {
			e.printStackTrace();
		}
		double totalAmount = 0;
		try {
			JSONArray jsonArray = JSONFactoryUtil.createJSONArray(OrderInvoiceData);
			System.out.println("Jsonaray "+jsonArray);
			int noOfDoc = 0;

			paymentRecepientNo = getPaymentReceiptNo();
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonobject = jsonArray.getJSONObject(i);
				// jsonobject.getDouble("Amount");
				// jsonobject.getString("Document_No");

				totalAmount = totalAmount + jsonobject.getDouble("Amount");
				noOfDoc = jsonArray.length();
			}
		}catch(Exception e) {
			System.out.println("Error hgetting values");
		}
		
		//add payment detail in lifreay custom table using service builder
		//onlinepayment=OnlinePaymentLocalServiceUtil.addOnlinePayment(themeDisplay, paymentRecepientNo, paymentDate, AMOUNT, user.getScreenName(),1,PanasonicAdvancePaymentPortletKeys.INITIALIZED);
		
		String ip = PortalUtil.getHttpServletRequest(resourceRequest).getRemoteAddr();
		String useragent= PortalUtil.getHttpServletRequest(resourceRequest).getHeader("USER-AGENT");
		JSONObject responseObj=LfrgsIndBilldeskUtil.getToken(paymentRecepientNo,Double.toString(totalAmount),_billDeskConfiguration.currency(),_billDeskConfiguration.webHookURL(),
				user.getScreenName(),"additionalinfo2","internat",ip,useragent,paymentRecepientNo,
				paymentRecepientNo,"","");
		token=LfrgsIndBilldeskUtil.parseOauthToken(responseObj);
		bdOrderId=LfrgsIndBilldeskUtil.parsebdOrderId(responseObj);
		
		Date date = paymentDate;  
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");  
		String strDate = dateFormat.format(date);
		if(token!=null) {
			//Update your payment table status here
			//using serviceLocalserviceUtil
		}
		//OnlinePaymentDetailLocalServiceUtil.addOnlinePaymentDetail(themeDisplay, paymentRecepientNo, paymentDate, (AMOUNT), user.getScreenName(), bdOrderId, "RECPT", null,strDate, AMOUNT,BU);
		
		PrintWriter writer = resourceResponse.getWriter();
		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
		jsonObject.put("bdOrderId", bdOrderId);
		jsonObject.put("token", token);
		writer.write(jsonObject.toString());

		
		
	}
		public static String getPaymentReceiptNo() {
			// It will generate 10 digit random Number.
			// from 0 to 9999999999
			long c = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
			return Long.toString(c);
		}
}