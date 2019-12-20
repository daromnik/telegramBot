package danilov.roman.telegramBot;

//import com.google.gson.Gson;
//import org.apache.log4j.Logger;
//import text_music_bot.Json.AnswerFromTelegramApi;

import org.apache.commons.io.Charsets;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.telegram.telegrambots.ApiContextInitializer;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FileUtil {

    public static final String serviseDetectEyes = "http://172.17.100.194:8081/api/upload";

    /**
     * Метод для сохранения файла
     * @param url - расположение файла
     * @param pathSaveFile - путь сохранения файла
     */
    public static boolean saveFile(String url, String pathSaveFile) {

        try {
            Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", new MyConnectionSocketFactory(SSLContexts.createSystemDefault()))
                    .build();
            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);
            CloseableHttpClient client = HttpClients.custom()
                    .setConnectionManager(cm)
                    .build();

            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(Main.PROXY_USER, Main.PROXY_PASSWORD.toCharArray());
                }
            });

            InetSocketAddress socksaddr = new InetSocketAddress(Main.PROXY_HOST, Main.PROXY_PORT);
            HttpClientContext context = HttpClientContext.create();
            context.setAttribute("socks.address", socksaddr);

            HttpGet request = new HttpGet(url);

            HttpResponse response = client.execute(request, context);
            HttpEntity entity = response.getEntity();

            int responseCode = response.getStatusLine().getStatusCode();

            System.out.println("Request Url: " + request.getURI());
            System.out.println("Response Code: " + responseCode);

            InputStream is = entity.getContent();

            FileOutputStream fos = new FileOutputStream(new File(pathSaveFile));
            int inByte;
            while ((inByte = is.read()) != -1) {
                fos.write(inByte);
            }

            is.close();
            fos.close();

            client.close();
            System.out.println("File Download Completed!!!");

            return true;


        } catch (UnsupportedOperationException | IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    static class MyConnectionSocketFactory extends SSLConnectionSocketFactory {

        public MyConnectionSocketFactory(final SSLContext sslContext) {
            super(sslContext);
        }

        @Override
        public Socket createSocket(final HttpContext context) throws IOException {
            InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("socks.address");
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
            return new Socket(proxy);
        }
    }

    /**
     * Отправка фото в сервис определения глаза.
     *
     * @param uploadImage String путь к фото
     * @return Map Ответ от сервиса
     */
    public static Map<String, Object> sendFileToServiceAndGetResponse(String uploadImage) {
        try {
            CloseableHttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(serviseDetectEyes);
            File postFile = new File(uploadImage);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addBinaryBody("file", postFile, ContentType.IMAGE_JPEG, uploadImage);

            HttpEntity entity = builder.build();
            post.setEntity(entity);
            HttpResponse response = client.execute(post);
            HttpEntity entityResponse = response.getEntity();
            Header encodingHeader = entityResponse.getContentEncoding();

            // you need to know the encoding to parse correctly
            Charset encoding = encodingHeader == null ? StandardCharsets.UTF_8 :
                    Charsets.toCharset(encodingHeader.getValue());

            // use org.apache.http.util.EntityUtils to read json as string
            String json = EntityUtils.toString(entityResponse, encoding);

            return (new JSONObject(json)).toMap();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new HashMap<>();
    }

}