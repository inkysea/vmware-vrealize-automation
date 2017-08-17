package com.inkysea.vmware.vra.jenkins.plugin.model;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import net.sf.json.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;


/**
 * Created by kthieler on 2/24/16.
 */
public class Deployment {

    private PluginParam params;
    private DestroyParam dParams;

    private Request request;
    private PrintStream logger;
    private String DESTROY_TEMPLATE_URL;
    private String DESTROY_URL;
    private String deploymentName;
    private String parentResourceID;
    private JsonObject deploymentResources;
    private String businessGroupId;
    private String tenantId;
    public JsonObject bluePrintTemplate;
    private String cataologID;
    private String subtenantRef;

    private String jsonString = "{\"@type\":\"ResourceActionRequest\", \"resourceRef\":{\"id\":\"\"}, \"resourceActionRef\"\n" +
            ":{\"id\":\"\"}, \"organization\":{\"tenantRef\":\"\", \"tenantLabel\"\n" +
            ":\"\", \"subtenantRef\":\"\", \"subtenantLabel\":\"\"\n" +
            "}, \"state\":\"SUBMITTED\", \"requestNumber\":0, \"requestData\":{\"entries\":[]}}";

    private Set<String> componentSet = new HashSet<String>();

    private List<List<String>> machineList = new ArrayList<List<String>>();
    private ArrayList<String> machineDataList = new ArrayList<String>();



    private List<List<String>> loadBalancerList = new ArrayList<List<String>>();
    private ArrayList<String> loadBalancerDataList = new ArrayList<String>();


    public Deployment(PrintStream logger, PluginParam params) throws IOException {

        this.params = params;
        this.logger = logger;

        this.request  = new Request(logger, params);

    }

    public Deployment(PrintStream logger, DestroyParam params) throws IOException {

        this.dParams = params;
        this.logger = logger;

        this.request  = new Request(logger, params);


    }

