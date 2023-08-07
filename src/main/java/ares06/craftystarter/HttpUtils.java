package ares06.craftystarter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.cert.X509Certificate;

public class HttpUtils {
    public static boolean isServerOnline(String ipAddress, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ipAddress, port), 1000); // Timeout in milliseconds
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static CloseableHttpClient createInsecureHttpClient() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }}, new java.security.SecureRandom());

        return HttpClients.custom()
                .setSslcontext(sslContext)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .setDefaultRequestConfig(RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000).build())
                .build();
    }

    public static String executePostRequest(String url, String token) throws IOException {
        try (CloseableHttpClient httpClient = createInsecureHttpClient()) {
            HttpPost httpPost = new HttpPost(url);

            // Set headers (e.g., Authorization header)
            httpPost.setHeader("Authorization", "Bearer " + token);
            httpPost.setHeader("Content-Type", "application/json");

            // Set request body (if needed)
            String jsonPayload = "{\"key\":\"value\"}"; // Replace with your JSON payload
            StringEntity entity = new StringEntity(jsonPayload);
            httpPost.setEntity(entity);

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                return EntityUtils.toString(responseEntity);
            }
        } catch (Exception e) {
            // Handle the exception
            throw new IOException("An error occurred while executing the POST request.", e);
        }

        return null;
    }
}