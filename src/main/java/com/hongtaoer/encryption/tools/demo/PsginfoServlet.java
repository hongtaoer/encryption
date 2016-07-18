package com.hongtaoer.encryption.tools.demo;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hongtaoer.encryption.tools.AESTool;
import com.hongtaoer.encryption.tools.SignatureUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;



/**
 * Created by pengtao on 7/17/16.
 * Servlet implementation class PsginfoServlet
 */
@WebServlet(urlPatterns = { "/psginfo.do" }, loadOnStartup = 1)
public class PsginfoServlet extends HttpServlet {
    protected static final Logger log = Logger.getLogger(PsginfoServlet.class);
    private static final long serialVersionUID = 6536688299231165548L;

    private SignatureUtil signatureUtil = new SignatureUtil();

    private AESTool aes = new AESTool();

    /**
     * @see HttpServlet#HttpServlet()
     */
    public PsginfoServlet() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        String echostr = request.getParameter("e");
        log.info("echostr before echo: " + echostr);
        String signature = request.getParameter("s");
        String appid = request.getParameter("a");
        String timestamp = request.getParameter("t");
        String lol = request.getParameter("l");
        long millis = Long.valueOf(timestamp);
        // Need to check signature in product mode.
        if (signatureUtil.isValid(signature, appid, lol, millis)) {
            PrintWriter writer = response.getWriter();
            log.info("echostr after echo: " + echostr);
            writer.print(echostr);
            writer.flush();
            writer.close();
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        // Get request parameters.
        String signature = request.getParameter("s");
        String appid = request.getParameter("a");
        String timestamp = request.getParameter("t");
        String lol = request.getParameter("l");
        String operation = request.getParameter("o");
        long millis = Long.valueOf(timestamp);

        // Get xml data.
        String encoding = StringUtils
                .isNotBlank(request.getCharacterEncoding()) ? request
                .getCharacterEncoding() : "utf-8";
        String requestXmlString = getXmlStringFromHttpRequest(request);
        String digest = signatureUtil.digest(requestXmlString, "MD5");

        // Check signature and digest.
        if (StringUtils.equals(digest, lol)) {
            if (signatureUtil.isValid(signature, appid, lol, millis)) {
                try {
                    String key = aes.findKeyById(appid);
                    requestXmlString = aes.decrypt(requestXmlString, key);
                    log.info("received xml data:\n" + requestXmlString);
                    // 校验xml合法性并执行相应动作
                    String responseXmlString = doSomeThing(requestXmlString,
                            operation);
                    responseXmlString = aes.encrypt(responseXmlString, key);
                    log.info("responsed xml data:\n" + responseXmlString);
                    response.setCharacterEncoding(encoding);
                    PrintWriter writer = response.getWriter();
                    writer.print(responseXmlString);
                    writer.flush();
                    writer.close();
                } catch (Exception e) {
                    log.error(e, e);
                }
            } else {
                log.error("invalid signature");
            }
        } else {
            log.error("invalid digest.");
        }
    }

    /**
     * TODO Write your own business here.
     *
     * @param xml
     * @param operation
     * @return
     */
    private String doSomeThing(String xml, String operation) {
        return "done";
    }

    /**
     * Extract xml string form http request.
     *
     * @param request
     * @return
     * @throws IOException
     */
    private String getXmlStringFromHttpRequest(HttpServletRequest request) {
        String requestXmlString = "";
        try {
            InputStream inputStream = request.getInputStream();
            String encoding = StringUtils.isNotBlank(request
                    .getCharacterEncoding()) ? request.getCharacterEncoding()
                    : "utf-8";
            requestXmlString = getXmlStringFromInputStream(inputStream,
                    encoding);
            encoding = null;
            inputStream.close();
            inputStream = null;
        } catch (IOException e) {
            log.error(e, e);
        }

        return requestXmlString;
    }

    /**
     * Extract xml string from the inputStream.
     *
     * @param inputStream
     * @param charsetName
     * @return
     */
    private String getXmlStringFromInputStream(InputStream inputStream,
                                               String charsetName) {
        String resultXmlString = "";
        String tempString = null;
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(
                    inputStream, charsetName));
            tempString = bufferedReader.readLine();
            while (tempString != null) {
                resultXmlString += tempString;
                tempString = bufferedReader.readLine();
            }
            tempString = null;
            bufferedReader.close();
            bufferedReader = null;
        } catch (UnsupportedEncodingException e) {
            log.error(e, e);
        } catch (IOException e) {
            log.error(e, e);
        }
        return StringUtils.trim(resultXmlString);
    }

}