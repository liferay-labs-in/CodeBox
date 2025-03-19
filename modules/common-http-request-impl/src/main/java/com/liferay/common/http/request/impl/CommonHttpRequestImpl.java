package com.liferay.common.http.request.impl;


import com.liferay.common.http.request.api.CommonHttpRequestApi;
import com.liferay.common.http.request.api.constants.DataNotFoundException;
import org.osgi.service.component.annotations.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import org.osgi.service.component.annotations.Component;

import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
/**
 * @author Payal
 */
@Component(
	property = {
	},
	service = CommonHttpRequestApi.class
)
public class CommonHttpRequestImpl implements CommonHttpRequestApi {


	public static final String HTTP_GET_METHOD="GET";
	public static final String APPLICATION_JSON = MediaType.APPLICATION_JSON;
	public static final String ACCEPT ="Accept";
	public static final String HTTP_POST_METHOD ="POST";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String APPLICATION_JSON_UTF8 = "application/json; utf-8";
	public static final String AUTHORIZATION = "Authorization";
	public static final String SPACE_REPLACEMENT_CHAR = "%20";
	public static final String UTF8_ENCODING = "utf-8";
	public static final String EXCEPTION_REST_SERVICE_CALL_MSG="Application Issue: Failed to Rest Service[FNE_ERRR009] : HTTP Error Code";
	public static final String HTTP_PUT_METHOD = "PUT";
	public static final String HTTP_DELETE_METHOD = "DELETE";

	final Log logger = LogFactoryUtil.getLog(CommonHttpRequestImpl.class);

	// Method for HTTP requests

