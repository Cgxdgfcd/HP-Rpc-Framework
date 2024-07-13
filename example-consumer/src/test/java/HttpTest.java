import cn.hutool.core.io.IORuntimeException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.junit.Test;

public class HttpTest {

    @Test
    public void Test() {
        try (HttpResponse response = HttpRequest.get("http://localhost:8080").execute()) {

        } catch (IORuntimeException e) {
            System.out.println(1);
        }
    }
}
