package com.inkysea.vmware.vra.jenkins.plugin.model

import groovy.util.GroovyTestCase;
import org.junit.Test;


/**
 * Created by kthieler on 2/23/16.
 */
class RestClientTest extends GroovyTestCase  {

    private PluginParam params;
    private PrintStream logger;


    RestClientTest() {

        this.logger = logger;

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
    public void testAuth() {
        RestClient connect = new RestClient( params )
        def token = connect.token;
        logger.println(token)
    }
}

