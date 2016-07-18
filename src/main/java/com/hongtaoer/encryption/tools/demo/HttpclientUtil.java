package com.hongtaoer.encryption.tools.demo;

import com.hongtaoer.encryption.tools.AESTool;
import com.hongtaoer.encryption.tools.SignatureUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by pengtao on 7/17/16.
 */
public class HttpclientUtil {
    protected static final Logger log = Logger.getLogger(HttpclientUtil.class);

    /**
     * 根据传入的uri和参数map拼接成实际uri
     *
     * @param uri
     * @param paraMap
     * @return
     */
    public String buildUri(String uri, Map<String, String> paraMap) {
        StringBuilder sb = new StringBuilder();
        uri = StringUtils.trim(uri);
        uri = StringUtils.removeEnd(uri, "/");
        uri = StringUtils.removeEnd(uri, "?");
        sb.append(uri);
        if (paraMap != null && !paraMap.isEmpty()) {
            sb.append("?");
            Iterator<Map.Entry<String, String>> iterator = paraMap.entrySet()
                    .iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> pair = iterator.next();
                try {
                    String keyString = pair.getKey();
                    String valueString = pair.getValue();
                    sb.append(keyString);
                    sb.append("=");
                    sb.append(valueString);
                    sb.append("&");
                } catch (Exception e) {
                    log.error(e, e);
                }
            }
        }
        return StringUtils.removeEnd(sb.toString(), "&");
    }

    /**
     * Post an xml string to a specific host.
     *
     * @param targetHost
     * @param targetPort
     * @param protocol
     * @param proxyHost
     * @param proxyPort
     * @param proxyUser
     * @param proxyPassword
     * @param uri
     * @param paraMap
     * @param xml
     * @param charset
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public String postXmlString(String targetHost, int targetPort,
                                String protocol, String proxyHost, int proxyPort, String proxyUser,
                                String proxyPassword, String uri, Map<String, String> paraMap,
                                String xml, String charset) throws ClientProtocolException,
            IOException {
        String result = null;
        DefaultHttpClient httpclient = new DefaultHttpClient();
        if (StringUtils.isNotBlank(proxyHost) && proxyPort > 0) {
            // 设置上网代理
            AuthScope authScope = new AuthScope(proxyHost, proxyPort);
            if (StringUtils.isNotBlank(proxyUser)
                    && StringUtils.isNotBlank(proxyPassword)) {
                // 设置上网代理的用户名和密码
                UsernamePasswordCredentials upc = new UsernamePasswordCredentials(
                        proxyUser, proxyPassword);
                httpclient.getCredentialsProvider().setCredentials(authScope,
                        upc);
            }
            HttpHost proxy = new HttpHost(proxyHost, proxyPort);
            httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
                    proxy);
        }
        HttpHost host = new HttpHost(targetHost, targetPort, protocol);
        uri = buildUri(uri, paraMap);
        log.info("post uri: " + uri);
        log.info("post content: " + xml);
        HttpPost post = new HttpPost(uri);
        StringEntity se = new StringEntity(xml,
                StringUtils.isNotBlank(charset) ? charset : "utf-8");
        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                "application/xml"));
        post.setEntity(se);
        HttpResponse response = httpclient.execute(host, post);
        if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
                log.info("post result: " + result);
            }
        } else {
            log.error("post failed, status code: "
                    + response.getStatusLine().getStatusCode());
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        AESTool aes = new AESTool();
        SignatureUtil signatureUtil = new SignatureUtil();
        String appid = "canairport001";
        String token = signatureUtil.findTokenById(appid);
        String key = aes.findKeyById(appid);
        long millis = System.currentTimeMillis();
        String xml = "<dependency><groupId>commons-lang</groupId><artifactId>commons-lang</artifactId><version>2.5</version></dependency>";
        xml = aes.encrypt(xml, key);
        String lol = signatureUtil.digest(xml, "MD5");
        String signature = signatureUtil.generateSignature(appid, token, lol,
                millis);
        log.info("lol: \n" + lol);
        log.info("signature: \n" + signature);
        String uri = "http://127.0.0.1:8080/demo/psginfo.do";
        Map<String, String> paraMap = new HashMap<String, String>();
        paraMap.put("s", signature);
        paraMap.put("a", appid);
        paraMap.put("t", String.valueOf(millis));
        paraMap.put("l", lol);
        paraMap.put("o", "test");
        HttpclientUtil util = new HttpclientUtil();
        try {
            String result = util.postXmlString("127.0.0.1", 8080, "http", null,
                    0, null, null, uri, paraMap, xml, "utf-8");
            result = aes.decrypt(result, key);
            System.out.println(result);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}