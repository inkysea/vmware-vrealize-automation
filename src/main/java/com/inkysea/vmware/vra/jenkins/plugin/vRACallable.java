package com.inkysea.vmware.vra.jenkins.plugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.inkysea.vmware.vra.jenkins.plugin.model.PluginParam;

import hudson.remoting.Callable;
import org.jenkinsci.remoting.RoleChecker;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kthieler on 11/25/15.
 */
public class vRACallable implements Callable<Map<String, String>, IOException>, Serializable {
    private PluginParam params;

    public vRACallable(PluginParam params) {
        this.params = params;

    }

    @Override
    public Map<String, String> call() throws IOException {
        Map<String, String> data = new HashMap<String, String>();
        try {
            vRAClient connection = new vRAClient(params);
            JsonObject requestJsonObj = connection.fetchBluePrint(params.getBluePrintName());
            String catalogId = requestJsonObj.getAsJsonObject().getAsJsonPrimitive("catalogItemId").toString();
            catalogId = catalogId.replaceAll("^\"|\"$", "");

            JsonArray contentArray = requestJsonObj.getAsJsonArray("content");
            JsonArray linkArray = requestJsonObj.getAsJsonArray("links");


            String templateURL = linkArray.get(0).getAsJsonObject().get("href").toString();
            templateURL = templateURL.replaceAll("^\"|\"$", "");

            // Get Blueprint Template JSON
            JsonObject tempJsonObj = connection.GetBluePrintTemplate(templateURL);


            // Request to provision blueprint
            JsonObject execJsonRes = connection.ProvisionBluePrint(catalogId, tempJsonObj);

            // Get request status using the location property from JSON
            JsonElement execIdElement = execJsonRes.get("id");

            if (execIdElement != null) {
                String execId = execIdElement.getAsString();
                data.put("VRA_REQUEST_ID", execId);

                if (params.isWaitExec()) {
                    System.out.println("Getting Execution Status : " + execId);

                    while (!connection.IsRequestComplete(connection.RequestStatus(execId))) {
                        System.out.println("Execution status : "+ connection.RequestStatus(execId).toString());
                        Thread.sleep(10 * 1000);
                    }

                    data.put("VRA_REQUEST_EXECUTION_STATUS", connection.RequestStatus(execId).toString());

                    switch (connection.RequestStatus(execId)) {
                        case SUCCESSFUL:
                            System.out.println("Requested complete successfully");
                            break;
                        case FAILED:
                            System.out.println("Request execution failed");
                            throw new IOException("Request execution failed. Please go to vRA for more details");
                        case REJECTED:
                            throw new IOException("Request execution cancelled. Please go to vRA for more details");
                    }
                }
            } else {
                System.out.println("Execution ID is null!!! Throwing exception");
                throw new IOException();

            }

        }catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return data;
    }

    @Override
    public void checkRoles(RoleChecker roleChecker) throws SecurityException {

    }
}
