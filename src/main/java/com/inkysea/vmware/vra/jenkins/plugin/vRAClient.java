package com.inkysea.vmware.vra.jenkins.plugin;

import com.google.gson.*;
import com.inkysea.vmware.vra.jenkins.plugin.model.ExecutionStatus;
import com.inkysea.vmware.vra.jenkins.plugin.model.PluginParam;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by kthieler on 11/25/15.
 */
public class vRAClient {

    private String token;
    private String FETCH_TOKEN = "";
    private String CHECK_EXEC_STATUS = "";
    private String FETCH_CATALOG_ITEM = "";
    private String PROVISION_BLUEPRINT = "";
    private String TOKEN_JSON = "{\"username\": \"%s\", \"password\": \"%s\", \"tenant\": \"%s\"}";
    private PluginParam params;


    public vRAClient(PluginParam params) throws IOException {

        this.params = params;
        this.FETCH_TOKEN = params.getServerUrl() + "/identity/api/tokens";

        String catalogServiceApiUrl = params.getServerUrl() + "/catalog-service/api/";
        this.FETCH_CATALOG_ITEM = catalogServiceApiUrl + "consumer/entitledCatalogItemViews?$filter=name+eq+'%s'";
        this.PROVISION_BLUEPRINT = catalogServiceApiUrl + "consumer/entitledCatalogItems/%s/requests";
        this.CHECK_EXEC_STATUS = catalogServiceApiUrl + "consumer/requests//%s";
        this.token = AuthToken();
    }

    private String AuthToken() throws IOException {
        String tokenPayload = String.format(TOKEN_JSON, params.getUserName(), params.getPassword(), params.getTenant());
        HttpResponse httpResponse = this.Post(FETCH_TOKEN, tokenPayload);
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
        }
        catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }

    private HttpResponse Post(String URL, String payload) throws IOException {
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
        }
        catch (KeyManagementException e) {
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

    private String FormatResponseAsJsonString(HttpResponse response) throws IOException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader((response.getEntity().getContent())));

        StringBuilder output = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            output.append(line);
        }
        return output.toString();
    }


    private JsonObject FormJsonObject(String responseAsJson) {
        JsonElement execResponseParse = new JsonParser().parse(responseAsJson);
        return execResponseParse.getAsJsonObject();
    }

    public JsonObject fetchBluePrint(String blueprint) throws IOException {
        JsonObject response = null;
        String url = String.format(FETCH_CATALOG_ITEM, params.getBluePrintName());
        System.out.println("Using :" + url);

        HttpResponse vRAResponse = Get(url);
        String responseAsJson = this.FormatResponseAsJsonString(vRAResponse);
        System.out.println("JSON :" + responseAsJson);

        JsonObject stringJsonAsObject = FormJsonObject(responseAsJson);
        JsonElement contentElement = stringJsonAsObject.get("content");
        System.out.println("Parsing content :");

        if (contentElement == null) {
            throw new IOException(responseAsJson);
        } else {
            JsonArray contents = contentElement.getAsJsonArray();
            System.out.println("Array content :");

            if (contents.size() == 1) {
                response = contents.get(0).getAsJsonObject();
                System.out.println("Unique :");

            } else {
                if (contents.size() > 1) {
                    throw new IOException("More than one blueprint with name " + blueprint + " found");
                } else if (contents.size() < 1) {
                    throw new IOException("Blueprint with name " + blueprint + " not found");
                }
            }
        }
        return response;
    }

    public JsonObject GetBluePrintTemplate(String blueprinttemplateURL) throws IOException {

        JsonObject response = null;
        String url = blueprinttemplateURL;
        System.out.println("Get BP Template: "+blueprinttemplateURL);

        HttpResponse vRAResponse = Get(url);
        String responseAsJson = this.FormatResponseAsJsonString(vRAResponse);
        System.out.println("BP JSON : "+responseAsJson);

        response = FormJsonObject(responseAsJson);

        return response;
    }
    public JsonObject ProvisionBluePrint(String catalogId, JsonObject blueprinttemplate) throws IOException {
        JsonObject response = null;
        String url = String.format(PROVISION_BLUEPRINT, catalogId);
        Gson gson = new Gson();

        HttpResponse httpResponse = this.Post(url, blueprinttemplate.toString());
        String responseAsJson = this.FormatResponseAsJsonString(httpResponse);
        response = FormJsonObject(responseAsJson);
        return response;
    }

    public ExecutionStatus RequestStatus(String ExecId) throws IOException {

        String url = String.format(CHECK_EXEC_STATUS, ExecId);

        System.out.println("Request URL: "+url);

        HttpResponse vRAResponse = Get(url);
        System.out.println("Got Response : "+ vRAResponse.toString());

        String responseAsJson = this.FormatResponseAsJsonString(vRAResponse);

        System.out.println("BP JSON : "+responseAsJson);
        JsonObject stringJsonAsObject = FormJsonObject(responseAsJson);
        String phase = stringJsonAsObject.get("phase").getAsString();

        System.out.println("Request phase : "+phase);

        ExecutionStatus status = ExecutionStatus.valueOf(phase);

        return status;
    }

    public boolean IsRequestComplete (ExecutionStatus status) throws IOException {

        switch (status) {
            case SUCCESSFUL:
            case FAILED:
            case REJECTED:
                return true;
            case PENDING_PRE_APPROVAL:
                return false;
            case PENDING_POST_APPROVAL:
                return false;
            default:
                return false;
        }

    }

    }
