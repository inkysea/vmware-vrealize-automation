package com.inkysea.vmware.vra.jenkins.plugin.model

import org.junit.Test

/**
 * Created by kthieler on 2/23/16.
 */
class DeploymentTest extends GroovyTestCase {

    private PluginParam params;
    private PrintStream logger;


    private String userName = "cloudadmin@corp.local";
    private String password = "VMware1!";
    private String tenant = "vsphere.local";
    private String vRAURL = "https://vra-app-1.inkysea.com/";
    private String blueprintName = "CentOS_7";
    private boolean waitExec = true;
    protected List<Deployment> deployments = new ArrayList<Deployment>();


    DeploymentTest() {
        this.params = new PluginParam(vRAURL, userName, password, tenant, blueprintName, waitExec)
    }


    @Test
    public void testCreateAndDestroy() {
        Deployment request = new Deployment(logger, params)

        request.Create();
        //System.out.println("Machine List  :   "+request.getMachineList());
        this.deployments.add(request);

        System.out.println("Machine List : "+request.getOutputs());

    }

/*
    @Test
    public void testDestroy() {
        Deployment request = new Deployment(logger, params)

        request.Destroy("CentOS_7-09847286");
    }
*/

}


