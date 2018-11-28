/**
 *  HttpUtils.java
 *  Date：12-6-27
 *  Time: 上午9:19
 *  Copyright 众邦金控科技有限公司 2012 版权所有
 */
package com.zbjk.crc.utils.http;

import com.zbjk.crc.utils.exception.NetServiceException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * httpclient工具类
 *
 * @author zhangchanghong
 */
public class HttpUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);
    private static final String CHARSET_UTF8 = "UTF-8";
    private static final String CHARSET_GBK = "GBK";
    private static final String SSL_DEFAULT_SCHEME = "https";
    private static final int SSL_DEFAULT_PORT = 443;
    private static final int DEFAULTTIMEOUT=60;

    // 异常自动恢复处理，使用httpRequestRetryHandler接口实现请求异常恢复
    private static HttpRequestRetryHandler requestRetryHandler = new HttpRequestRetryHandler() {
        // 自定义恢复策略
        @Override
        public boolean retryRequest(IOException e, int i, HttpContext httpContext) {
            // 设置恢复策略，在发生异常时将自动重试3次
            if (i >=3) {
                return false;
            }
            if (e instanceof NoHttpResponseException) {
                return true;
            }
            if (e instanceof SSLHandshakeException) {
                return false;
            }
            HttpRequest request = (HttpRequest) httpContext.getAttribute(ExecutionContext.HTTP_REQUEST);
            boolean idempotent = (request instanceof HttpEntityEnclosingRequest);
            if (!idempotent) {
                return true;
            }
            return false;
        }
    };

    // 使用ResponseHandler接口处理响应，HttpClient使用ResponseHandler会自动管理连接的释放，解决了对连接释放管理
    private static ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
        // 自定义响应处理
        @Override
        public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String charset = EntityUtils.getContentCharSet(entity) == null ? CHARSET_UTF8 : EntityUtils.getContentCharSet(entity);
                return new String(EntityUtils.toByteArray(entity),charset);
            }
            return null;
        }
    };

    /**
     * Get方式提交，URL中包含查询参数，格式：http://www.x.cn?search=p&n=s
     */
    public static String get(String url) throws NetServiceException {
        return get(url,null,null);
    }

    /**
     * Get方式提交，URL中不包含查询参数，格式：http://www.x.cn
     */
    public static String get(String url,Map<String,String> params) throws NetServiceException {
        return get(url,params,null);
    }

    /**
     * Get方式提交，URL中不包含查询参数，格式：http://www.x.cn
     */
    public static String get(String url,Map<String,String> params,String charset) throws NetServiceException {
        if (url == null || StringUtils.isEmpty(url)) {
            return null;
        }
        List<NameValuePair> qparams = getParamsList(params);
        if (qparams != null && qparams.size() > 0) {
            charset = charset == null ? CHARSET_UTF8 : charset;
            String formatParams = URLEncodedUtils.format(qparams,charset);
            url = (url.indexOf("?")) < 0 ? (url + "?" + formatParams) : (url.substring(0,url.indexOf("?") + 1) + formatParams);
        }
        DefaultHttpClient httpClient = getDefaultHttpClient(charset);
        HttpGet hg = new HttpGet(url);
        // 发送请求
        String responseStr = null;
        try {
            responseStr = httpClient.execute(hg,responseHandler);
        } catch (ClientProtocolException e) {
            throw new NetServiceException("客户端连接协议错误",e);
        } catch (IOException e) {
            throw new NetServiceException("IO操作异常",e);
        } finally {
            abortConnection(hg,httpClient);
        }
        return responseStr;
    }

    /**
     * Post方式提交，URL中不包含提交参数，格式：http:www.x.cn
     */
    public static String post(String url,Map<String,String> params) throws NetServiceException {
        return post(url,params,null);
    }

    public static String post(String url,Map<String,String> params,String charset) throws NetServiceException {
        if (url == null || StringUtils.isEmpty(url)) {
            return null;
        }
        // 创建HttpClient实例
        DefaultHttpClient httpClient = getSecuredHttpClient(getDefaultHttpClient(charset));
        UrlEncodedFormEntity formEntity = null;
        HttpPost hp = new HttpPost(url);
        if(params != null){
            try {
                if (charset == null || StringUtils.isEmpty(charset)) {
                    formEntity = new UrlEncodedFormEntity(getParamsList(params),CHARSET_UTF8);
                } else {
                    formEntity = new UrlEncodedFormEntity(getParamsList(params),charset);
                }
            } catch (UnsupportedEncodingException e) {
                throw new NetServiceException("不支持的编码集",e);
            }
            hp.setEntity(formEntity);
        }
        // 发送请求
        String responseStr = null;
        try {
            responseStr = httpClient.execute(hp,responseHandler);
        } catch (ClientProtocolException e) {
            throw new NetServiceException("客户端连接协议错误",e);
        } catch (IOException e) {
            throw new NetServiceException("IO操作异常",e);
        } finally {
            abortConnection(hp,httpClient);
        }
        return responseStr;
    }
    
    private static DefaultHttpClient getSecuredHttpClient(HttpClient httpClient) {
        final X509Certificate[] _AcceptedIssuers = new X509Certificate[] {};
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return _AcceptedIssuers;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain,
                        String authType) throws CertificateException {
                }
            };
            ctx.init(null, new TrustManager[] { tm }, new SecureRandom());
            SSLSocketFactory ssf = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ClientConnectionManager ccm = httpClient.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", 443, ssf));
            return new DefaultHttpClient(ccm, httpClient.getParams());
        } catch (Exception e) {
        }
        return null;
    }
    
    public static String postUsingXml(String postUrl, String xmlParamsString, String charset) {
        String retStr = null;
        try {
            URL url = new URL(postUrl);
            HttpURLConnection connection = null;
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("content-type", "text/xml; charset=" + charset);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            OutputStream out = null;
            out = connection.getOutputStream();
            String reqData = null;
            reqData = xmlParamsString;
            out.write(reqData.getBytes(charset));
            int len = connection.getContentLength();
            byte[] retData;
            retData = HttpUtils.readContent(connection.getInputStream(), len);
            retStr = new String(retData, charset);
        } catch (UnsupportedEncodingException e1) {
        } catch (IOException e1) {
        }
        return retStr;
    }
    
    /**
     * 发送HTTP_POST_SSL请求
     * @see 1)该方法会自动关闭连接,释放资源
     * @see 2)该方法亦可处理普通的HTTP_POST请求
     * @see 3)方法内设置了连接和读取超时时间,单位为毫秒,超时后方法会自动返回空字符串
     * @see 4)请求参数含中文等特殊字符时,可在传入前自行<code>URLEncoder.encode(string,encodeCharset)</code>,再指定encodeCharset为null
     * @see 5)亦可指定encodeCharset参数值,由本方法代为编码
     * @see 6)该方法在解码响应报文时所采用的编码,为响应消息头中[Content-Type:text/html; charset=GBK]的charset值
     * @see   若响应消息头中未指定Content-Type属性,则默认使用HttpClient内部默认的ISO-8859-1
     * @see 7)若reqURL的HTTPS端口不是默认的443,那么只需要在reqURL中指明其实际的HTTPS端口即可,而无需修改本方法内部指定的443
     * @see   此时,方法默认会取指明的HTTPS端口进行连接.如reqURL=https://123.125.97.66:8085/pay/则实际请求即对方HTTPS_8085端口
     * @param reqURL        请求地址
     * @param params        请求参数
     * @param encodeCharset 编码字符集,编码请求数据时用之,其为null表示请求参数已编码完毕,不需要二次编码
     * @return 远程主机响应正文
     */
    public static String sendPostSSLRequest(String reqURL, Map<String, String> params, String encodeCharset){
        String responseContent = "";
        HttpClient httpClient = new DefaultHttpClient();
        //设置代理服务器
        //httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost("10.0.0.4", 8080));
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 1000);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 2000);
        //创建TrustManager
        //用于解决javax.net.ssl.SSLPeerUnverifiedException: peer not authenticated
        X509TrustManager trustManager = new X509TrustManager(){
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            @Override
            public X509Certificate[] getAcceptedIssuers() {return null;}
        };
        //创建HostnameVerifier
        //用于解决javax.net.ssl.SSLException: hostname in certificate didn't match: <123.125.97.66> != <123.125.97.241>
        X509HostnameVerifier hostnameVerifier = new X509HostnameVerifier(){
            @Override
            public void verify(String host, SSLSocket ssl) throws IOException {}
            @Override
            public void verify(String host, X509Certificate cert) throws SSLException {}
            @Override
            public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {}
            @Override
            public boolean verify(String arg0, SSLSession arg1) {return true;}
        };
        try {
            //TLS1.0与SSL3.0基本上没有太大的差别,可粗略理解为TLS是SSL的继承者，但它们使用的是相同的SSLContext
            SSLContext sslContext = SSLContext.getInstance(SSLSocketFactory.TLS);
            //使用TrustManager来初始化该上下文,TrustManager只是被SSL的Socket所使用
            sslContext.init(null, new TrustManager[]{trustManager}, null);
            //创建SSLSocketFactory
            SSLSocketFactory socketFactory = new SSLSocketFactory(sslContext, hostnameVerifier);
            //通过SchemeRegistry将SSLSocketFactory注册到HttpClient上
            httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, socketFactory));
            //创建HttpPost
            HttpPost httpPost = new HttpPost(reqURL);
            //httpPost.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8");
            //构建POST请求的表单参数
            if(null != params){
                List<NameValuePair> formParams = new ArrayList<NameValuePair>();
                for(Map.Entry<String,String> entry : params.entrySet()){
                    formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                httpPost.setEntity(new UrlEncodedFormEntity(formParams, encodeCharset));
            }
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (null != entity) {
//                //打印HTTP响应报文的第一行,即状态行
//                System.out.println(response.getStatusLine());
//                //打印HTTP响应头信息
//                for(Header header : response.getAllHeaders()){
//                    System.out.println(header.toString());
//                }
                responseContent = EntityUtils.toString(entity, ContentType.getOrDefault(entity).getCharset());
                EntityUtils.consume(entity);
            }
        } catch (ConnectTimeoutException cte){
            //Should catch ConnectTimeoutException, and don`t catch org.apache.http.conn.HttpHostConnectException
            LOGGER.error("与[" + reqURL + "]连接超时,自动返回空字符串");
        } catch (SocketTimeoutException ste){
            LOGGER.error("与[" + reqURL + "]读取超时,自动返回空字符串");
        } catch (Exception e) {
            LOGGER.error("与[" + reqURL + "]通信异常,堆栈信息为", e);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return responseContent;
    }

    public static KeyStore getKeyStore(String password,String keyStorePath) throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        FileInputStream is = new FileInputStream(keyStorePath);
        ks.load(is,password.toCharArray());
        is.close();
        return ks;
    }

    public static void initHttpsURLConnection(String password,String keyStorePath,String trustStorePath) throws Exception {
        SSLContext sslContext = null;
        HostnameVerifier hnv = new HostnameVerifier() {
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        };
        try{
            sslContext = getSSLContext(password,keyStorePath,trustStorePath);
        } catch (GeneralSecurityException e) {
        }
        if (sslContext != null) {
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        }
        HttpsURLConnection.setDefaultHostnameVerifier(hnv);
    }

    private static SSLContext getSSLContext(String password,String keyStorePath,String trustStorePath) throws Exception {
        // 实例化密钥库
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        // 获得密码库
        KeyStore keyStore = getKeyStore(password,keyStorePath);
        // 初始化密钥工厂
        keyManagerFactory.init(keyStore,password.toCharArray());
        // 实例化信任库
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        // 获得信任库
        KeyStore trustStore = getKeyStore(password,trustStorePath);
        // 初始化信任库
        trustManagerFactory.init(trustStore);
        // 实例化SSL上下文
        SSLContext ctx = SSLContext.getInstance("TLS");
        // 初始化SSL上下文
        ctx.init(keyManagerFactory.getKeyManagers(),trustManagerFactory.getTrustManagers(),null);
        return ctx;
    }

    public static String postResp(String url,String content) {
        HttpsURLConnection urlCon = null;
        String xmlResponse = "";
        try {
            urlCon = (HttpsURLConnection)(new URL(url)).openConnection();
            urlCon.setDoOutput(true);
            urlCon.setDoInput(true);
            urlCon.setRequestMethod("PUT");
//            urlCon.setReadTimeout(4000); //
            urlCon.setRequestProperty("Content-Length",String.valueOf(content.getBytes().length));
            urlCon.setUseCaches(false);
            urlCon.getOutputStream().write(content.getBytes("utf-8"));
            urlCon.getOutputStream().flush();
            urlCon.getOutputStream().close();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlCon.getInputStream(),"UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                xmlResponse = xmlResponse + line;
            }
            xmlResponse = new String(xmlResponse.getBytes("ISO-8859-1"),"UTF-8");
        } catch (Exception e) {
        } finally {
            urlCon.disconnect();
        }
        return xmlResponse;
    }

    /**
     * Post方式提交，忽略URL中包含的参数，解决SSL双向数字证书认证
     */
    public static String post(String url,Map<String,String> params,String charset,final URL keystoreUrl,
                              final String keystorePass,final URL truststoreUrl,final String truststorePass) throws NetServiceException {
        if (url == null || StringUtils.isEmpty(url)) {
            return null;
        }
        DefaultHttpClient httpClient = getDefaultHttpClient(charset);
        UrlEncodedFormEntity formEntity = null;
        try {
            if (charset == null || StringUtils.isEmpty(charset)) {
                formEntity = new UrlEncodedFormEntity(getParamsList(params),CHARSET_UTF8);
            } else {
                formEntity = new UrlEncodedFormEntity(getParamsList(params),charset);
            }
        } catch (UnsupportedEncodingException e) {
            throw new NetServiceException("不支持的编码集",e);
        }
        HttpPost hp = null;
        String responseStr = null;
        try {
            KeyStore keyStore = createKeyStore(keystoreUrl,keystorePass);
            KeyStore trustStore = createKeyStore(truststoreUrl,truststorePass);
            SSLSocketFactory socketFactory = new SSLSocketFactory(keyStore,keystorePass,trustStore);
            Scheme scheme = new Scheme(SSL_DEFAULT_SCHEME,socketFactory,SSL_DEFAULT_PORT);
            httpClient.getConnectionManager().getSchemeRegistry().register(scheme);
            hp = new HttpPost(url);
            hp.setEntity(formEntity);
            responseStr = httpClient.execute(hp,responseHandler);
        } catch (NoSuchAlgorithmException e) {
            throw new NetServiceException("指定的加密算法不可用",e);
        } catch (KeyStoreException e) {
            throw new NetServiceException("keystore解析异常",e);
        } catch (CertificateException e) {
            throw new NetServiceException("信任证书过期或解析异常",e);
        } catch (FileNotFoundException e) {
            throw new NetServiceException("keystore文件不存在",e);
        } catch (IOException e) {
            throw new NetServiceException("IO操作失败或中断",e);
        } catch (UnrecoverableKeyException e) {
            throw new NetServiceException("keystore中的密钥无法恢复异常",e);
        } catch (KeyManagementException e) {
            throw new NetServiceException("处理密钥管理的操作异常",e);
        } finally {
            abortConnection(hp,httpClient);
        }
        return responseStr;
    }

    /**
     * 猎取DefaultHttpClient实例
     */
    private static DefaultHttpClient getDefaultHttpClient(final String charset) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION,HttpVersion.HTTP_1_1);
        // 模拟浏览器，解决一些服务器程序只允许浏览器访问问题
        httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");
        httpClient.getParams().setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, Boolean.FALSE);
        httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, charset == null ? CHARSET_GBK : charset);
        httpClient.setHttpRequestRetryHandler(requestRetryHandler);
        return httpClient;
    }

    /**
     * 释放HttpClient连接
     */
    private static void abortConnection(final HttpRequestBase hrb, final HttpClient httpclient){
        if (hrb != null) {
            hrb.abort();
        }
        if (httpclient != null) {
            httpclient.getConnectionManager().shutdown();
        }
    }

    /**
     * 从给定的路径中加载此 KeyStore
     */
    private static KeyStore createKeyStore(final URL url, final String password)
            throws KeyStoreException, NoSuchAlgorithmException,CertificateException, IOException {
        if (url == null) {
            throw new IllegalArgumentException("Keystore url may not be null");
        }
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream is = null;
        try {
            is = url.openStream();
            keystore.load(is, password != null ? password.toCharArray() : null);
        } finally {
            if (is != null){
                is.close();
                is = null;
            }
        }
        return keystore;
    }

    /**
     * 将传入的键/值对参数转换为NameValuePair参数集
     */
    private static List<NameValuePair> getParamsList(Map<String, String> paramsMap) {
        if (paramsMap == null || paramsMap.size() == 0) {
            return null;
        }
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> map : paramsMap.entrySet()) {
            params.add(new BasicNameValuePair(map.getKey(), map.getValue()));
        }
        return params;
    }

    public static void main(String[] args) throws NetServiceException {
        //{"sign":"5fe58414b2287e53d4ad6505bdb909f8","payResult":"0","transId":"1072824-322095-nj","refNum":"ZF0000119764"}

        Map<String,String> map = new HashMap<String, String>();
        map.put("sign", "5fe58414b2287e53d4ad6505bdb909f8");
        map.put("payResult", "0");
        map.put("transId", "1072824-322095-nj");
        map.put("refNum", "ZF0000119764");
        for (int i=0;i<100;i++) {
            String s = HttpUtils.post("http://fmis8.zbjk.com/notify_pay_njcb.php", map);
            if (s == null || !s.equals("0") || s.equals("null")) {
                System.out.println(i + " -> " + s);
            }
        }
    }
    
    private static byte[] readContent(InputStream in, int len) throws IOException {
        if (len <= 0) {
            byte[] buf = new byte[2048];
            byte[] readBuf = new byte[1024];
            int totalLen = 0;
            while (true) {
                int readLen = in.read(readBuf);
                if (readLen <= 0) {
                    break;
                }
                if (totalLen + readLen > buf.length) {
                    byte[] tmp = new byte[buf.length + readLen + 1024];
                    System.arraycopy(buf, 0, tmp, 0, totalLen);
                    buf = tmp;
                }
                System.arraycopy(readBuf, 0, buf, totalLen, readLen);
                totalLen = totalLen + readLen;
            }
            byte[] retBuf = new byte[totalLen];
            System.arraycopy(buf, 0, retBuf, 0, totalLen);
            return retBuf;
        } else {
            int totalLen = 0;
            byte[] buf = new byte[len];
            while (totalLen < len) {
                int readLen = in.read(buf, totalLen, len - totalLen);
                if (readLen <= 0) {
                    break;
                }
                totalLen = totalLen + readLen;
            }
            return buf;
        }
    }
    
}
