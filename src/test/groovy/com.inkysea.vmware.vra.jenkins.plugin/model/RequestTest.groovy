package com.inkysea.vmware.vra.jenkins.plugin.model

import org.junit.Test
import java.util.logging.Logger;
import com.inkysea.vmware.vra.jenkins.plugin.model.ExecutionStatus;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by kthieler on 2/23/16.
 */
class RequestTest extends GroovyTestCase {

    private PluginParam params;
    private PrintStream logger;



    RequestTest() {

        Properties prop = new Properties();
        InputStream input = null;

        try {

            String filename = "config.properties";
            input = getClass().getClassLoader().getResourceAsStream(filename);
            if(input==null){
                System.out.println("Sorry, unable to find " + filename);
                return;
            }

            prop.load(input);
            this.params = new PluginParam(prop.getProperty("vRAURL"),
                                        prop.getProperty("userName"),
                                        prop.getProperty("password"),
                                        prop.getProperty("tenant"),
                                        prop.getProperty("bluePrintName"),
                                        prop.getProperty("waitExec"))

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally{
            if(input!=null){
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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


