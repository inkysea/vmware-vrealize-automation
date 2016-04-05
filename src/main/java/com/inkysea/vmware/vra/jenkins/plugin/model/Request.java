package com.inkysea.vmware.vra.jenkins.plugin.model;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import com.google.gson.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

/**
 * Created by kthieler on 2/23/16.
 */
public class Request  {

    private RestClient restclient;
    private PluginParam params;
    private DestroyParam dparams;

    private ExecutionStatus executionStatus;
    private String REQUESTS_ID_URL = "";
    private String FETCH_CATALOG_ITEM = "";
    private String PROVISION_BLUEPRINT = "";
    private String RESOURCE_ACTIONS_REQUEST_TEMPLATE_REST = "";
    private String RESOURCE_ACTIONS_REQUEST_REST = "";
    private String RESOURCE_ACTIONS_REST = "";
    private String REQUESTS_POST_URL = "";
    private String RESOURCEVIEW_REST = "";
    private String REQUEST_RESOURCEVIEW_REST = "";
    private String PACKAGES_REST;
    private String BLUEPRINTS_REST;
    private String SOFTWARECOMPONENTTYPE_REST;
    private String PACKAGESVALIDATE_REST;
    private String BLUEPRINTS_STATUS_REST;
    private String CATALOGITEMS_REST;
    private String SERVICES_REST;


    private String AUTH_TOKEN = "";
    private PrintStream logger;
    private String catalogId;
    public String requestID;
    public JsonObject bluePrintTemplate;




    public Request(PrintStream logger, PluginParam params) throws IOException {
    // Constructor for build and wrapper to create or destroy deployment

        this.params = params;
        this.logger = logger;


        try {
            this.restclient  = new RestClient(params);
            this.AUTH_TOKEN = restclient.AuthToken();
        }catch ( IOException e) {
            e.printStackTrace();
        }

        String catalogServiceApiUrl = params.getServerUrl().replaceFirst("/+$", "")
                                                + "/catalog-service/api/";

        this.REQUESTS_ID_URL = catalogServiceApiUrl + "consumer/requests/%s";
        this.REQUESTS_POST_URL = catalogServiceApiUrl + "consumer/requests/";
        this.FETCH_CATALOG_ITEM = catalogServiceApiUrl + "consumer/entitledCatalogItemViews?$filter=name+eq+'%s'";
        this.PROVISION_BLUEPRINT = catalogServiceApiUrl + "consumer/entitledCatalogItems/%s/requests";
        this.REQUEST_RESOURCEVIEW_REST = catalogServiceApiUrl + "consumer/requests/%s/resourceViews";
        this.RESOURCE_ACTIONS_REQUEST_TEMPLATE_REST =  catalogServiceApiUrl + "consumer/resources/%s/actions/%s/requests/template";
        this.RESOURCE_ACTIONS_REQUEST_REST =  catalogServiceApiUrl + "consumer/resources/%s/actions/%s/requests";
        this.RESOURCE_ACTIONS_REST = catalogServiceApiUrl + "consumer/resources/%s/actions/";
        this.RESOURCEVIEW_REST = catalogServiceApiUrl + "consumer/resourceViews/";
        this.CATALOGITEMS_REST = catalogServiceApiUrl + "catalogItems/";
        this.SERVICES_REST=catalogServiceApiUrl+"services/";


        String compositionServiceURL = params.getServerUrl()  + "/composition-service/api/";
        this.BLUEPRINTS_REST = compositionServiceURL + "blueprints/%s";
        this.BLUEPRINTS_STATUS_REST = compositionServiceURL + "blueprints/%s/status";



    }

