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


    private List<List<String>> machineList = new ArrayList<List<String>>();
    private ArrayList<String> machineDataList = new ArrayList<String>();

    private List<List<String>> loadBalancerList = new ArrayList<List<String>>();
    private ArrayList<String> loadBalancerDataList = new ArrayList<String>();


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


    private void getMachineList() {

        JsonArray contentArray = this.deploymentResources.getAsJsonArray("content");

        for (JsonElement content : contentArray) {

            if (content.getAsJsonObject().get("resourceType").getAsString().contains("Infrastructure.Virtual")) {

                JsonObject jsonData = content.getAsJsonObject().getAsJsonObject("data");
                JsonArray  networkArray = jsonData.getAsJsonArray("NETWORK_LIST");


                for (JsonElement e : networkArray) {
                    JsonElement jsonNetworkData = e.getAsJsonObject().get("data");

                    machineDataList.add(content.getAsJsonObject().get("resourceType").getAsString());
                    machineDataList.add(jsonData.getAsJsonObject().get("Component").getAsString());
                    machineDataList.add(content.getAsJsonObject().get("name").getAsString());
                    machineDataList.add(jsonNetworkData.getAsJsonObject().get("NETWORK_NAME").getAsString());
                    machineDataList.add(jsonNetworkData.getAsJsonObject().get("NETWORK_ADDRESS").getAsString());

                    machineList.add(machineDataList);

                }

            }
        }

    }

    private void getLoadBalancerList() {

        JsonArray contentArray = this.deploymentResources.getAsJsonArray("content");

        for (JsonElement content : contentArray) {

            if (content.getAsJsonObject().get("resourceType").getAsString().contains("Infrastructure.Network.LoadBalancer")) {

                JsonObject jsonData = content.getAsJsonObject().getAsJsonObject("data");

                loadBalancerDataList.add(content.getAsJsonObject().get("resourceType").getAsString());
                loadBalancerDataList.add(jsonData.getAsJsonObject().get("Name").getAsString());
                loadBalancerDataList.add(jsonData.getAsJsonObject().get("LoadBalancerInfo").getAsString());

                loadBalancerList.add(loadBalancerDataList);

                }

            }

    }

    public Map <String, String> getMachineHashMap() throws IOException {

        Map machineMap = null;

        getMachineList();


        for( List machine : this.machineList ){
            //creat map named grup__machine_name__network_name :  network address
            for ( Object data : machine ){
                System.out.println(data.toString());
            }

        }

        return machineMap;
    }

    public Map<String, String> getDeploymentComponents(int count) {
        // Prefix outputs with stack name to prevent collisions with other stacks created in the same build.
        HashMap<String, String> map = new HashMap<String, String>();

        String deploymentName = getDeploymentName();

        map.put("DEPLOYMENT_"+count+"_NAME", deploymentName);
        map.put("DEPLOYMENT_"+count+"_TENANT", params.getTenant());


        getMachineList();

        for( List machine : this.machineList ){
            //creat map named grup__machine_name__network_name :  network address
            for ( Object data : machine ){
                //tenant_deployment_group_machine_network = IP
                map.put(params.getTenant() + "_" + deploymentName+"_"+
                                machine.get(1).toString()+"_"+ machine.get(2).toString()+"_"+
                                machine.get(3).toString(),
                                machine.get(4).toString());
            }

        }

        getLoadBalancerList();

        for( List loadbalancer : this.loadBalancerList ){
            //creat map named grup__machine_name__network_name :  network address
            for ( Object data : loadbalancer ){
                //tenant_deployment_group_machine_network = IP
                map.put(params.getTenant() + "_" + deploymentName+
                                loadbalancer.get(1).toString(),
                                loadbalancer.get(2).toString());
            }

        }



        return map;
    }

    public String getDeploymentName(){

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
