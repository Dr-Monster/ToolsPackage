package com.zbjk.crc.utils.http;

import com.zbjk.crc.utils.JsonUtil;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HttpClientUtils {
    private static final Logger LOGGER = Logger.getLogger(HttpClientUtils.class);
    private static HttpClient httpClient;
    private static HttpClient jsonHttpClient;

    @Value("${fbi.wcs.proxy.ip}")
    private static String proxyIp;
    @Value("${fbi.wcs.proxy.port}")
    private static Integer proxyPort;

    static {
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setConnectionTimeout(36000000);
        connectionManager.getParams().setSoTimeout(36000000);
        connectionManager.getParams().setMaxTotalConnections(2000);
        connectionManager.getParams().setDefaultMaxConnectionsPerHost(500);
        httpClient = new HttpClient(connectionManager);
        jsonHttpClient = new HttpClient(connectionManager);
    }

    public static String executePostHttpClientUTF(String url, Map<String, String> contentMap) {
        PostMethod method = new PostMethod(url);
        try {
            NameValuePair[] parameters = new NameValuePair[contentMap.size()];
            int i = 0;
            for (String key : contentMap.keySet()) {
                parameters[i] = new NameValuePair();
                parameters[i].setName(key);
                parameters[i].setValue(contentMap.get(key));
                i++;
            }
            method.addParameters(parameters);
            method.getParams().setContentCharset("utf-8");
            httpClient.executeMethod(method);
            return IOUtils.toString(method.getResponseBodyAsStream(), "utf-8");
        } catch (Exception e) {
            LOGGER.error("execute  httpClient failed", e);
            return null;
        } finally {
            method.releaseConnection();
        }
    }

    public static String executePostHttpClientListUTF(String url, List<Object> list) {
        PostMethod method = new PostMethod(url);
        try {
            Map<String, String> contentMap = (HashMap) list.get(0);
            NameValuePair[] parameters = new NameValuePair[list.size()];
            int i = 0;
            for (String key : contentMap.keySet()) {
                parameters[i] = new NameValuePair();
                parameters[i].setName(key);
                parameters[i].setValue(contentMap.get(key));
                i++;
            }
            method.addParameters(parameters);
            method.getParams().setContentCharset("utf-8");
            httpClient.executeMethod(method);
            return IOUtils.toString(method.getResponseBodyAsStream(), "utf-8");
        } catch (Exception e) {
            LOGGER.error("execute  httpClient failed", e);
            return null;
        } finally {
            method.releaseConnection();
        }
    }

    public static String executePostHttpClientGBK(String url, Map<String, String> contentMap) {
        PostMethod method = new PostMethod(url);
        try {
            NameValuePair[] parameters = new NameValuePair[contentMap.size()];
            int i = 0;
            for (String key : contentMap.keySet()) {
                parameters[i] = new NameValuePair();
                parameters[i].setName(key);
                parameters[i].setValue(contentMap.get(key));
                i++;
            }
            method.addParameters(parameters);
            method.getParams().setContentCharset("utf-8");
            httpClient.executeMethod(method);
            return IOUtils.toString(method.getResponseBodyAsStream(), "gbk");
        } catch (Exception e) {
            LOGGER.error("execute  httpClient failed", e);
            return null;
        } finally {
            method.releaseConnection();
        }
    }

    public static String executeJsonHttpClientUTF(String url, String jsonData) {
        PostMethod method = new PostMethod(url);
        InputStream inputStream = null;
        jsonHttpClient.setTimeout(10*60*1000);
        jsonHttpClient.setConnectionTimeout(10*60*1000);
        try {
            StringRequestEntity requestEntity = new StringRequestEntity(jsonData, "application/json", "UTF-8");
            method.setRequestEntity(requestEntity);
            jsonHttpClient.executeMethod(method);
            inputStream = method.getResponseBodyAsStream();
            if (inputStream != null) {
                return IOUtils.toString(inputStream, "utf-8");
            } else {
                return null;
            }
//            return IOUtils.toString(method.getResponseBodyAsStream(), "utf-8");
        } catch (Exception e) {
            LOGGER.error("execute  httpClient failed", e);
            return null;
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            method.releaseConnection();
        }
    }




    public static String executeJsonHttpClientUTF(String url, Object jsonData) {
        String jsonDataStr = JsonUtil.toString(jsonData);
        return executeJsonHttpClientUTF(url, jsonDataStr);
    }

    public static String executePostHttpClient(String url, Map<String, String> contentMap, String charset) {
        PostMethod method = new PostMethod(url);
        try {
            NameValuePair[] parameters = new NameValuePair[contentMap.size()];
            int i = 0;
            for (String key : contentMap.keySet()) {
                parameters[i] = new NameValuePair();
                parameters[i].setName(key);
                parameters[i].setValue(URLEncoder.encode(contentMap.get(key), "utf-8"));
                i++;
            }
            method.addParameters(parameters);
            method.getParams().setContentCharset(charset);
            httpClient.executeMethod(method);
            return IOUtils.toString(method.getResponseBodyAsStream(), charset);
        } catch (Exception e) {
            LOGGER.error("execute  httpClient failed", e);
            return null;
        } finally {
            method.releaseConnection();
        }
    }

    public static String executeGetHttpClient(String url, Map<String, String> contentMap) {
        GetMethod method = null ;
        if(contentMap != null){
            StringBuffer buffer = new StringBuffer();
            int i = 0;
            for (String key : contentMap.keySet()) {
                if (i == 0) {
                    buffer.append("?").append(key).append("=").append(contentMap.get(key));
                } else {
                    buffer.append("&").append(key).append("=").append(contentMap.get(key));
                }
                i++;
            }
            method = new GetMethod(url + buffer.toString());
        }else{
            method = new GetMethod(url);
        }
        try {
            httpClient.executeMethod(method);
            return IOUtils.toString(method.getResponseBodyAsStream(), "utf-8");
        } catch (Exception e) {
            LOGGER.error("execute  httpClient failed", e);
            return null;
        } finally {
            method.releaseConnection();
        }
    }

    public static Header[] executeGetHttpClientHeaders(String url, Map<String, String> contentMap) {
        StringBuffer buffer = new StringBuffer();
        int i = 0;
        for (String key : contentMap.keySet()) {
            if (i == 0) {
                buffer.append("?").append(key).append("=").append(contentMap.get(key));
            } else {
                buffer.append("&").append(key).append("=").append(contentMap.get(key));
            }
            i++;
        }
        GetMethod method = new GetMethod(url + buffer.toString());
        try {
            httpClient.executeMethod(method);
            return (Header[]) method.getResponseHeaders();
        } catch (Exception e) {
            LOGGER.error("execute  httpClient failed", e);
            return null;
        } finally {
            method.releaseConnection();
        }

    }

    public static Integer executePostHttpClientStatusCode(String url, Map<String, String> contentMap, String charset) {
        PostMethod method = new PostMethod(url);
        try {
            NameValuePair[] parameters = new NameValuePair[contentMap.size()];
            int i = 0;
            for (String key : contentMap.keySet()) {
                parameters[i] = new NameValuePair();
                parameters[i].setName(key);
                parameters[i].setValue(URLEncoder.encode(contentMap.get(key), "utf-8"));
                i++;
            }
            method.addParameters(parameters);
            method.getParams().setContentCharset(charset);

            httpClient.executeMethod(method);
            return method.getStatusCode();
        } catch (Exception e) {
            LOGGER.error("execute  httpClient failed", e);
            return null;
        } finally {
            method.releaseConnection();
        }
    }

    public static String executeXMLHttpClientGBK(String url, String xmlData) {
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(url);
        try {
            httpClient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "GBK");
            //postMethod.setRequestBody(strSendData);
            StringRequestEntity requestEntity = new StringRequestEntity(xmlData, "application/xml", "GBK");
            postMethod.setRequestEntity(requestEntity);
            int statusCode = httpClient.executeMethod(postMethod);
            if (statusCode != HttpStatus.SC_OK) {
                byte[] responseBody = postMethod.getResponseBody();
                String strResp = new String(responseBody, "GBK");
                LOGGER.error("execute  xmlhttpClient failed...... {}" + strResp);
                return "fail";
            } else {
                byte[] responseBody = postMethod.getResponseBody();
                return new String(responseBody, "GBK");

            }
        } catch (Exception e) {
            LOGGER.error("execute  executeXMLHttpClientGBK failed", e);
            return "fail";
        } finally {
            postMethod.releaseConnection();
        }
    }

    @PostConstruct
    public void initMethod() {
        if (StringUtils.isNotBlank(proxyIp)) {
            httpClient.getHostConfiguration().setProxy(proxyIp, proxyPort);
            httpClient.getParams().setAuthenticationPreemptive(true);
        }
    }

    public static String tdPostMethod(String apiUrl , String partner_code , String partner_key , String app_name , Map<String, Object> params) {
        HttpURLConnection conn;
        try {
            String urlString = new StringBuilder().append(apiUrl)
                    .append("?partner_code=").append(partner_code)
                    .append("&partner_key=").append(partner_key)
                    .append("&app_name=").append(app_name).toString();
            URL url = new URL(urlString);
            // 组织请求参数
            StringBuilder postBody = new StringBuilder();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                postBody.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue().toString(),
                        "utf-8")).append("&");
            }

            if (!params.isEmpty()) {
                postBody.deleteCharAt(postBody.length() - 1);
            }

            conn = (HttpURLConnection) url.openConnection();
            // 设置长链接
            conn.setRequestProperty("Connection", "Keep-Alive");
            // 设置连接超时
            conn.setConnectTimeout(1000);
            // 设置读取超时
            conn.setReadTimeout(3000);
            // 提交参数
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.getOutputStream().write(postBody.toString().getBytes("UTF-8"));
            conn.getOutputStream().flush();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                return null;
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line).append("\n");
            }
            return result.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
