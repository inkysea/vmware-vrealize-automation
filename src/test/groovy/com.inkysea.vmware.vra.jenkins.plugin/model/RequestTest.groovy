package com.inkysea.vmware.vra.jenkins.plugin.model

import org.junit.Test
import java.util.logging.Logger;
import com.inkysea.vmware.vra.jenkins.plugin.model.ExecutionStatus;

/**
 * Created by kthieler on 2/23/16.
 */
class RequestTest extends GroovyTestCase {

    private PluginParam params;
    private PrintStream logger;


    private String userName = "cloudadmin@corp.local";
    private String password = "VMware1!";
    private String tenant = "vsphere.local";
    private String vRAURL = "https://vra-app-1.inkysea.com/";
    private String blueprintName = "CentOS_7";
    private boolean waitExec = true;


    RequestTest() {
        this.params = new PluginParam(vRAURL, userName, password, tenant, blueprintName, waitExec)
    }


    @Test
    public void testfetchBlueprint() {
        Request request = new Request(logger, params)
        def token = request.fetchBluePrint();
        logger.println(token)
    }

    @Test
    public void testGetBluePrintTemplate() {
        Request request = new Request(logger, params)
        def token = request.GetBluePrintTemplate();
        logger.println(token)
    }

    @Test
    public void testProvisionBluePrint() {
        Request request = new Request(logger, params)
        def token = request.ProvisionBluePrint();

        while (!request.IsRequestComplete()) {
            System.out.println("Execution status : " + request.RequestStatus().toString());
            Thread.sleep(10 * 1000);
        }

        switch (request.RequestStatus().toString()) {
            case "SUCCESSFUL":
                System.out.println("Requested complete successfully");
                break;
            case "FAILED":
                System.out.println("Request execution failed");
                throw new IOException("Request execution failed. Please go to vRA for more details");
            case "REJECTED":
                throw new IOException("Request execution cancelled. Please go to vRA for more details");
        }

        System.out.println("Resource View :"+request.GetResourceView().toString());

    }


}


