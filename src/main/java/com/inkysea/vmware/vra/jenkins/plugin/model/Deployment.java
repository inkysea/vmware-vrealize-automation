package com.inkysea.vmware.vra.jenkins.plugin.model;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.*;

/**
 * Created by kthieler on 2/24/16.
 */
public class Deployment {

    private PluginParam params;
    private Request request;
    private PrintStream logger;
    private String DESTROY_TEMPLATE_URL;
    private String DESTROY_URL;
    private String deploymentName;
    private String parentResourceID;
    private JsonObject deploymentResources;
    private String businessGroupId;
    private String tenantId;
    private String jsonString = "{\"@type\":\"ResourceActionRequest\", \"resourceRef\":{\"id\":\"\"}, \"resourceActionRef\"\n" +
            ":{\"id\":\"\"}, \"organization\":{\"tenantRef\":\"\", \"tenantLabel\"\n" +
            ":\"\", \"subtenantRef\":\"\", \"subtenantLabel\":\"\"\n" +
            "}, \"state\":\"SUBMITTED\", \"requestNumber\":0, \"requestData\":{\"entries\":[]}}";

    private List machineList;
    private List loadBalancerList;

    private Map<String, String> deploymentVars = new HashMap<String, String>();


    public Deployment(PrintStream logger, PluginParam params) throws IOException {

        this.params = params;
        this.logger = logger;

        this.request  = new Request(logger, params);

    }

    public boolean Create() throws IOException, InterruptedException {

        boolean rcode = false;

        request.ProvisionBluePrint();


        if (this.params.isWaitExec()) {
            while (!request.IsRequestComplete()) {
                System.out.println("Execution status : " + request.RequestStatus().toString());
                Thread.sleep(10 * 1000);
            }

            switch (request.RequestStatus()) {
                case SUCCESSFUL:
                    System.out.println("Request completed successfully");
                    DeploymentResources();
                    deploymentVars.put(getDeploymentName()+"_NAME", getDeploymentName());
                    rcode = true;
                    break;
                case FAILED:
                    rcode = false;
                    throw new IOException("Request execution failed. Please go to vRA for more details");
                case REJECTED:
                    rcode = false;
                    throw new IOException("Request execution cancelled. Please go to vRA for more details");
            }
        }

        return rcode;

    }

    public List getMachineList(String name) throws IOException {

        this.deploymentResources = this.request.GetResourceView();
        return getMachineList();
    }

    public List getMachineList() throws IOException{

        JsonArray contentArray = this.deploymentResources.getAsJsonArray("content");
        System.out.println("Array content :" + contentArray );

        for (JsonElement content : contentArray) {

            if (content.getAsJsonObject().get("resourceType").getAsString().contains("Infrastructure.Virtual")) {

                JsonObject jsonData = content.getAsJsonObject().getAsJsonObject("data");
                JsonArray  jsonArray = jsonData.getAsJsonArray("NETWORK_LIST");
                JsonObject jsonNetworkData = jsonArray.getAsJsonObject().getAsJsonObject("data");

                jsonNetworkData.get("NETWORK_NAME").getAsString();
                jsonNetworkData.get("NETWORK_ADDRESS").getAsString();


                String[] machineArray = {
                        content.getAsJsonObject().get("resourceType").getAsString(),
                        jsonData.getAsJsonObject("name").getAsString(),
                        jsonData.getAsJsonObject().get("Component").getAsString(),
                        jsonNetworkData.get("NETWORK_NAME").getAsString(),
                        jsonNetworkData.get("NETWORK_ADDRESS").getAsString(),
                };

                this.machineList.add(machineArray);

            }
        }

        return this.machineList;
    }

    public Map<String, String> getOutputs() {
        // Prefix outputs with stack name to prevent collisions with other stacks created in the same build.
        HashMap<String, String> map = new HashMap<String, String>();
        for (String key : deploymentVars.keySet()) {
            map.put(params.getTenant() + "_" + key, deploymentVars.get(key));
        }
        return map;
    }

    public String getDeploymentName() throws IOException{

        JsonArray contentArray = this.deploymentResources.getAsJsonArray("content");
        for (JsonElement content : contentArray) {

            if (content.getAsJsonObject().get("resourceType").getAsString().equals("composition.resource.type.deployment")) {

                this.deploymentName = content.getAsJsonObject().get("name").getAsString();
                System.out.println("Name :" + this.deploymentName );

            }
        }
        String depName = this.deploymentName;
        return depName;
    }

    public void DeploymentResources() throws IOException{

            this.deploymentResources  = request.GetRequestResourceView();

    }