    public boolean Create() throws IOException, InterruptedException {

        boolean rcode = false;

        if ( params.getRequestTemplate()) {

            logger.println("Requesting Blueprint Template");
            this.bluePrintTemplate = this.request.GetBluePrintTemplate();
            JsonParser parser = new JsonParser();

            for ( RequestParam option : params.getRequestParams()){
                if ( option.getJson() == null ){

                    logger.println("Request Parameter is null. skipping to next parameter");

                }else {
                    logger.println("Request Parameter : " + option.getJson());

                    this.bluePrintTemplate = merge(this.bluePrintTemplate.getAsJsonObject(),
                            parser.parse(option.getJson()).getAsJsonObject());
                }
            }
            request.ProvisionBluePrint(this.bluePrintTemplate);

        }else{

            JsonObject bpDetails = request.fetchBluePrint();

            JsonArray contentArray = bpDetails.getAsJsonArray("content");

            for (JsonElement content : contentArray ){

                if( content.getAsJsonObject().get("name").getAsString().equalsIgnoreCase(params.getBluePrintName())){

                    this.cataologID= content.getAsJsonObject().get("catalogItemId").getAsString();

                    JsonArray orgArray = content.getAsJsonObject().getAsJsonArray("entitledOrganizations");

                    for ( JsonElement org : orgArray ){
                        if( org.getAsJsonObject().get("tenantLabel").getAsString() != null) {
                            this.subtenantRef= org.getAsJsonObject().get("subtenantRef").getAsString();
                            break;
                        }

                    }

                    break;
                }
            }

            if(this.cataologID == null ){
                throw new IOException("Did not find the catalogID value from the provided blueprint : "
                        +params.getBluePrintName()+"\nPlease validate blueprint name in vRA");
            }



            if(this.subtenantRef == null ){
                throw new IOException("Did not find the subtenantRef value from the provided tenant name : "
                                        +params.getTenant()+"\nPlease validate tenant name in vRA");
            }

            this.bluePrintTemplate = requestCreateJSON();
            String json = this.bluePrintTemplate.toString();
            System.out.println("Requesting Blueprint with JSON body : " + json);
            request.PostRequestJson(this.bluePrintTemplate.toString());
        }


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

    private JsonObject requestCreateJSON(){

        // create the albums object
        JsonObject requestJson = new JsonObject();
        // add a property calle title to the albums object
            requestJson.addProperty("@type","CatalogItemRequest");

            JsonObject catalogItemRef = new JsonObject();
                catalogItemRef.addProperty("id",this.cataologID);
            requestJson.add("catalogItemRef", catalogItemRef);

            JsonObject organization = new JsonObject();
                organization.addProperty("tenantRef",this.params.getTenant());
                organization.addProperty("subtenantRef",this.subtenantRef);
            requestJson.add("organization", organization);

            requestJson.addProperty("requestedFor",this.params.getUserName());
            requestJson.addProperty("state","SUBMITTED");
            requestJson.addProperty("requestNumber", 0);


                JsonObject requestData = new JsonObject();


                    JsonArray entriesArray = new JsonArray();
                    JsonObject entries = new JsonObject();

                        entries.addProperty("key", "requestedFor");

                        JsonObject value = new JsonObject();
                            value.addProperty("type","string");
                            value.addProperty("value",this.params.getUserName());

                        entries.add("value", value);

                    entriesArray.add(entries);

                    for ( RequestParam option : params.getRequestParams()) {

                        if ( option.getJson() == null ){

                            logger.println("Request Parameter is null. skipping to next parameter");

                        }else {
                            logger.println("Request Parameter : " + option.getJson());
                            JsonElement response = new JsonParser().parse(option.getJson()).getAsJsonObject();
                            entriesArray.add(response);
                        }

                    }

                requestData.add("entries", entriesArray);
            requestJson.add("requestData", requestData);

        return requestJson;

    }


    private void getMachineList() {

        JsonArray contentArray = this.deploymentResources.getAsJsonArray("content");

        for (JsonElement content : contentArray) {

            if (content.getAsJsonObject().get("resourceType").getAsString().contains("Infrastructure.Virtual")) {

                JsonObject jsonData = content.getAsJsonObject().getAsJsonObject("data");
                JsonArray  networkArray = jsonData.getAsJsonArray("NETWORK_LIST");

                componentSet.add(jsonData.getAsJsonObject().get("Component").getAsString());

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

    public Map<String, String> getDeploymentComponents(String count) {
        // Prefix outputs with stack name to prevent collisions with other stacks created in the same build.
        HashMap<String, String> map = new HashMap<String, String>();

        String deploymentName = getDeploymentName();
        String prefixDep = "VRADEP_"+count+"_";
        map.put( prefixDep+"NAME", deploymentName);
        map.put( prefixDep+"TENANT", params.getTenant());


        getMachineList();

        int componentCounter=1;
        for ( String component : componentSet ) {

            String componentPrefix = prefixDep + "COMPONENT" + componentCounter;
            String componentMachinePrefix = "";

            //VRADEP_PB_1_COMPONENT#_NAME=
            map.put(componentPrefix + "_NAME", component.toUpperCase());

            Set<String> machineSet = new HashSet<String>();

            for (List machine : this.machineList) {
                if (machine.get(1).toString().equalsIgnoreCase(component)) {
                    machineSet.add(machine.get(2).toString());
                }
            }

            int machineCounter = 1;
            Set<String> networkSet = new HashSet<String>();

            for (String machines : machineSet) {

                for (List machine : this.machineList) {
                    if (machine.get(1).toString().equalsIgnoreCase(component)
                            && machine.get(2).toString().equalsIgnoreCase(machines)) {
                        componentMachinePrefix = componentPrefix + "_MACHINE" + machineCounter;

                        //VRADEP_PB_1_COMPONENT#_MACHINE#_NAME=

                        map.put(componentMachinePrefix + "_NAME", machine.get(2).toString().toUpperCase());
                        networkSet.add(machine.get(3).toString());
                        break;
                    }
                }

                for (String network : networkSet) {
                    int networkCounter = 1;
                    for (List machine : this.machineList) {
                        int ipCounter = 1;

                        if (machine.get(1).toString().equalsIgnoreCase(component)
                                && machine.get(3).toString().equalsIgnoreCase(network)) {

                            String networkMachinePrefix = componentMachinePrefix + "_NETWORK" + networkCounter;

                            //VRADEP_PB_1_COMPONENT#_MACHINE#_NETWORK#_NAME=

                            map.put(networkMachinePrefix + "_NAME", machine.get(3).toString().toUpperCase());

                            //VRADEP_PB_1_COMPONENT#_MACHINE#_NETWORK#_IP#=

                            map.put(networkMachinePrefix + "_IP" + ipCounter, machine.get(4).toString().toUpperCase());
                            ipCounter++;
                            networkCounter++;
                            break;
                        }
                    }

                }

                machineCounter++;

            }
        }

        /*
        for( List machine : this.machineList ){
            //creat map named grup__machine_name__network_name :  network address
            for ( Object data : machine ){
                //VRADEP_PB_1_COMPONENT#_NAME=
                map.put(prefixDep+machine.get(1).toString().toUpperCase()+"_"+
                                machine.get(1).toString().toUpperCase()+"_"+
                                machine.get(2).toString().toUpperCase()+"_"+
                                machine.get(3).toString().toUpperCase(),
                                    machine.get(4).toString().toUpperCase());
                //VRADEP_PB_1_COMPONENT#_MACHINE#_NAME=
                //VRADEP_PB_1_COMPONENT#_MACHINE#_NETWORK#_NAME=
                //VRADEP_PB_1_COMPONENT#_MACHINE#_NETWORK#_IP#=
                map.put("VRADEP_"+count+"_"+params.getTenant().toUpperCase() + "_" +
                                machine.get(1).toString().toUpperCase()+"_"+
                                machine.get(2).toString().toUpperCase()+"_"+
                                machine.get(3).toString().toUpperCase(),
                                machine.get(4).toString().toUpperCase());
            }

        }
        */

        getLoadBalancerList();

        for( List loadbalancer : this.loadBalancerList ){

            int lbCounter = 1;
            for ( Object data : loadbalancer ){
                String lbPrefix = prefixDep+"LB"+lbCounter+"_";
                //VRADEP_PB_1_LBNAME
                map.put(lbPrefix + "NAME",loadbalancer.get(1).toString().toUpperCase());
                map.put(lbPrefix + "SERVICES",loadbalancer.get(2).toString());

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

    public static JsonNode merge(JsonNode mainNode, JsonNode updateNode) {

        Iterator<String> fieldNames = updateNode.fieldNames();
        while (fieldNames.hasNext()) {

            String fieldName = fieldNames.next();
            JsonNode jsonNode = mainNode.get(fieldName);
            // if field exists and is an embedded object
            if (jsonNode != null && jsonNode.isObject()) {
                merge(jsonNode, updateNode.get(fieldName));
            }
            else {
                if (mainNode instanceof ObjectNode) {
                    // Overwrite field
                    JsonNode value = updateNode.get(fieldName);
                    ((ObjectNode) mainNode).put(fieldName, value);
                }
            }

        }

        return mainNode;
    }

    public static JsonObject merge(JsonObject mainJson, JsonObject updateJson) throws IOException {

        JsonParser parser = new JsonParser();
        JsonObject returnJSON;

        ObjectMapper mapper = new ObjectMapper();

        String json1 = mainJson.toString();
        String json2 = updateJson.toString();

        System.out.println("Original BP request : " + json1);
        System.out.println("JSON to merge : " + json2);


        JsonNode mainNode = mapper.readTree(json1);
        returnJSON = parser.parse(mainNode.toString()).getAsJsonObject();
        JsonNode updateNode = mapper.readTree(json2);

        returnJSON = parser.parse(merge(mainNode,updateNode).toString()).getAsJsonObject();

        /*Iterator<String> fieldNames = updateNode.fieldNames();

        while (fieldNames.hasNext()) {
            String updatedFieldName = fieldNames.next();
            System.out.println("FieldName Next : "+updatedFieldName );

            JsonNode valueToBeUpdated = mainNode.get(updatedFieldName);
            System.out.println("valueToBeUpdated  : "+valueToBeUpdated.toString() );

            JsonNode updatedValue = updateNode.get(updatedFieldName);
            System.out.println("updatedValue  : "+updatedValue.toString() );


            // If the node is an @ArrayNode
            if (valueToBeUpdated != null && updatedValue.isArray()) {
                // running a loop for all elements of the updated ArrayNode
                for (int i = 0; i < updatedValue.size(); i++) {
                    JsonNode updatedChildNode = updatedValue.get(i);
                    // Create a new Node in the node that should be updated, if there was no corresponding node in it
                    // Use-case - where the updateNode will have a new element in its Array
                    if (valueToBeUpdated.size() <= i) {
                        ((ArrayNode) valueToBeUpdated).add(updatedChildNode);
                    }
                    // getting reference for the node to be updated
                    JsonNode childNodeToBeUpdated = valueToBeUpdated.get(i);
                    updatedValue = mapper.readTree( merge(parser.parse(childNodeToBeUpdated.toString()).getAsJsonObject(),
                            parser.parse(updatedChildNode.toString()).getAsJsonObject()).toString());
                }
                // if the Node is an @ObjectNode
            } else if (valueToBeUpdated != null && valueToBeUpdated.isObject()) {
                System.out.println("In ObjectNode "+updatedFieldName);
                //returnJSON =

                //mainNode = mapper.readTree(merge(parser.parse(valueToBeUpdated.toString()).getAsJsonObject(),
                //         parser.parse(updatedValue.toString()).getAsJsonObject()).toString());
                JsonObject test = merge(parser.parse(valueToBeUpdated.toString()).getAsJsonObject(),
                                parser.parse(updatedValue.toString()).getAsJsonObject());
                updatedValue = mapper.readTree(test.toString());
                mainNode = updatedValue;

                System.out.println("Leaving ObjectNode "+updatedFieldName+" with "+updatedValue);


            } else {
                if (mainNode instanceof ObjectNode) {
                    System.out.println("Updating "+updatedFieldName+" from "+valueToBeUpdated+" to "+updatedValue);
                    ((ObjectNode) mainNode).replace(updatedFieldName, updatedValue);
                    System.out.println("JSON after replace : "+mainNode);

                }else{
                    System.out.println("Error ");
                    return returnJSON;
                }
            }
            System.out.println("Done with "+updatedFieldName+" JSON "+mainNode);

        }

        returnJSON = parser.parse(mainNode.toString()).getAsJsonObject();
        System.out.println("Returning with :"+returnJSON);
        */
        return returnJSON;

    }


    public boolean Destroy( String DeploymentName ) throws IOException {

        logger.println("Destroying Deployment "+DeploymentName);

        // Get ResrouceView to find parentID from name
        this.deploymentResources = this.request.GetResourceView(DeploymentName);
        System.out.println("JSON Obj "+this.deploymentResources);

        this.getParentResourceID(DeploymentName);
        // Get actionID for destroy
        return this.Destroy();

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
