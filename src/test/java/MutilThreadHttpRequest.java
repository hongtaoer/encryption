import org.junit.Test;

/**
 * Created by pengtao on 7/17/16.
 */
public class MutilThreadHttpRequest {


    private final static String URL = "http://www.97hyg.com/member/user/sendcode";


    @Test
    public void execute() throws InterruptedException {

        for (int i = 0; i < 200; i++) {
            new RequestThread().start();
        }

        Thread.sleep(1000000000);

    }

}