    public void getParentResourceID() throws IOException{

        JsonArray contentArray = this.deploymentResources.getAsJsonArray("content");
        for (JsonElement content : contentArray) {

            if (content.getAsJsonObject().get("resourceType").getAsString().equals("composition.resource.type.deployment")) {

                this.parentResourceID = content.getAsJsonObject().get("resourceId").getAsString();
            }
        }
    }



    public void getParentResourceID(String name) throws IOException{

        JsonArray contentArray = this.deploymentResources.getAsJsonArray("content");
        System.out.println("Array content :" + contentArray );

        for (JsonElement content : contentArray) {
            System.out.println("Content :" + content.getAsJsonObject().get("name").getAsString() );

            if (content.getAsJsonObject().get("name").getAsString().equals(name)) {
                this.parentResourceID = content.getAsJsonObject().get("resourceId").getAsString();
                System.out.println("ParentID :" + this.parentResourceID );
                break;
            }
        }
    }

    public String getDestroyURL() throws IOException {

        String URL = "";

        return URL;
    }

    public String getDestroyAction() throws IOException {

        this.getParentResourceID();

        JsonObject actions = this.request.getResourceActions(this.parentResourceID);

        JsonArray contentArray = actions.getAsJsonArray("content");

        String actionID = "";

        for (JsonElement content : contentArray) {
                System.out.println(content.getAsJsonObject().get("name").getAsString());
            if (content.getAsJsonObject().get("name").getAsString().equals("Destroy")) {
                 actionID = content.getAsJsonObject().get("id").getAsString();
                 System.out.println(actionID);
                 break;
            }
        }
        return actionID;

    }

    private void getTenant(){

        JsonArray contentArray = this.deploymentResources.getAsJsonArray("content");
        System.out.println("Array content :" + contentArray );

        for (JsonElement content : contentArray) {
            if (content.getAsJsonObject().get("resourceId").getAsString().equals(this.parentResourceID)) {
                this.tenantId = content.getAsJsonObject().get("tenantId").getAsString();
                System.out.println("tenantID :" + this.tenantId );
                break;
            }
        }
    }

    private void getBusinessGroup(){
        JsonArray contentArray = this.deploymentResources.getAsJsonArray("content");
        System.out.println("Array content :" + contentArray );

        for (JsonElement content : contentArray) {
            if (content.getAsJsonObject().get("resourceId").getAsString().equals(this.parentResourceID)) {
                this.businessGroupId = content.getAsJsonObject().get("businessGroupId").getAsString();
                System.out.println("tenantID :" + this.businessGroupId );
                break;
            }
        }
    }

    public void Destroy( String DeploymentName ) throws IOException {

        System.out.println("Destroying Deployment "+DeploymentName);

        // Get ResrouceView to find parentID from name
        this.deploymentResources = this.request.GetResourceView();
        System.out.println("JSON Obj "+this.deploymentResources);

        this.getParentResourceID(DeploymentName);
        // Get actionID for destroy
        this.Destroy();

    }

    public boolean Destroy() throws IOException {

        if( this.parentResourceID == null ) {
            System.out.println("Destroying Deployment");

            DeploymentResources();
            this.getParentResourceID();
        }

        String actionID = this.getDestroyAction();
        getBusinessGroup();
        getTenant();
/*
        JsonObject json = request.getResourceActionsRequestTemplate(parentResourceID, actionID);

        json.addProperty("description", "test");
        JsonObject jsonData = json.getAsJsonObject("data");
        jsonData.addProperty("description", "test");
        jsonData.addProperty("reasons", "test");

        System.out.println(json);
        request.ResourceActionsRequest(parentResourceID, actionID, json);
*/

        System.out.println("JSON Destroy "+ jsonString);


        JsonElement jsonDestroyElement = new JsonParser().parse(jsonString);
        JsonObject jsonDestroyObject = jsonDestroyElement.getAsJsonObject();

        JsonObject jsonResourceReb = jsonDestroyObject.getAsJsonObject("resourceRef");
        jsonResourceReb.addProperty("id", this.parentResourceID);

        JsonObject jsonResourceAction = jsonDestroyObject.getAsJsonObject("resourceActionRef");
        jsonResourceAction.addProperty("id", actionID);

        JsonObject jsonOrganizationAction = jsonDestroyObject.getAsJsonObject("organization");
        jsonOrganizationAction.addProperty("tenantRef", this.tenantId);
        jsonOrganizationAction.addProperty("tenantLabel", this.tenantId);
        jsonOrganizationAction.addProperty("subtenantRef", this.businessGroupId);





        System.out.println("JSON Destroy "+jsonDestroyObject.toString());

        request.PostRequest(jsonDestroyObject.toString());

        return true;

    }

}