    public Request(PrintStream logger, DestroyParam dparams) throws IOException {
    // Constructor for post build actions to destroy deployment

        this.dparams = dparams;
        this.logger = logger;


        try {
            this.restclient  = new RestClient(dparams);
            this.AUTH_TOKEN = restclient.AuthToken();
        }catch ( IOException e) {
            e.printStackTrace();
        }

        String catalogServiceApiUrl = dparams.getServerUrl().replaceFirst("/+$", "") + "/catalog-service/api/";

        this.REQUESTS_ID_URL = catalogServiceApiUrl + "consumer/requests/%s";
        this.REQUESTS_POST_URL = catalogServiceApiUrl + "consumer/requests/";
        this.FETCH_CATALOG_ITEM = catalogServiceApiUrl + "consumer/entitledCatalogItemViews?$filter=name+eq+'%s'";
        this.PROVISION_BLUEPRINT = catalogServiceApiUrl + "consumer/entitledCatalogItems/%s/requests";
        this.REQUEST_RESOURCEVIEW_REST = catalogServiceApiUrl + "consumer/requests/%s/resourceViews";
        this.RESOURCE_ACTIONS_REQUEST_TEMPLATE_REST =  catalogServiceApiUrl + "consumer/resources/%s/actions/%s/requests/template";
        this.RESOURCE_ACTIONS_REQUEST_REST =  catalogServiceApiUrl + "consumer/resources/%s/actions/%s/requests";
        this.RESOURCE_ACTIONS_REST = catalogServiceApiUrl + "consumer/resources/%s/actions/";
        this.RESOURCEVIEW_REST = catalogServiceApiUrl + "consumer/resourceViews/";

    }

    public Request(PrintStream logger, String vRA_URL, String userName, String password, String tenant ) {
        // Constructor for blueprint requests

        this.logger = logger;


        try {
            this.restclient  = new RestClient(vRA_URL,userName,password,tenant);
            this.AUTH_TOKEN = restclient.AuthToken();
        }catch ( IOException e) {
            e.printStackTrace();
        }

        String catalogServiceApiUrl = vRA_URL.replaceFirst("/+$", "") + "/catalog-service/api/";

        this.REQUESTS_ID_URL = catalogServiceApiUrl + "consumer/requests/%s";
        this.REQUESTS_POST_URL = catalogServiceApiUrl + "consumer/requests/";
        this.FETCH_CATALOG_ITEM = catalogServiceApiUrl + "consumer/entitledCatalogItemViews?$filter=name+eq+'%s'";
        this.PROVISION_BLUEPRINT = catalogServiceApiUrl + "consumer/entitledCatalogItems/%s/requests";
        this.REQUEST_RESOURCEVIEW_REST = catalogServiceApiUrl + "consumer/requests/%s/resourceViews";
        this.RESOURCE_ACTIONS_REQUEST_TEMPLATE_REST =  catalogServiceApiUrl + "consumer/resources/%s/actions/%s/requests/template";
        this.RESOURCE_ACTIONS_REQUEST_REST =  catalogServiceApiUrl + "consumer/resources/%s/actions/%s/requests";
        this.RESOURCE_ACTIONS_REST = catalogServiceApiUrl + "consumer/resources/%s/actions/";
        this.RESOURCEVIEW_REST = catalogServiceApiUrl + "consumer/resourceViews/";
        this.SERVICES_REST=catalogServiceApiUrl+"services/";


        String contentManagementServiceURL = vRA_URL + "/content-management-service/api/";

        this.PACKAGES_REST = contentManagementServiceURL + "packages/";
        this.PACKAGESVALIDATE_REST = contentManagementServiceURL + "packages/validate";

        String compositionServiceURL = vRA_URL + "/composition-service/api/";
        this.BLUEPRINTS_REST = compositionServiceURL + "blueprints/";
        this.SERVICES_REST=compositionServiceURL+"/catalog-service/api/services/";
        this.CATALOGITEMS_REST = catalogServiceApiUrl + "catalogItems/";

        this.BLUEPRINTS_STATUS_REST = compositionServiceURL + "blueprints/%s/status";
        this.SERVICES_REST=catalogServiceApiUrl+"services/";


        String softwareServiceURL = vRA_URL + "/software-service/api/";

        this.SOFTWARECOMPONENTTYPE_REST = softwareServiceURL + "softwarecomponenttypes";

    }

    public JsonObject validatePackages(String file, String resolutionMode) throws IOException {

        String url = PACKAGESVALIDATE_REST.replace(' ', '+');
        System.out.println("Using :" + url);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("resolutionMode", resolutionMode);
        builder.addTextBody("size", "original");
        builder.addBinaryBody("file", new File(file),
                ContentType.APPLICATION_OCTET_STREAM, file);

        HttpEntity multipart = builder.build();

        HttpResponse vRAResponse = restclient.Post(url, multipart);
        System.out.println("HTTP Response :" + vRAResponse);

        String responseAsJson = restclient.FormatResponseAsJsonString(vRAResponse);
        System.out.println("JSON :" + responseAsJson);

        JsonObject stringJsonAsObject = restclient.FormJsonObject(responseAsJson);

        return stringJsonAsObject;
    }

