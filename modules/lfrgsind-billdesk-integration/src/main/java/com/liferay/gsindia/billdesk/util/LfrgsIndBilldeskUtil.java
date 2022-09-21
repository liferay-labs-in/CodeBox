package com.liferay.gsindia.billdesk.util;

import com.liferay.gsindia.billdesk.configuration.BilldeskConfiguration;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.module.configuration.ConfigurationProviderUtil;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class LfrgsIndBilldeskUtil {

	
	private static Log _log = LogFactoryUtil.getLog(LfrgsIndBilldeskUtil.class);
	public static String encryptAndSignJWSWithHMAC(String reqStr, String secretKey, String clientid)
		    throws JOSEException, ConfigurationException {
			JWSSigner signer = new MACSigner(secretKey);
		    HashMap<String, Object> customParams = new HashMap<String, Object>();
		    customParams.put("clientid", clientid);
		    JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256,
		                                        null,
		                                        null,
		                                        null,
		                                        null,
		                                        null,
		                                        null,
		                                        null,
		                                        null,
		                                        null,
		                                        null,
		                                        customParams,
		                                        null);
		    JWSObject jwsObject = new JWSObject(jwsHeader, new Payload(reqStr)); // Apply the HMAC
		    jwsObject.sign(signer);
		    return jwsObject.serialize();
		  }

		  public static String verifyAndDecryptJWSWithHMAC(String encryptedSignedMessage,
		                                                   String verificationKey)
		    throws Exception {

		    JWSObject jwsObject = JWSObject.parse(encryptedSignedMessage);
		    
		    String clientId = jwsObject.getHeader().getCustomParam("clientid").toString();
		    JWSVerifier verifier = new MACVerifier(verificationKey);
		    boolean isVerified = jwsObject.verify(verifier);
		    String message = jwsObject.getPayload().toString();
		    return message;
		  }
		  		  
		  public static HttpResponse<String> callUnirest(String newRequest) {
				Unirest.setTimeouts(0, 0);
				HttpResponse<String> response=null;
				
				try {
					BilldeskConfiguration _billDeskConfiguration = ConfigurationProviderUtil.getSystemConfiguration(BilldeskConfiguration.class);
					//Certificates
					//No certificate
					//End certificates
					long currentTimestamp = System.currentTimeMillis();
					response = Unirest.post(_billDeskConfiguration.createAPI())
					  .header("content-type", "application/jose")
					  .header("bd-timestamp", Long.toString(currentTimestamp))
					  .header("accept", " application/jose")
					  .header("bd-traceid", "Pana"+Long.toString(currentTimestamp))		
					  .body(newRequest)
					  .asString();
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				return response;
			}
		  
		  public static HttpResponse<String> callUnirestTransactionStatus(String newRequest) {
				Unirest.setTimeouts(0, 0);
				HttpResponse<String> response=null;
				
				try {
					BilldeskConfiguration _billDeskConfiguration = ConfigurationProviderUtil.getSystemConfiguration(BilldeskConfiguration.class);
					
					//Certificates
					//No certificate
					//End certificates
					long currentTimestamp = System.currentTimeMillis();
					System.out.println("Timestamp "+currentTimestamp);
					response = Unirest.post(_billDeskConfiguration.statusAPI())
					  .header("content-type", "application/jose")
					  .header("bd-timestamp", Long.toString(currentTimestamp))
					  .header("accept", " application/jose")
					  .header("bd-traceid", "Pana"+Long.toString(currentTimestamp))		
					  .body(newRequest)
					  .asString();
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				return response;
			}
		  
		  public static JSONObject createPayload(String orderId,String amount,String currency,String ru,
					String additionalinfo1,String additionalinfo2,String initchannel,String ip,String useragent,String invoiceNo,
					String invoice_display_number,String customername,String invoicedate) throws ConfigurationException {
			    BilldeskConfiguration _billDeskConfiguration = ConfigurationProviderUtil.getSystemConfiguration(BilldeskConfiguration.class);
				
				JSONObject jsonObj=JSONFactoryUtil.createJSONObject();
				jsonObj.put("mercid", _billDeskConfiguration.merchantId());
				jsonObj.put("orderid", orderId);
				jsonObj.put("amount", amount);
				//Format current time zone
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				formatter.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
				String orderDate=formatter.format(new Date())+"+05:30";
				jsonObj.put("order_date",orderDate);
				jsonObj.put("currency",currency);
				jsonObj.put("ru",ru);
				//Additionalinfo jsonobj
				JSONObject jsonObj1=JSONFactoryUtil.createJSONObject();
				jsonObj1.put("additional_info1",additionalinfo1);
				jsonObj1.put("additional_info2",additionalinfo2);
				jsonObj.put("additional_info", jsonObj1);
				jsonObj.put("itemcode", _billDeskConfiguration.itemcode());
				
				//Invoice details	
					
					  JSONObject jsonObj2=JSONFactoryUtil.createJSONObject();
					  jsonObj2.put("invoice_number", invoiceNo);
					  jsonObj2.put("invoice_display_number", invoice_display_number);
					  jsonObj2.put("customer_name", customername); 
					  jsonObj2.put("invoice_date", orderDate);
					   JSONObject jsonobj3=JSONFactoryUtil.createJSONObject();
					   jsonobj3.put("cgst", "");
					   jsonobj3.put("sgst", "");
					   jsonobj3.put("igst", "");
					   jsonobj3.put("gst", "");
					   jsonobj3.put("cess", "");
					   jsonobj3.put("gstincentive", "");
					   jsonobj3.put("gstpct", "");
					   jsonobj3.put("gstin", "");
					jsonObj2.put("gst_details", jsonobj3);					  
				jsonObj.put("invoice", jsonObj2);
	
				//device details
				JSONObject deviceObj=JSONFactoryUtil.createJSONObject();
				deviceObj.put("init_channel", initchannel);
				deviceObj.put("ip", ip);
				deviceObj.put("user_agent",useragent);
				
				jsonObj.put("device", deviceObj);	
				System.out.println("Request "+jsonObj);
				return jsonObj;
			}
	public static JSONObject createPayloadStatus(String orderId,String merchantId) throws ConfigurationException{
		BilldeskConfiguration _billDeskConfiguration = ConfigurationProviderUtil.getSystemConfiguration(BilldeskConfiguration.class);
		
		JSONObject jsonObj=JSONFactoryUtil.createJSONObject();
		jsonObj.put("mercid", _billDeskConfiguration.merchantId());
		jsonObj.put("orderid", orderId);
		return jsonObj;
	}
		  
	public static String parseOauthToken(JSONObject responseObj) {
			String tokenString=null;
			//Extract token from response
			try {
			String  jsonAray=responseObj.getString("links");
			System.out.println("Response is "+jsonAray);
			JSONArray arrayRes=JSONFactoryUtil.createJSONArray(jsonAray);
			JSONObject jsonObj=arrayRes.getJSONObject(1).getJSONObject("headers");
			tokenString=jsonObj.getString("authorization");
			}catch(Exception e){
				_log.error("Error parsing JOSN array");
			}
			
			return tokenString;
		}
		
	public static String parsebdOrderId(JSONObject responseObj) {
		String tokenString=null;
		//Extract token from response
		try {
			tokenString=responseObj.getString("bdorderid");
		}catch(Exception e){
			_log.error("Error parsing JOSN array");
		}
		
		return tokenString;
	}
	
	public static JSONObject getToken(String orderId,String amount,String currency,String ru,
			String additionalinfo1,String additionalinfo2,String initchannel,String ip,String useragent,String invoiceNo,
			String invoice_display_number,String customername,String invoicedate) {
		JSONObject responseObj=null;
		try {
			BilldeskConfiguration _billDeskConfiguration = ConfigurationProviderUtil.getSystemConfiguration(BilldeskConfiguration.class);
			
			JSONObject payload=JSONFactoryUtil.createJSONObject();
			
			payload=createPayload(orderId,amount,currency,ru,
					additionalinfo1,additionalinfo2,initchannel,ip,useragent,invoiceNo,
					invoice_display_number,customername,invoicedate);
			_log.debug("Payload is                  "+payload);
			String encryptedString=LfrgsIndBilldeskUtil.encryptAndSignJWSWithHMAC(payload.toString(), _billDeskConfiguration.secretKey(), _billDeskConfiguration.clientId());
			//decrypt
			HttpResponse<String> response=LfrgsIndBilldeskUtil.callUnirest(encryptedString);
			String responseencrypted= response.getBody();
			String responsedecrypted=LfrgsIndBilldeskUtil.verifyAndDecryptJWSWithHMAC(responseencrypted, _billDeskConfiguration.secretKey());
			responseObj=JSONFactoryUtil.createJSONObject(responsedecrypted);
			_log.debug("response decrepted                 "+responsedecrypted);
			_log.debug("responseObject is                     "+responseObj);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return responseObj;
	}
	
	
	public static Map<String,String> getPaymentStatus(String orderId,String merchantId) {
		String responseString=null;
		String transactionId=StringPool.BLANK;
		Map<String ,String> responseMap=new HashMap<String, String>();
		try {
			JSONObject payload=JSONFactoryUtil.createJSONObject();
			BilldeskConfiguration _billDeskConfiguration = ConfigurationProviderUtil.getSystemConfiguration(BilldeskConfiguration.class);
			
			payload=createPayloadStatus(orderId, merchantId);
			_log.debug("Payload is "+payload);
			String encryptedString=LfrgsIndBilldeskUtil.encryptAndSignJWSWithHMAC(payload.toString(), _billDeskConfiguration.secretKey(), _billDeskConfiguration.clientId());
			_log.debug("Encrypted payload :" +encryptedString);
			//decrypt
			HttpResponse<String> response=LfrgsIndBilldeskUtil.callUnirestTransactionStatus(encryptedString);
			String responseBody= response.getBody();
			String responsedecrypted=LfrgsIndBilldeskUtil.verifyAndDecryptJWSWithHMAC(responseBody, _billDeskConfiguration.secretKey());
			_log.debug("Decrypted response is : "+responsedecrypted);
			JSONObject responseObject=JSONFactoryUtil.createJSONObject(responsedecrypted);
			responseString=LfrgsIndBilldeskUtil.parseStatus(responseObject);
			transactionId=LfrgsIndBilldeskUtil.parsetransactionId(responseObject);	
			responseMap.put("Status", responseString);
			responseMap.put("TransactionId",transactionId);
			_log.debug("responseObject is"+responseString);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return responseMap;
	}
	
	public static String parseStatus(JSONObject jsonObj) {
		String status=null;
		try {
			status=jsonObj.getString("transaction_error_type");
		}catch(Exception e) {
		_log.error("Error parsing json object"+e.getMessage());
		}
		return status;
		
	}
	
	public static String parsetransactionId(JSONObject jsonObj) {
		String status=null;
		try {
			status=jsonObj.getString("transactionid");
		}catch(Exception e) {
		_log.error("Error parsing json object"+e.getMessage());
		}
		return status;
		
	}
}