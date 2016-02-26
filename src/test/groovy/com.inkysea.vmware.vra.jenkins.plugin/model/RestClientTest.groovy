package com.inkysea.vmware.vra.jenkins.plugin.model

import groovy.util.GroovyTestCase;
import org.junit.Test;


/**
 * Created by kthieler on 2/23/16.
 */
class RestClientTest extends GroovyTestCase  {

    private PluginParam params;
    private PrintStream logger;

    private String userName = "cloudadmin@corp.local";
    private String password = "VMware1!";
    private String tenant   = "vsphere.local";
    private String vRAURL   = "https://vra-app-1.inkysea.com/";
    private String blueprintName   = "CentOS_7";
    private boolean waitExec   = true;


    RestClientTest() {

        this.params = new PluginParam( vRAURL, userName, password, tenant, blueprintName, waitExec)

        this.logger = logger;
    }



    @Test
    public void testAuth() {
        RestClient connect = new RestClient( params )
        def token = connect.token;
        logger.println(token)
    }
}

