package za.co.imqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.rules.ExternalResource;
import org.springframework.util.StringUtils;
import za.co.imqs.libimqs.auth.Permit;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static za.co.imqs.TestUtils.poll;

public class LoginRule extends ExternalResource {

    private Permit permit;
    private String session;

    private String url;
    private String username;
    private String password;
    private TimeUnit unit = TimeUnit.SECONDS;
    private long numUnits = 15;

    public LoginRule withUrl(String url) {
        this.url = url;
        return this;
    }

    public LoginRule withUsername(String username) {
        this.username = username;
        return this;
    }

    public LoginRule withTimeout(TimeUnit unit, long numUnits) {
        this.unit = unit;
        this.numUnits = numUnits;
        return this;
    }

    public LoginRule withPassword(String password) {
        this.password = password;
        return this;
    }


    @Override
    protected void before()  {
        if (StringUtils.isEmpty(url)) throw new IllegalArgumentException("url not specified");
        if (StringUtils.isEmpty(username)) throw new IllegalArgumentException("username not specified");
        if (StringUtils.isEmpty(password)) throw new IllegalArgumentException("password not specified");

        final Object[] l = poll(()-> getAuthSession(url, username, password), TimeUnit.SECONDS, 15);
        this.session = (String)l[0];
        this.permit = (Permit)l[1];
    }

    @Override
    protected void after() {
    }

    public String getSession() {
        return session;
    }

    public Permit getPermit() {
        return permit;
    }

    private Object[] getAuthSession(String url, String username, String password)   {
        try {
            HttpClient client = new HttpClient(new SimpleHttpConnectionManager());
            PostMethod post = new PostMethod(url);
            post.setRequestHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
            client.executeMethod(post);
            if (post.getStatusCode() != 200)
                throw new RuntimeException(String.format("Unable to log in to local IMQS instance with username %s (%s, %s)", username, post.getStatusCode(), new String(post.getResponseBody())));

            return new Object[]{post.getResponseHeader("Set-Cookie").getValue(),new ObjectMapper().readValue(post.getResponseBody(), Permit.class)};
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
