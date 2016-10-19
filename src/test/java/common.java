import org.apache.jasper.tagplugins.jstl.core.Url;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by wm on 16/9/29.
 */
public class common extends BaseTest {

    @Test
    public void urlTest() {
        try {
            URL url = new URL("http://localhost:5000/login.html?url=http://localhost:5000/blog/new");

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

}