	@Override
	public <T> T invoketHttpsGet(String url, String urlParameters, Class<T> responseType,
								 Map<String, String> requestHeaderAttributes) {
		ObjectMapper objectMapper = new ObjectMapper();
		HttpsURLConnection conn = null;
		StringBuilder jsonBuffer = new StringBuilder();
		URL endpointURL;

		try {
			if (urlParameters != null && !urlParameters.isEmpty()) {
				endpointURL = new URL(url + StringPool.QUESTION
						+ urlParameters.replace(StringPool.SPACE, StringPool.BLANK));
			} else {
				endpointURL = new URL(url);
			}
			logger.debug("endpointURL: " + endpointURL);

			conn = (HttpsURLConnection) endpointURL.openConnection();
			conn.setRequestMethod(HTTP_GET_METHOD);
			conn.setRequestProperty(ACCEPT, APPLICATION_JSON);

			if (requestHeaderAttributes != null) {
				requestHeaderAttributes.forEach(conn::setRequestProperty);
			}

			validateConnResponse(conn, jsonBuffer);

			convertStreamToJsonBuffer(conn.getInputStream(), jsonBuffer);
			// Deserialize the JSON response to an object of type T
			return objectMapper.readValue(jsonBuffer.toString(), responseType);

		} catch (Exception e) {
			logger.error("Error occurred in invokeHttpsGet: " + e.getMessage(), e);
			throw new RuntimeException("Failed to invoke GET request", e);

		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	@Override
	public String invokeGetRestServiceHttps(String url, String requestBody, String urlParameters,
											Map<String, String> requestHeaderAttributes) {

		HttpsURLConnection conn = null;
		String output;
		String response = null;
		StringBuilder jsonBuffer = new StringBuilder();
		URL endpointURL;

		try {
			if (null != urlParameters && !urlParameters.isEmpty()) {
				endpointURL = new URL(url + StringPool.QUESTION
						+ urlParameters.replace(StringPool.SPACE, StringPool.BLANK));
			} else {
				endpointURL = new URL(url);
			}
			logger.debug("endpointURL ::" + url);
			conn = (HttpsURLConnection) endpointURL.openConnection();
			conn.setRequestMethod(HTTP_GET_METHOD);
			conn.setRequestProperty(ACCEPT, APPLICATION_JSON);

			if (null != requestHeaderAttributes && !requestHeaderAttributes.isEmpty()) {
				for (Map.Entry<String, String> entry : requestHeaderAttributes.entrySet()) {
					conn.setRequestProperty(entry.getKey(), entry.getValue());
				}
			}

			validateConnResponse(conn, jsonBuffer);

			try (BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())))) {

				while ((output = br.readLine()) != null) {
					jsonBuffer.append(output);
				}
			} catch (Exception e) {
				logger.error(
						"CommonService :: invokeGetRestServiceHttps :: Error Occurred:while reading output to json buffer ::Exception::"
								+ e);
			}
			response = jsonBuffer.toString();

		} catch (PortalException e) {
			logger.error(
					"CommonService :: invokeGetRestServiceHttps :: Error Occurred:PortalException::" + e.getMessage());

		} catch (Exception e) {
			logger.error(
					"CommonService :: invokeGetRestServiceHttps :: Error Occurred:Exception::" + e.getMessage());

		} finally {

			if (null != conn) {
				conn.disconnect();
			}
		}
		logger.debug("CommonService :: exiting from invokeGetRestServiceHttps() method");
		return response;
	}

	private void validateConnResponse(HttpsURLConnection conn, StringBuilder jsonBuffer) throws Exception {
		int responseCode = conn.getResponseCode();
		logger.info("API ResponseCode: "+ responseCode+ "Response Message: "+ conn.getErrorStream());
		if (responseCode != HttpsURLConnection.HTTP_OK && responseCode != HttpsURLConnection.HTTP_CREATED) {

			String errorMessage = getErrorMessage(conn, jsonBuffer);

			if (responseCode == Response.Status.BAD_REQUEST.getStatusCode() || responseCode == 422) {
				throw new BadRequestException("API Bad Request Exception: " + errorMessage);
			}

			if (responseCode == Response.Status.NOT_FOUND.getStatusCode()) {
				throw new DataNotFoundException("API Data Not Found Exception: " + errorMessage);
			}

			throw new PortalException("API call failure with HTTP error code: " + responseCode+ ", ErrorMessage: "+errorMessage);
		}
	}

	private static String getErrorMessage(HttpsURLConnection conn, StringBuilder jsonBuffer) throws IOException, JSONException {
		String errorMessage = conn.getResponseMessage();
		convertStreamToJsonBuffer(conn.getErrorStream(), jsonBuffer);
		if (jsonBuffer.length() > 0) {
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject(jsonBuffer.toString());
			errorMessage = jsonObject.getString("error");
		}
		return errorMessage;
	}

	private static void convertStreamToJsonBuffer(InputStream inputStreamResponse, StringBuilder jsonBuffer) throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStreamResponse))) {
			String output;
			while ((output = br.readLine()) != null) {
				jsonBuffer.append(output);
			}
		}
	}

	@Override
	public String invokePostRestService(String url, String requestBody,String authHeader, String urlParameters) {
		Long startTime =System.currentTimeMillis();
		HttpURLConnection conn = null;
		String response = null;
		StringBuilder jsonBuffer = new StringBuilder();
		URL endpointURL = null;
		try {
			if(null!=urlParameters && !urlParameters.isEmpty()) {
				endpointURL = new URL(url+StringPool.QUESTION+urlParameters.replace(StringPool.SPACE, SPACE_REPLACEMENT_CHAR));
			}else {
				endpointURL = new URL(url);
			}

			conn = (HttpURLConnection) endpointURL.openConnection();

			conn.setRequestMethod(HTTP_POST_METHOD);
			conn.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);
			conn.setRequestProperty(ACCEPT, APPLICATION_JSON);
			if(null!=authHeader && !authHeader.isEmpty())
			{
				conn.setRequestProperty(AUTHORIZATION,authHeader);
			}

			conn.setDoOutput(true);
			try(OutputStream os = conn.getOutputStream()) {
				byte[] input = requestBody.getBytes(UTF8_ENCODING);
				os.write(input, 0, input.length);
				os.flush();
			}catch(Exception e) {
				logger.error("RestInvokerImpl :: invokePostRestService :: Error Occured while writing request to OutStream::"+e);
				throw new RuntimeException("Failed to invoke POST request", e);
			}
			// added 201,204 status codes for PEGA calls
			if (conn.getResponseCode() != 200 && conn.getResponseCode() != 201 && conn.getResponseCode() != 204) {


				logger.error(new StringBuilder()
						.append("REST Call Error").append(StringPool.BLANK)
						.append(EXCEPTION_REST_SERVICE_CALL_MSG).append(conn.getResponseCode())
						.append(StringPool.PIPE).append(conn.getResponseMessage()));
				throw new PortalException("Failed to invoke POST request for the code " + conn.getResponseCode());

			}else {
				response=  readResponse(conn.getInputStream(), jsonBuffer);
			}

		}catch(Exception e) {
			logger.error("RestInvokerImpl :: invokePostRestService :: Error Occured:Exception::"+e);
			e.printStackTrace();
		}
		return response;
	}

	public String readResponse(InputStream in, StringBuilder jsonBuffer)  {
		String output = null;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(in, UTF8_ENCODING))) {
			while ((output = br.readLine()) != null) {
				jsonBuffer.append(output.trim());
			}
		} catch (Exception e) {
			logger.error(
					"RestInvokerImpl :: invokePostRestService :: Error Occured while reading response from inputstream::"
							+ e);
			e.printStackTrace();
		}
		return jsonBuffer.toString();
	}

	@Override
	public String invokePostRestServiceHttps(String url, String requestBody,String authHeader, String urlParameters, Map<String,String> requestHeaderAttributes)  {
		Long startTime =System.currentTimeMillis();
		HttpsURLConnection conn = null;
		String output = null;
		String response = null;

		URL endpointURL = null;
		try {
			if(null!=urlParameters && !urlParameters.isEmpty()) {
				endpointURL = new URL(url+StringPool.QUESTION+urlParameters.replace(StringPool.BLANK, SPACE_REPLACEMENT_CHAR));
			}else {
				endpointURL = new URL(url);
			}

			conn = (HttpsURLConnection) endpointURL.openConnection();

			conn.setRequestMethod(HTTP_POST_METHOD);
			conn.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);
			conn.setRequestProperty(ACCEPT, APPLICATION_JSON);
			if(null!=requestHeaderAttributes && requestHeaderAttributes.size()>0)
			{
				for(Map.Entry<String, String> entry: requestHeaderAttributes.entrySet()) {
					conn.setRequestProperty(entry.getKey(), entry.getValue());
				}
			}
			if(null!=authHeader && !authHeader.isEmpty()) {
				conn.setRequestProperty(AUTHORIZATION,authHeader);
			}


			conn.setDoOutput(true);
			try(OutputStream os = conn.getOutputStream()) {
				byte[] input = requestBody.getBytes(UTF8_ENCODING);
				os.write(input, 0, input.length);
				os.flush();
			}catch(Exception e) {
				logger.error("RestInvokerImpl :: invokePostRestServiceHttps :: Error Occured while writing request to OutStream::"+e);
				e.printStackTrace();
			}
			if (conn.getResponseCode() != 200 && conn.getResponseCode() != 201)  {
				logger.error(" The Post request call is failed with responseCode "+ conn.getResponseCode());
			}else {
				response = populateJsonBuffer(conn,output);
			}

		}catch(Exception e) {
			logger.error("RestInvokerImpl :: invokePostRestServiceHttps :: Error Occured:Exception::"+e);
			e.printStackTrace();
		}
		return response;
	}

	@Override
	public String invokePutRestServiceHttps(String url, String requestBody, String authHeader, String urlParameters)
	{
		Long startTime =System.currentTimeMillis();
		HttpsURLConnection conn = null;
		String output = null;
		String response = null;

		URL endpointURL = null;
		try {
			endpointURL = new URL(url);
			conn = (HttpsURLConnection) endpointURL.openConnection();

			conn.setRequestMethod(HTTP_PUT_METHOD);
			conn.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);
			conn.setRequestProperty(ACCEPT, APPLICATION_JSON);
			if(null!=authHeader && !authHeader.isEmpty()) {
				conn.setRequestProperty(AUTHORIZATION,authHeader);
			}

			conn.setDoOutput(true);
			try(OutputStream os = conn.getOutputStream()) {
				byte[] input = requestBody.getBytes(UTF8_ENCODING);
				os.write(input, 0, input.length);
				os.flush();
			}catch(Exception e) {
				logger.error("RestInvokerImpl :: invokePutRestServiceHttps :: Error Occured while writing request to OutStream::"+e);
				e.printStackTrace();
			}
			if (conn.getResponseCode() != 200 && conn.getResponseCode() != 201) {

				logger.error(" The Put request call is failed with responseCode "+ conn.getResponseCode());
			}else {

				response = populateJsonBuffer(conn,output);
			}

		}catch(Exception e) {
			logger.error("RestInvokerImpl :: invokePutRestServiceHttps :: Error Occured:Exception::"+e);
			e.printStackTrace();
		}
		return response;
	}

	@Override
	public String invokePutRestService(String url, String requestBody, String authHeader, String urlParameters)
	{
		Long startTime =System.currentTimeMillis();
		HttpURLConnection conn = null;
		String response = null;
		StringBuilder jsonBuffer = new StringBuilder();
		URL endpointURL = null;
		try {
			endpointURL = new URL(url);

			conn = (HttpURLConnection) endpointURL.openConnection();

			conn.setRequestMethod(HTTP_PUT_METHOD);
			conn.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);
			conn.setRequestProperty(ACCEPT, APPLICATION_JSON);
			if(null!=authHeader && !authHeader.isEmpty()) {
				conn.setRequestProperty(AUTHORIZATION,authHeader);
			}

			conn.setDoOutput(true);
			try(OutputStream os = conn.getOutputStream()) {
				byte[] input = requestBody.getBytes(UTF8_ENCODING);
				os.write(input, 0, input.length);
				os.flush();
			}catch(Exception e) {
				logger.error("RestInvokerImpl :: invokePutRestService :: Error Occured while writing request to OutStream::"+e);
				e.printStackTrace();
			}
			if (conn.getResponseCode() != 200 && conn.getResponseCode() != 201) {
				logger.error(" The Put request call is failed with responseCode "+ conn.getResponseCode());

			}else {
				response=  readResponse(conn.getInputStream(), jsonBuffer);
			}

		}catch(Exception e) {
			logger.error("RestInvokerImpl :: invokePutRestService :: Error Occured:Exception::"+e);
			e.printStackTrace();
		}
		return response;

	}

	private String populateJsonBuffer(HttpURLConnection conn,String output)
	{
		StringBuilder jsonBuffer = new StringBuilder();
		String response=null;
		try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), UTF8_ENCODING))) {
			while ((output = br.readLine()) != null) {
				jsonBuffer.append(output.trim());
			}
		}catch(Exception e) {
			logger.error("RestInvokerImpl :: populateJsonBuffer :: Error Occured while reading response from inputstream::"+e);
			e.printStackTrace();
		}
		response=jsonBuffer.toString();
		return response;
	}

	@Override
	public String invokeDeleteFileRestServiceHttps(String url, String queryString, String requestBody, String authHeader,
												   Map<String, String> requestHeaderAttributes) {
		Long startTime =System.currentTimeMillis();
		HttpsURLConnection conn = null;
		String output = null;
		String response = null;
		StringBuilder jsonBuffer = new StringBuilder();
		URL endpointURL = null;

		try {
			if(null!=queryString && !queryString.isEmpty()) {
				endpointURL = new URL(url+StringPool.QUESTION+queryString.replace(StringPool.BLANK, SPACE_REPLACEMENT_CHAR));
			}else {
				endpointURL = new URL(url);
			}


			conn = (HttpsURLConnection) endpointURL.openConnection();


			conn.setRequestMethod(HTTP_DELETE_METHOD);
			conn.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);


			if(null!=requestHeaderAttributes && requestHeaderAttributes.size()>0)
			{
				for(String key: requestHeaderAttributes.keySet()) {
					conn.setRequestProperty(key, requestHeaderAttributes.get(key));
				}
			}
			if(null!=authHeader && !authHeader.isEmpty())
			{
				conn.setRequestProperty(AUTHORIZATION,authHeader);
			}

			conn.setDoOutput(true);
			try(OutputStream os = conn.getOutputStream()) {
				byte[] input = requestBody.getBytes(UTF8_ENCODING);
				os.write(input, 0, input.length);
				os.flush();
			}catch(Exception e) {
				logger.error("RestInvokerImpl :: invokePostRestService :: Error Occured while writing request to OutStream::"+e);
				e.printStackTrace();
			}

			if (conn.getResponseCode() != 200 && conn.getResponseCode() != 201) {
				logger.error(" The Delete request call is failed with responseCode "+ conn.getResponseCode());
			}else {
				response = populateJsonBuffer(conn, output);
			}

		} catch (Exception e) {
			logger.error("RestInvokerImpl :: invokeDeleteRestServiceHttps :: Error Occured:Exception::"+e.getMessage());
			e.printStackTrace();
		} finally {

			if (null != conn) {
				conn.disconnect();
			}
		}
		return response;
	}
}