    public JsonObject Packages(String file, String resolutionMode) throws IOException {

        String url = PACKAGES_REST.replace(' ', '+');
        System.out.println("Using :" + url);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("resolutionMode", resolutionMode);
        builder.addTextBody("size", "original");
        builder.addBinaryBody("file", new File(file),
                ContentType.APPLICATION_OCTET_STREAM, file);

        HttpEntity multipart = builder.build();

        HttpResponse vRAResponse = restclient.Post(url, multipart);
        System.out.println("HTTP Response :" + vRAResponse);

        String responseAsJson = restclient.FormatResponseAsJsonString(vRAResponse);
        System.out.println("JSON :" + responseAsJson);

        JsonObject stringJsonAsObject = restclient.FormJsonObject(responseAsJson);

        return stringJsonAsObject;
    }


    public JsonObject fetchBluePrint() throws IOException {
        JsonObject response = null;
        System.out.println("Fetching BP :" + params.getBluePrintName());

        String url = String.format(FETCH_CATALOG_ITEM, params.getBluePrintName()).replace(' ', '+');
        System.out.println("Using :" + url);

        HttpResponse vRAResponse = restclient.Get(url);
        String responseAsJson = restclient.FormatResponseAsJsonString(vRAResponse);
        System.out.println("JSON :" + responseAsJson);

        JsonObject stringJsonAsObject = restclient.FormJsonObject(responseAsJson);
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
                    throw new IOException("More than one blueprint with name " + params.getBluePrintName() + " found");
                } else if (contents.size() < 1) {
                    throw new IOException("Blueprint with name " + params.getBluePrintName() + " not found");
                }
            }
        }
        return stringJsonAsObject;
    }

    public JsonObject GetBluePrintTemplate() throws IOException {


        JsonObject response = this.fetchBluePrint();

        JsonArray catalogItemContentArray = response.getAsJsonArray("content");

        this.catalogId = catalogItemContentArray.get(0).getAsJsonObject()
                .get("catalogItemId").getAsString();

        JsonArray linkArray = catalogItemContentArray.get(0)
                                                     .getAsJsonObject()
                                                     .getAsJsonArray("links");

        System.out.println("Links Array: "+linkArray);


        String templateURL = linkArray.get(0).getAsJsonObject().get("href").getAsString();

        String url = templateURL;
        System.out.println("Get BP Template: "+templateURL);

        HttpResponse vRAResponse = restclient.Get(url);
        String responseAsJson = restclient.FormatResponseAsJsonString(vRAResponse);
        System.out.println("BP JSON : "+responseAsJson);

        this.bluePrintTemplate = restclient.FormJsonObject(responseAsJson);

        return this.bluePrintTemplate ;
    }

    public JsonObject ProvisionBluePrint() throws IOException {

        JsonObject template = this.GetBluePrintTemplate();

        JsonObject response = ProvisionBluePrint(template);

        return response;
    }

    public JsonObject ProvisionBluePrint(JsonObject template) throws IOException {

        String url = String.format(PROVISION_BLUEPRINT, this.catalogId).replace(' ', '+');
        Gson gson = new Gson();

        System.out.println("Using Template : "+template.toString());

        HttpResponse httpResponse = restclient.Post(url, template.toString());
        String responseAsJson = restclient.FormatResponseAsJsonString(httpResponse);
        JsonObject response = restclient.FormJsonObject(responseAsJson);
        this.requestID = response.get("id").getAsString();
        return response;
    }



    public ExecutionStatus RequestStatus() throws IOException {

        String url = String.format(REQUESTS_ID_URL, this.requestID).replace(' ', '+');

        System.out.println("Request URL: "+url);

        HttpResponse vRAResponse = restclient.Get(url);
        System.out.println("Got Response : "+ vRAResponse.toString());

        String responseAsJson = restclient.FormatResponseAsJsonString(vRAResponse);

        System.out.println("BP JSON : "+responseAsJson);
        JsonObject stringJsonAsObject = restclient.FormJsonObject(responseAsJson);
        String phase = stringJsonAsObject.get("phase").getAsString();

        System.out.println("Request phase : "+phase);

        ExecutionStatus status = ExecutionStatus.valueOf(phase);

        this.executionStatus = status;

        return status;
    }

    public boolean IsRequestComplete () throws IOException {

        switch (RequestStatus()) {
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

    public JsonObject GetRequestResourceView(String reqID) throws IOException {
        this.requestID = reqID;
        JsonObject responseAsJSON = GetResourceView();
        return responseAsJSON;
    }

    public JsonObject GetRequestResourceView() throws IOException {
        //  get /consumer/requests/{ID}/resourceViews   search for...

        String url = String.format(REQUEST_RESOURCEVIEW_REST, this.requestID).replace(' ', '+');
        System.out.println("Using :" + url);

        HttpResponse vRAResponse = restclient.Get(url);
        String response = restclient.FormatResponseAsJsonString(vRAResponse);
        System.out.println("JSON :" + response);

        JsonObject responseAsJSON = restclient.FormJsonObject(response);

        return responseAsJSON;

    }

    public JsonObject GetResourceView() throws IOException {
        //  get /consumer/requests/{ID}/resourceViews   search for...

        String url = String.format(RESOURCEVIEW_REST).replace(' ', '+');
        System.out.println("Using :" + url);

        HttpResponse vRAResponse = restclient.Get(url);
        String response = restclient.FormatResponseAsJsonString(vRAResponse);
        System.out.println("JSON :" + response);

        JsonObject responseAsJSON = restclient.FormJsonObject(response);

        return responseAsJSON;

    }

    public JsonObject GetResourceView(String deploymentName) throws IOException {
        //  get /consumer/requests/{ID}/resourceViews   search for...

        String url = String.format(RESOURCEVIEW_REST+"?$filter=name+eq+'%s'", deploymentName).replace(' ', '+');
        System.out.println("Using :" + url);

        HttpResponse vRAResponse = restclient.Get(url);
        String response = restclient.FormatResponseAsJsonString(vRAResponse);
        System.out.println("JSON :" + response);

        JsonObject responseAsJSON = restclient.FormJsonObject(response);

        return responseAsJSON;

    }

    public JsonObject GetCatalogItemByName(String catalogItemName) throws IOException {
        //  get /consumer/requests/{ID}/resourceViews   search for...

        String url = String.format(CATALOGITEMS_REST+"?$filter=name+eq+'%s'", catalogItemName).replace(' ', '+');
        System.out.println("Using :" + url);

        HttpResponse vRAResponse = restclient.Get(url);
        String response = restclient.FormatResponseAsJsonString(vRAResponse);
        System.out.println("JSON :" + response);

        JsonObject responseAsJSON = restclient.FormJsonObject(response);

        return responseAsJSON;

    }

    public JsonObject getResourceActions(String resourceID ) throws IOException {

        String url = String.format(RESOURCE_ACTIONS_REST, resourceID ).replace(' ', '+');
        System.out.println("Using :" + url);

        HttpResponse vRAResponse = restclient.Get(url);
        String response = restclient.FormatResponseAsJsonString(vRAResponse);
        System.out.println("JSON :" + response);

        JsonObject responseAsJSON = restclient.FormJsonObject(response);

        return responseAsJSON;

    }

    public JsonObject GetBlueprint(String blueprintName) throws IOException{

        ///calls {{vRAURL}}/composition-service/api/blueprints/blueprintName

        String url = String.format(BLUEPRINTS_REST, blueprintName ).replace(' ', '+');
        System.out.println("Using :" + url);

        HttpResponse vRAResponse = restclient.Get(url);
        String response = restclient.FormatResponseAsJsonString(vRAResponse);
        System.out.println("JSON :" + response);

        JsonObject responseAsJSON = restclient.FormJsonObject(response);

        return responseAsJSON;
    }



    public JsonObject PutBluprintStatus(String blueprintName, String body) throws IOException{
        // {{vRAURL}}/composition-service/api/blueprints/CommerceAppcopycopy/status
        System.out.println("Publishing REST :"+BLUEPRINTS_STATUS_REST);
        System.out.println("BP NAME :"+blueprintName);

        String url = String.format(BLUEPRINTS_STATUS_REST, blueprintName ).replace(' ', '+');
        System.out.println("Using :" + url);

        HttpResponse vRAResponse = restclient.Put(url, "\""+body + "\"");
        String response = restclient.FormatResponseAsJsonString(vRAResponse);
        System.out.println("JSON :" + response);

        JsonObject responseAsJSON = restclient.FormJsonObject(response);

        return responseAsJSON;
    }

    public JsonObject PutCatalogItem(String catalogId, String body) throws IOException{
        // /catalog-service/api/catalogItems/a29501cd-179a-4be5-8096-f29ad4847521

        String url = CATALOGITEMS_REST+"/"+catalogId;
        url = url.replace(' ', '+');
        System.out.println("Using :" + url);

        HttpResponse vRAResponse = restclient.Put(url, body);
        String response = restclient.FormatResponseAsJsonString(vRAResponse);
        System.out.println("JSON :" + response);

        JsonObject responseAsJSON = restclient.FormJsonObject(response);

        return responseAsJSON;
    }

    public JsonObject getServiceCategory(String serviceCategory) throws IOException {
        //  get /consumer/requests/{ID}/resourceViews   search for...

        String url = String.format(SERVICES_REST+"?$filter=name+eq+'%s'", serviceCategory).replace(' ', '+');
        System.out.println("Using :" + url);

        HttpResponse vRAResponse = restclient.Get(url);
        //if ( vRAResponse.getStatusLine().getStatusCode() != 200 ){
        //    throw new IOException("Received invalid response from vRA : \n HTTP Response: "+ vRAResponse );
        //}else {

            String response = restclient.FormatResponseAsJsonString(vRAResponse);
            System.out.println("JSON :" + response);

            JsonObject responseAsJSON = restclient.FormJsonObject(response);

            return responseAsJSON;

        //}

    }


    public JsonObject getResourceActionsRequestTemplate(String resourceID, String actionID ) throws IOException{

        String url = String.format(RESOURCE_ACTIONS_REQUEST_TEMPLATE_REST, resourceID, actionID ).replace(' ', '+');
        System.out.println("Using :" + url);

        HttpResponse vRAResponse = restclient.Get(url);
        String response = restclient.FormatResponseAsJsonString(vRAResponse);
        System.out.println("JSON :" + response);

        JsonObject responseAsJSON = restclient.FormJsonObject(response);

        return responseAsJSON;

    }

    public void ResourceActionsRequest(String resourceID, String actionID, JsonObject jsonBody ) throws IOException{

        String url = String.format(RESOURCE_ACTIONS_REQUEST_REST, resourceID, actionID ).replace(' ', '+');
        System.out.println("Using :" + url);

        HttpResponse vRAResponse = restclient.Post(url, jsonBody.toString());
        String response = restclient.FormatResponseAsJsonString(vRAResponse);
        System.out.println("JSON :" + response);

        //JsonObject responseAsJSON = restclient.FormJsonObject(response);

        //return responseAsJSON;

    }


    public void PostRequestJson( String jsonBody ) throws IOException{
        JsonObject responseAsJSON = null;

        String url = String.format(REQUESTS_POST_URL).replace(' ', '+');
        System.out.println("Using :" + url);

        HttpResponse vRAResponse = restclient.Post(url, jsonBody );

        System.out.println("HTTP Response :" + vRAResponse);

        if ( vRAResponse.getStatusLine().getStatusCode() != 201 ){
            throw new IOException("Received invalid response from vRA : \n HTTP Response: "+ vRAResponse +
                    "\n JSON Response :" + responseAsJSON );
        }else{
            String locationHeader = vRAResponse.getFirstHeader("Location").getValue();
            this.requestID = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
        }


    }
    public void PostRequest( String jsonBody ) throws IOException {

        JsonObject responseAsJSON = null;

        String url = String.format(REQUESTS_POST_URL).replace(' ', '+');
        System.out.println("Using :" + url);

        HttpResponse vRAResponse = restclient.Post(url, jsonBody );
        String response = restclient.FormatResponseAsJsonString(vRAResponse);

        System.out.println("HTTP Response :" + vRAResponse);

        System.out.println("JSON Response:" + response);


        if ( ! response.equals("") ) {

            responseAsJSON = restclient.FormJsonObject(response);
            System.out.println("JSON :" + responseAsJSON);


        }

        if ( vRAResponse.getStatusLine().getStatusCode() != 201 ){
            throw new IOException("Received invalid response from vRA : \n HTTP Response: "+ vRAResponse +
                                   "\n JSON Response :" + responseAsJSON );
        }


    }




}
