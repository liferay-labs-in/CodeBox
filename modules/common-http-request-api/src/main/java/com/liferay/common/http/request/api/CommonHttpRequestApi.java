package com.liferay.common.http.request.api;

import java.util.Map;

/**
 * @author Payal
 */
public interface CommonHttpRequestApi {

    String invokeGetRestServiceHttps(String url, String requestBody, String urlParameters,
                                     Map<String, String> requestHeaderAttributes);
    <T> T invoketHttpsGet(String url, String urlParameters, Class<T> responseType,
                          Map<String, String> requestHeaderAttributes);
    String invokePostRestService(String url, String requestBody,String authHeader, String urlParameters);

    String invokePostRestServiceHttps(String url, String requestBody,String authHeader, String urlParameters, Map<String,String> requestHeaderAttributes);

    String invokePutRestService(String url, String requestBody, String authHeader, String urlParameters);

    String invokePutRestServiceHttps(String url, String requestBody, String authHeader, String urlParameters);

    String invokeDeleteFileRestServiceHttps(String url, String queryString, String requestBody, String authHeader,
                                            Map<String, String> requestHeaderAttributes);

}