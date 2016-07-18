import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by pengtao on 7/17/16.
 */
public class RequestThread extends Thread {

    private final static String URL = "http://www.97hyg.com/member/user/sendcode";
    public void  run(){
        while (true){
            try {
                Thread.sleep(20);
                HttpClient client = new HttpClient();
                PostMethod method = new PostMethod(URL);

                method.setRequestHeader("Content-Type","application/x-www-form-urlencoded;charset=gbk");
                method.setRequestBody(new NameValuePair[]{new NameValuePair("name","18602108194")});
                client.executeMethod(method);

                StringBuffer response = new StringBuffer();

                if (method.getStatusCode() == HttpStatus.SC_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(method.getResponseBodyAsStream(),
                                    "utf-8"));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        boolean pretty =true;
                        if (pretty)
                            response.append(line).append(
                                    System.getProperty("line.separator"));
                        else
                            response.append(line);
                    }
                    reader.close();
                }

                System.out.println(Thread.currentThread().getName() + "-response====" + response);


            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (HttpException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
