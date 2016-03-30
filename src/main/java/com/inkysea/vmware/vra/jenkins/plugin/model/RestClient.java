package com.inkysea.vmware.vra.jenkins.plugin.model;

import com.inkysea.vmware.vra.jenkins.plugin.model.PluginParam;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;


/**
 * Created by kthieler on 2/23/16.
 */
public class RestClient {

        private String token;
        private String AUTH_REST_URL = "";
        private String CHECK_EXEC_STATUS = "";
        private String ENTITTLEDCATALOGITEMVIEW_REST_URL = "";
        private String ENTITTLEDCATALOGITEM_REQUEST_REST_URL = "";
        private String REQUESTS_REST_URL = "";

        private String TOKEN_JSON = "{\"username\": \"%s\", \"password\": \"%s\", \"tenant\": \"%s\"}";
        private String userName;
        private String password;
        private String tenant;
        private Map<String, String> outputs;



    public RestClient (){

        }

        public RestClient( PluginParam params) throws IOException {

            this.AUTH_REST_URL = params.getServerUrl() + "/identity/api/tokens";

            this.userName = params.getUserName();
            this.password = params.getPassword();
            this.tenant   = params.getTenant();

            this.token = AuthToken();


        }

        public RestClient( DestroyParam dparams) throws IOException {

            this.AUTH_REST_URL = dparams.getServerUrl() + "/identity/api/tokens";

            this.userName = dparams.getUserName();
            this.password = dparams.getPassword();
            this.tenant   = dparams.getTenant();

            this.token = AuthToken();


        }

        public RestClient( String vRA_URL, String userName, String password, String tenant) throws IOException {

            this.AUTH_REST_URL = vRA_URL + "/identity/api/tokens";

            this.userName = userName;
            this.password = password;
            this.tenant   = tenant;

            this.token = AuthToken();

        }

        public String AuthToken() throws IOException {
            String tokenPayload = String.format(TOKEN_JSON, userName, password, tenant);
            HttpResponse httpResponse = this.Post(AUTH_REST_URL, tokenPayload);
            String responseAsJson = this.FormatResponseAsJsonString(httpResponse);
            JsonObject stringJsonAsObject = FormJsonObject(responseAsJson);
            JsonElement idElement = stringJsonAsObject.get("id");
            if (idElement == null) {
                throw new IOException(responseAsJson);
            } else {
                token = idElement.getAsString();
            }
            return token;
        }

        public HttpResponse Get(String URL) throws IOException {
            CloseableHttpClient httpClient = null;
            try {
                httpClient = HttpClient();
                HttpGet request = new HttpGet(URL);
                request.setHeader("accept", "application/json; charset=utf-8");
                if (StringUtils.isNotBlank(token)) {
                    String authorization = "Bearer " + token;
                    request.setHeader("Authorization", authorization);
                }
                return httpClient.execute(request);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
            return null;
        }

        public HttpResponse Post(String URL, String payload) throws IOException {
            CloseableHttpClient httpClient = null;
            try {
                httpClient = HttpClient();
                HttpPost postRequest = new HttpPost(URL);
                StringEntity input = new StringEntity(payload);
                input.setContentType("application/json");
                postRequest.setEntity(input);
                postRequest.setHeader("Content-Type", "application/json");
                postRequest.setHeader("accept", "application/json; charset=utf-8");
                if (StringUtils.isNotBlank(token)) {
                    String authorization = "Bearer " + token;
                    postRequest.setHeader("Authorization", authorization);
                }
                return httpClient.execute(postRequest);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
            return null;
        }

        public HttpResponse Post(String URL, HttpEntity multipart ) throws IOException {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            try {
                httpClient = HttpClient();
                HttpPost postRequest = new HttpPost(URL);

                postRequest.setEntity(multipart);

                //postRequest.setHeader("Content-Type", "multipart/form-data;boundary=vRAJenkinsUpload");
                //postRequest.setHeader("accept", "application/json; charset=utf-8");


            if (StringUtils.isNotBlank(token)) {
                String authorization = "Bearer " + token;
                postRequest.setHeader("Authorization", authorization);
            }
                CloseableHttpResponse response = httpClient.execute(postRequest);
               // assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
                //httpClient.close();

                return response;

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
        }
        return null;
    }

    public HttpResponse Put(String URL, String payload) throws IOException {
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClient();
            HttpPut putRequest = new HttpPut(URL);
            StringEntity input = new StringEntity(payload);
            input.setContentType("application/json");
            putRequest.setEntity(input);
            putRequest.setHeader("Content-Type", "application/json");
            putRequest.setHeader("accept", "application/json; charset=utf-8");
            if (StringUtils.isNotBlank(token)) {
                String authorization = "Bearer " + token;
                putRequest.setHeader("Authorization", authorization);
            }
            return httpClient.execute(putRequest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }


        private CloseableHttpClient HttpClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
            return HttpClients.custom().setSSLSocketFactory(
                    sslsf).build();
        }

        public String FormatResponseAsJsonString(HttpResponse response) throws IOException {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                output.append(line);
            }
            return output.toString();
        }


        public JsonObject FormJsonObject(String responseAsJson) {
            JsonElement execResponseParse = new JsonParser().parse(responseAsJson);
            return execResponseParse.getAsJsonObject();
        }
}
